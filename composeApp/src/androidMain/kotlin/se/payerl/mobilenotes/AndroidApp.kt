package se.payerl.mobilenotes

import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import se.payerl.mobilenotes.ui.screens.android.AndroidNoteDetailScreen
import se.payerl.mobilenotes.ui.screens.android.AndroidNotesListScreen
import se.payerl.mobilenotes.viewmodel.AndroidNotesViewModel

// TODO: När online mode aktiveras, återaktivera dessa imports
// import se.payerl.mobilenotes.ui.screens.LoginScreen
// import se.payerl.mobilenotes.viewmodel.NotesUiState
// import se.payerl.mobilenotes.viewmodel.NotesViewModel

/**
 * Android-specifik App-komponent med mobilanpassad navigation
 * 
 * OFFLINE-FIRST MODE (Beta)
 * - Hoppar direkt till anteckningslistan (ingen login)
 * - Använder AndroidNotesViewModel med lokal Room database
 * - All data lagras på enheten
 * 
 * TODO: När online mode aktiveras:
 * - Kommentera ut offline-koden nedan
 * - Aktivera online-koden (kommenterad längre ner)
 * - Återställ LoginScreen navigation
 */
@Composable
fun AndroidApp() {
    MaterialTheme {
        val context = LocalContext.current
        
        // OFFLINE MODE: Använd AndroidNotesViewModel
        val viewModel: AndroidNotesViewModel = viewModel { 
            AndroidNotesViewModel(context) 
        }

        // State från ViewModel
        val notes by viewModel.notes.collectAsState()
        val selectedNote by viewModel.selectedNote.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val currentUserId by viewModel.currentUserId.collectAsState()

        // Android-specifik navigationsstatus
        var showingDetail by remember { mutableStateOf(false) }

        // Hantera Android back-knapp
        BackHandler(enabled = showingDetail) {
            viewModel.clearSelection()
            showingDetail = false
        }

        // OFFLINE MODE: Hoppa direkt till listan, ingen login
        currentUserId?.let { userId ->
            if (showingDetail && selectedNote != null) {
                // Visa detaljvy i fullskärm
                AndroidNoteDetailScreen(
                    userId = userId,
                    note = selectedNote!!,
                    onBack = {
                        viewModel.clearSelection()
                        showingDetail = false
                    },
                    onCopyNote = { noteId, newTitle ->
                        viewModel.copyNote(noteId, newTitle)
                    },
                    onCreateReference = { parentId, referencedId ->
                        viewModel.createReference(parentId, referencedId)
                    },
                    onLoadExpanded = { noteId ->
                        viewModel.loadNoteExpanded(noteId)
                    },
                    onUpdateNote = { noteId, title, content ->
                        viewModel.updateNote(noteId, title, content)
                    },
                    availableNotes = notes.filter { it.id != selectedNote!!.id }
                )
            } else {
                // Visa lista i fullskärm
                AndroidNotesListScreen(
                    userId = userId,
                    notes = notes,
                    isLoading = isLoading,
                    onNoteClick = { note ->
                        viewModel.selectNote(note)
                        showingDetail = true
                    },
                    // TODO: Återaktivera logout när online mode finns
                    onLogout = { /* viewModel.logout() */ },
                    onRefresh = { viewModel.loadNotes() },
                    onCreateNote = { title, content ->
                        viewModel.createNote(title, content)
                        showingDetail = true
                    }
                )
            }
        }
    }
}

/* ============================================================================
 * TODO: ONLINE MODE CODE - Aktivera när backend är redo
 * ============================================================================
 * 
 * Kommentera ut offline-koden ovan och aktivera detta istället:
 * 
 * @Composable
 * fun AndroidApp() {
 *     MaterialTheme {
 *         val viewModel: NotesViewModel = viewModel { NotesViewModel() }
 * 
 *         val uiState by viewModel.uiState.collectAsState()
 *         val currentUserId by viewModel.currentUserId.collectAsState()
 *         val notes by viewModel.notes.collectAsState()
 *         val selectedNote by viewModel.selectedNote.collectAsState()
 *         val isLoading by viewModel.isLoading.collectAsState()
 *         val error by viewModel.error.collectAsState()
 * 
 *         // Android-specifik navigationsstatus
 *         var showingDetail by remember { mutableStateOf(false) }
 * 
 *         // Hantera Android back-knapp
 *         BackHandler(enabled = showingDetail) {
 *             viewModel.clearSelection()
 *             showingDetail = false
 *         }
 * 
 *         when (uiState) {
 *             is NotesUiState.Login -> {
 *                 LoginScreen(
 *                     onLogin = { userId -> viewModel.login(userId) },
 *                     errorMessage = error
 *                 )
 *             }
 *             is NotesUiState.NotesList -> {
 *                 currentUserId?.let { userId ->
 *                     if (showingDetail && selectedNote != null) {
 *                         // Visa detaljvy i fullskärm
 *                         AndroidNoteDetailScreen(
 *                             userId = userId,
 *                             note = selectedNote!!,
 *                             onBack = {
 *                                 viewModel.clearSelection()
 *                                 showingDetail = false
 *                             },
 *                             onCopyNote = { noteId, newTitle ->
 *                                 viewModel.copyNote(noteId, newTitle)
 *                             },
 *                             onCreateReference = { parentId, referencedId ->
 *                                 viewModel.createReference(parentId, referencedId)
 *                             },
 *                             onLoadExpanded = { noteId ->
 *                                 viewModel.loadNoteExpanded(noteId)
 *                             },
 *                             onUpdateNote = { noteId, title, content ->
 *                                 viewModel.updateNote(noteId, title, content)
 *                             },
 *                             availableNotes = notes.filter { it.id != selectedNote!!.id }
 *                         )
 *                     } else {
 *                         // Visa lista i fullskärm
 *                         AndroidNotesListScreen(
 *                             userId = userId,
 *                             notes = notes,
 *                             isLoading = isLoading,
 *                             onNoteClick = { note ->
 *                                 viewModel.selectNote(note)
 *                                 showingDetail = true
 *                             },
 *                             onLogout = { viewModel.logout() },
 *                             onRefresh = { viewModel.loadNotes() },
 *                             onCreateNote = { title, content ->
 *                                 viewModel.createNote(title, content)
 *                                 showingDetail = true
 *                             }
 *                         )
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 * ============================================================================
 */

