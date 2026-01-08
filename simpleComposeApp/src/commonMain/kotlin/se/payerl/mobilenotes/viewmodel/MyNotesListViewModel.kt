package se.payerl.mobilenotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.payerl.mobilenotes.data.storage.MyNoteStorage
import se.payerl.mobilenotes.db.Note
import se.payerl.mobilenotes.ui.OverviewUiState

/**
 * ViewModel for Notes List using MyNoteStorage directly.
 *
 * Simple and straightforward - fetches notes from storage and presents to UI.
 */
class MyNotesListViewModel(
    private val storage: MyNoteStorage
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<OverviewUiState<Note>>(OverviewUiState.Loading)
    val uiState: StateFlow<OverviewUiState<Note>> = _uiState.asStateFlow()

    private var currentFolderId: String? = null

    /**
     * Load notes for a specific folder.
     */
    fun loadNotesForFolder(folderId: String) {
        currentFolderId = folderId

        viewModelScope.launch {
            _uiState.value = OverviewUiState.Loading

            try {
                // Get folder to verify it exists and get its name
                val folder = try {
                    storage.getFolder(folderId)
                } catch (e: Exception) {
                    // Folder doesn't exist
                    _uiState.value = OverviewUiState.Error(
                        "Folder not found. It may have been deleted."
                    )
                    return@launch
                }

                // Get notes for this folder
                val dbNotes = storage.getNotes(folderId)

                val notes = dbNotes.map { dbNote ->
                    // Convert database model to domain model
                    Note(
                        id = dbNote.id,
                        folderId = dbNote.folderId,
                        name = dbNote.name,
                        lastModified = dbNote.lastModified
                    )
                }

                _uiState.value = OverviewUiState.Success(
                    name = folder.name,
                    list = notes
                )
            } catch (exception: Exception) {
                _uiState.value = OverviewUiState.Error(
                    exception.message ?: "Failed to load notes"
                )
            }
        }
    }

    /**
     * Create a new note in the current folder.
     */
    fun createNote(noteName: String) {
        val folderId = currentFolderId ?: return
        require(noteName.isNotBlank()) { "Note name cannot be blank" }

        viewModelScope.launch {
            try {
                storage.addNote(folderId = folderId, name = noteName)

                // Reload notes to show the new note
                val folder = storage.getFolder(folderId)
                loadNotesForFolder(folderId)
            } catch (exception: Exception) {
                _uiState.value = OverviewUiState.Error(
                    exception.message ?: "Failed to create note"
                )
            }
        }
    }

    /**
     * Retry loading notes after an error.
     */
    fun retry() {
        val folderId = currentFolderId ?: return
        loadNotesForFolder(folderId)
    }
}

