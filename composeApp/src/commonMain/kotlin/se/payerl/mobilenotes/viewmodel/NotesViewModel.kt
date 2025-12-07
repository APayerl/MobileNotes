package se.payerl.mobilenotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.payerl.mobilenotes.api.NotesApiClient
import se.payerl.mobilenotes.api.NoteDto

/**
 * ViewModel för att hantera app-state och API-kommunikation
 */
class NotesViewModel : ViewModel() {
    private val apiClient = NotesApiClient()

    // UI State
    private val _uiState = MutableStateFlow<NotesUiState>(NotesUiState.Login)
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    // Nuvarande användare
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    // Lista med anteckningar
    private val _notes = MutableStateFlow<List<NoteDto>>(emptyList())
    val notes: StateFlow<List<NoteDto>> = _notes.asStateFlow()

    // Vald anteckning
    private val _selectedNote = MutableStateFlow<NoteDto?>(null)
    val selectedNote: StateFlow<NoteDto?> = _selectedNote.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Logga in användare
     */
    fun login(userId: String) {
        if (userId.isBlank()) {
            _error.value = "User ID kan inte vara tomt"
            return
        }

        _currentUserId.value = userId
        _uiState.value = NotesUiState.NotesList
        loadNotes()
    }

    /**
     * Logga ut användare
     */
    fun logout() {
        _currentUserId.value = null
        _notes.value = emptyList()
        _selectedNote.value = null
        _error.value = null
        _uiState.value = NotesUiState.Login
    }

    /**
     * Ladda alla anteckningar för användaren
     */
    fun loadNotes() {
        val userId = _currentUserId.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val fetchedNotes = apiClient.getNotes(userId)
                _notes.value = fetchedNotes
            } catch (e: Exception) {
                _error.value = "Kunde inte ladda anteckningar: ${e.message}"
                println("Error loading notes: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Välj en anteckning för att visa den
     */
    fun selectNote(note: NoteDto) {
        _selectedNote.value = note
    }

    /**
     * Avmarkera vald anteckning
     */
    fun clearSelection() {
        _selectedNote.value = null
    }

    /**
     * Skapa en ny anteckning
     */
    fun createNote(title: String, content: String) {
        val userId = _currentUserId.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val newNote = apiClient.createNote(userId, title, content)
                _notes.value = _notes.value + newNote
                _selectedNote.value = newNote
            } catch (e: Exception) {
                _error.value = "Kunde inte skapa anteckning: ${e.message}"
                println("Error creating note: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Uppdatera en anteckning
     */
    fun updateNote(noteId: String, title: String, content: String) {
        val userId = _currentUserId.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = apiClient.updateNote(userId, noteId, title, content)
                if (success) {
                    // Reload notes to get updated data
                    loadNotes()
                    // Update selected note if it's the one we just updated
                    if (_selectedNote.value?.id == noteId) {
                        val updatedNote = apiClient.getNote(userId, noteId)
                        _selectedNote.value = updatedNote
                    }
                } else {
                    _error.value = "Kunde inte uppdatera anteckning"
                }
            } catch (e: Exception) {
                _error.value = "Kunde inte uppdatera anteckning: ${e.message}"
                println("Error updating note: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Kopiera en anteckning
     */
    fun copyNote(noteId: String, newTitle: String? = null) {
        val userId = _currentUserId.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val copiedNote = apiClient.copyNote(userId, noteId, newTitle)
                _notes.value = _notes.value + copiedNote
                _selectedNote.value = copiedNote
            } catch (e: Exception) {
                _error.value = "Kunde inte kopiera anteckning: ${e.message}"
                println("Error copying note: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Skapa en referens från en anteckning till en annan
     */
    fun createReference(parentNoteId: String, referencedNoteId: String) {
        val userId = _currentUserId.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                apiClient.createReference(userId, parentNoteId, referencedNoteId)
                // Reload notes to see updated content
                loadNotes()
            } catch (e: Exception) {
                _error.value = "Kunde inte skapa referens: ${e.message}"
                println("Error creating reference: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Hämta anteckning med expanderade referenser
     */
    fun loadNoteExpanded(noteId: String) {
        val userId = _currentUserId.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val expandedNote = apiClient.getNoteExpanded(userId, noteId)
                _selectedNote.value = expandedNote
            } catch (e: Exception) {
                _error.value = "Kunde inte ladda expanderad anteckning: ${e.message}"
                println("Error loading expanded note: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        apiClient.close()
    }
}

/**
 * UI State för appen
 */
sealed class NotesUiState {
    object Login : NotesUiState()
    object NotesList : NotesUiState()
}

