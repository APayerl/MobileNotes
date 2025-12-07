package se.payerl.mobilenotes

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import se.payerl.mobilenotes.ui.screens.LoginScreen
import se.payerl.mobilenotes.ui.screens.NotesListScreen
import se.payerl.mobilenotes.viewmodel.NotesUiState
import se.payerl.mobilenotes.viewmodel.NotesViewModel

@Composable
fun App() {
    MaterialTheme {
        val viewModel: NotesViewModel = viewModel { NotesViewModel() }

        val uiState by viewModel.uiState.collectAsState()
        val currentUserId by viewModel.currentUserId.collectAsState()
        val notes by viewModel.notes.collectAsState()
        val selectedNote by viewModel.selectedNote.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val error by viewModel.error.collectAsState()

        when (uiState) {
            is NotesUiState.Login -> {
                LoginScreen(
                    onLogin = { userId -> viewModel.login(userId) },
                    errorMessage = error
                )
            }
            is NotesUiState.NotesList -> {
                currentUserId?.let { userId ->
                    NotesListScreen(
                        userId = userId,
                        notes = notes,
                        selectedNote = selectedNote,
                        isLoading = isLoading,
                        onNoteClick = { note -> viewModel.selectNote(note) },
                        onLogout = { viewModel.logout() },
                        onRefresh = { viewModel.loadNotes() },
                        onCreateNote = { title, content -> viewModel.createNote(title, content) },
                        onCopyNote = { noteId, newTitle -> viewModel.copyNote(noteId, newTitle) },
                        onCreateReference = { parentId, referencedId ->
                            viewModel.createReference(parentId, referencedId)
                        },
                        onLoadExpanded = { noteId -> viewModel.loadNoteExpanded(noteId) },
                        onUpdateNote = { noteId, title, content ->
                            viewModel.updateNote(noteId, title, content)
                        }
                    )
                }
            }
        }
    }
}