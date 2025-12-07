package se.payerl.mobilenotes.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.payerl.mobilenotes.api.NoteDto
import se.payerl.mobilenotes.repository.NotesRepository

/**
 * Android-specifik ViewModel för offline-first funktionalitet
 * Använder Repository pattern för att abstrahera datakälla
 * 
 * Feature flag: Repository hanterar växling mellan online/offline
 * 
 * TODO: När online mode aktiveras:
 * - Aktivera userId-hantering
 * - Aktivera login/logout funktionalitet
 * - Lägga till synkronisering mellan lokal och remote data
 */
class AndroidNotesViewModel(context: Context) : ViewModel() {
    private val repository = NotesRepository(
        context = context,
        enableOnlineMode = false, // Offline-first för beta
        userId = "local" // Single user per device
    )

    // UI State - håller kvar strukturen för framtida online mode
    private val _uiState = MutableStateFlow<NotesUiState>(NotesUiState.NotesList) // Skip login i offline mode
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    // TODO: När online mode aktiveras, använd denna för riktig användar-hantering
    // För nu: hårdkodat till "local" för offline mode
    private val _currentUserId = MutableStateFlow<String?>("local")
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

    init {
        // Offline mode: Ladda data direkt vid start
        loadNotes()
        initializeTestData()
    }

    /**
     * Initialisera testdata om databasen är tom
     */
    private fun initializeTestData() {
        viewModelScope.launch {
            try {
                repository.initializeTestDataIfNeeded()
            } catch (e: Exception) {
                println("Error initializing test data: ${e.message}")
            }
        }
    }

    // TODO: Aktivera när online mode implementeras
    /*
    fun login(userId: String) {
        if (userId.isBlank()) {
            _error.value = "User ID kan inte vara tomt"
            return
        }
        _currentUserId.value = userId
        _uiState.value = NotesUiState.NotesList
        loadNotes()
    }
    */

    // TODO: Aktivera när online mode implementeras
    /*
    fun logout() {
        _currentUserId.value = null
        _notes.value = emptyList()
        _selectedNote.value = null
        _error.value = null
        _uiState.value = NotesUiState.Login
    }
    */

    /**
     * Ladda alla anteckningar
     */
    fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val fetchedNotes = repository.getAllNotes()
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
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val newNote = repository.createNote(title, content)
                // Reload för att få senaste listan
                loadNotes()
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
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = repository.updateNote(noteId, title, content)
                if (success) {
                    // Reload notes to get updated data
                    loadNotes()
                    // Update selected note if it's the one we just updated
                    if (_selectedNote.value?.id == noteId) {
                        val updatedNote = repository.getNoteById(noteId)
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
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val copiedNote = repository.copyNote(noteId, newTitle)
                if (copiedNote != null) {
                    loadNotes()
                    _selectedNote.value = copiedNote
                } else {
                    _error.value = "Kunde inte kopiera anteckning"
                }
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
     * TODO: Implementera fullt ut för offline mode
     */
    fun createReference(parentNoteId: String, referencedNoteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                repository.createReference(parentNoteId, referencedNoteId)
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
     * TODO: Implementera expansion logic för offline mode
     */
    fun loadNoteExpanded(noteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val expandedNote = repository.getNoteExpanded(noteId)
                if (expandedNote != null) {
                    _selectedNote.value = expandedNote
                }
            } catch (e: Exception) {
                _error.value = "Kunde inte ladda expanderad anteckning: ${e.message}"
                println("Error loading expanded note: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Ta bort en anteckning
     * Ny funktion för offline mode
     */
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = repository.deleteNote(noteId)
                if (success) {
                    // Om vi tog bort den valda anteckningen, rensa valet
                    if (_selectedNote.value?.id == noteId) {
                        _selectedNote.value = null
                    }
                    loadNotes()
                } else {
                    _error.value = "Kunde inte ta bort anteckning"
                }
            } catch (e: Exception) {
                _error.value = "Kunde inte ta bort anteckning: ${e.message}"
                println("Error deleting note: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.close()
    }
}

