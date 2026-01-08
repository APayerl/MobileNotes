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
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * UI State for the Note Detail screen.
 */
sealed interface NoteDetailUiState {
    data object Loading : NoteDetailUiState
    data class Success(val note: Note) : NoteDetailUiState
    data class Error(val message: String) : NoteDetailUiState
}

/**
 * ViewModel for the Note Detail screen using MyNoteStorage directly.
 *
 * Displays and allows editing of a single note.
 * Simple and straightforward - fetches note from storage.
 */
class NoteDetailViewModel(
    private val storage: MyNoteStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoteDetailUiState>(NoteDetailUiState.Loading)
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    private val _items = MutableStateFlow<List<NoteItem>>(emptyList())
    val items: StateFlow<List<NoteItem>> = _items.asStateFlow()

    private var currentNoteId: String? = null

    /**
     * Load a specific note by ID.
     * Fetches directly from storage.
     */
    fun loadNote(noteId: String) {
        currentNoteId = noteId

        viewModelScope.launch {
            _uiState.value = NoteDetailUiState.Loading

            try {
                val dbNote = storage.getNote(noteId)

                // Convert database model to domain model
                val note = Note(
                    id = dbNote.id,
                    folderId = dbNote.folderId,
                    name = dbNote.name,
                    lastModified = dbNote.lastModified
                )

                _items.value = storage.getNoteItems(note.id)
                _uiState.value = NoteDetailUiState.Success(note)
            } catch (exception: Exception) {
                _uiState.value = NoteDetailUiState.Error(
                    exception.message ?: "Failed to load note"
                )
            }
        }
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
                val newItem = NoteItem(
                    id = Uuid.random().toString(),
                    noteId = currentNoteId!!,
                    content = text,
                    isChecked = false,
                    indents = indentLevel,
                    lastModified = Clock.System.now().toEpochMilliseconds()
                )
                val savedItem = storage.addNoteItem(newItem)
                _items.value += savedItem
            } catch (exception: Exception) {
                // Handle error
                println("Failed to add item: ${exception.message}")
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
                // Handle error
                println("Failed to delete item: ${exception.message}")
            }
        }
    }
}
