package se.payerl.mobilenotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.payerl.mobilenotes.data.storage.MyNoteStorage
import se.payerl.mobilenotes.db.Note
import se.payerl.mobilenotes.db.NoteItem
import se.payerl.mobilenotes.util.AppLogger
import se.payerl.mobilenotes.util.PositionManager
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * ViewModel for the Note Detail screen using MyNoteStorage directly.
 *
 * Simplified version without complex state management.
 * Uses simple flows for note, items, loading state, and errors.
 */
class NoteDetailViewModel(
    private val storage: MyNoteStorage
) : ViewModel() {

    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note.asStateFlow()
    private val _items = MutableStateFlow<List<NoteItem>>(emptyList())
    val items: StateFlow<List<NoteItem>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentNoteId: String? = null

    /**
     * Load a specific note by ID.
     * Fetches directly from storage.
     */
    fun loadNote(noteId: String) {
        currentNoteId = noteId
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val dbNote = storage.getNote(noteId)
                _note.value = dbNote
                _items.value = ArrayList(storage.getNoteItems(noteId))
                _errorMessage.value = null
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "Failed to load note"
                AppLogger.error("NoteDetailViewModel", "Failed to load note: ${exception.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear error message after it's been shown
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Update an item.
     */
    fun updateItem(noteItem: NoteItem) {
        viewModelScope.launch {
            try {
                val updatedItem = noteItem.copy(lastModified = Clock.System.now().toEpochMilliseconds())
                storage.updateNoteItem(updatedItem)
                _items.value = _items.value.map { item ->
                    if (item.id == noteItem.id) updatedItem else item
                }
            } catch (exception: Exception) {
                // Handle error
                AppLogger.error("error", "Failed to update item: ${exception.message}")
            }
        }
    }

    /**
     * Add a new item to the note.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun addItem(text: String, indentLevel: Long = 0) {
        viewModelScope.launch {
            try {
                val currentItems = _items.value
                val maxPosition = currentItems.maxOfOrNull { it.position }
                val newPosition = PositionManager.calculatePositionForNewItem(maxPosition)

                val newItem = NoteItem(
                    id = Uuid.random().toString(),
                    noteId = currentNoteId!!,
                    content = text,
                    isChecked = false,
                    indents = indentLevel,
                    lastModified = Clock.System.now().toEpochMilliseconds(),
                    position = newPosition
                )
                val savedItem = storage.addNoteItem(newItem)
                _items.value += savedItem
            } catch (exception: Exception) {
                AppLogger.error("NoteDetailViewModel", "Failed to add item: ${exception.message}")
            }
        }
    }

    /**
     * Delete an item from the note.
     */
    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            try {
                storage.deleteNoteItem(itemId)
                _items.value = _items.value.filter { it.id != itemId }
            } catch (exception: Exception) {
                AppLogger.error("NoteDetailViewModel", "Failed to delete item: ${exception.message}")
            }
        }
    }

    /**
     * Move an item from one position to another.
     * Uses lazy normalization strategy - calculates midpoint position between items.
     * If UNIQUE constraint violation occurs, normalizes all positions and retries.
     */
    fun moveItem(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return

        viewModelScope.launch {
            // Save IDs before try block so they're accessible in catch
            val originalItems = _items.value
            val itemToMoveId = originalItems[fromIndex].id
            val targetItemId = if (toIndex < originalItems.size) originalItems[toIndex].id else null

            try {
                AppLogger.debug("NoteDetailViewModel", "🔄 Moving item from index $fromIndex to $toIndex")
                val currentItems = _items.value.toMutableList()
                val itemToMove = currentItems[fromIndex]

                // Remove item from current position first
                currentItems.removeAt(fromIndex)

                // Now calculate position based on the adjusted list
                val adjustedToIndex = toIndex.coerceIn(0, currentItems.size)

                val newPosition = when {
                    currentItems.isEmpty() -> 0L
                    adjustedToIndex == 0 -> {
                        // Moving to start - position before first item
                        val firstPos = currentItems[0].position
                        // CRITICAL FIX: If first item is at 0, use negative position to avoid conflict
                        if (firstPos <= 0) {
                            -PositionManager.POSITION_GAP
                        } else {
                            firstPos / 2
                        }
                    }
                    adjustedToIndex >= currentItems.size -> {
                        // Moving to end - position after last item
                        val lastPos = currentItems.last().position
                        lastPos + PositionManager.POSITION_GAP
                    }
                    else -> {
                        // Moving between items
                        val prevPos = currentItems[adjustedToIndex - 1].position
                        val nextPos = currentItems[adjustedToIndex].position
                        val gap = nextPos - prevPos
                        if (gap <= 1) {
                            // No room - need normalization
                            AppLogger.warn("NoteDetailViewModel", "⚠️ Gap too small ($gap) - will normalize")
                            throw Exception("UNIQUE constraint - gap exhausted")
                        }
                        PositionManager.calculatePositionBetween(prevPos, nextPos)
                    }
                }

                AppLogger.debug("NoteDetailViewModel", "📍 New position calculated: $newPosition (from ${itemToMove.position})")

                // Update item with new position
                val updatedItem = itemToMove.copy(
                    position = newPosition,
                    lastModified = Clock.System.now().toEpochMilliseconds()
                )

                storage.updateNoteItem(updatedItem)

                // Reload items from storage (will be sorted by position)
                val reloadedItems = storage.getNoteItems(currentNoteId!!)

                _items.value = ArrayList(reloadedItems)

                AppLogger.info("NoteDetailViewModel", "✅ Move completed. Items count: ${reloadedItems.size}")
                AppLogger.debug("NoteDetailViewModel", "   Positions: ${reloadedItems.map { "${it.content}(${it.position})" }}")

            } catch (exception: Exception) {
                // Check if it's a UNIQUE constraint violation
                if (exception.message?.contains("UNIQUE", ignoreCase = true) == true ||
                    exception.message?.contains("gap exhausted", ignoreCase = true) == true) {
                    AppLogger.warn("NoteDetailViewModel", "⚠️ Position conflict detected - normalizing positions")
                    AppLogger.debug("NoteDetailViewModel", "   Current items: ${_items.value.map { "${it.content}(${it.position})" }}")
                    try {
                        normalizePositions()
                        // CRITICAL FIX: Don't retry with same indices - reload and recalculate
                        val reloadedItems = storage.getNoteItems(currentNoteId!!)
                        _items.value = ArrayList(reloadedItems)

                        // Find NEW indices after normalization using saved IDs
                        val newFromIndex = reloadedItems.indexOfFirst { it.id == itemToMoveId }
                        val newToIndex = if (targetItemId != null) {
                            reloadedItems.indexOfFirst { it.id == targetItemId }
                        } else {
                            reloadedItems.size
                        }

                        if (newFromIndex >= 0 && newFromIndex != newToIndex) {
                            AppLogger.info("NoteDetailViewModel", "🔁 Retrying move with NEW indices: $newFromIndex → $newToIndex")
                            moveItem(newFromIndex, newToIndex)
                        } else {
                            AppLogger.info("NoteDetailViewModel", "✅ Items already in correct position after normalization")
                        }
                    } catch (normException: Exception) {
                        AppLogger.error("NoteDetailViewModel", "❌ Normalization failed: ${normException.message}", normException)
                        // Reload items to at least show current state
                        _items.value = ArrayList(storage.getNoteItems(currentNoteId!!))
                    }
                } else {
                    AppLogger.error("NoteDetailViewModel", "❌ Failed to move item: ${exception.message}", exception)
                }
            }
        }
    }

    /**
     * Re-normalize all item positions with fresh gaps.
     * Called when UNIQUE constraint violation occurs (rare).
     *
     * Strategy: First move all items to negative positions (guaranteed no conflict),
     * then move them to correct positive positions.
     */
    private fun normalizePositions() {
        val noteId = currentNoteId ?: return
        val currentItems = _items.value.sortedBy { it.position }

        val newPositions = PositionManager.normalizePositions(currentItems.size)
        val now = Clock.System.now().toEpochMilliseconds()

        // Step 1: Move all items to negative temporary positions to avoid conflicts
        currentItems.forEachIndexed { index, item ->
            val tempPosition = -(index + 1).toLong() // -1, -2, -3, etc.
            val updatedItem = item.copy(
                position = tempPosition,
                lastModified = now
            )
            storage.updateNoteItem(updatedItem)
        }

        // Step 2: Move all items to their final positive positions
        currentItems.zip(newPositions).forEach { (item, newPos) ->
            val updatedItem = item.copy(
                position = newPos,
                lastModified = now
            )
            storage.updateNoteItem(updatedItem)
        }

        // Reload items with new list instance
        val reloadedItems = storage.getNoteItems(noteId)
        _items.value = ArrayList(reloadedItems)
        AppLogger.info("NoteDetailViewModel", "✅ Normalized ${currentItems.size} items")
   }
}
