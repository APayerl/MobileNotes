package se.payerl.mobilenotes.ui.components

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import se.payerl.mobilenotes.data.storage.MyNoteStorage
import se.payerl.mobilenotes.db.NoteItem
import se.payerl.mobilenotes.viewmodel.MyViewModelFactory
import se.payerl.mobilenotes.viewmodel.NoteDetailUiState
import se.payerl.mobilenotes.viewmodel.NoteDetailViewModel

/**
 * Note Detail screen using ListView as GUI.
 * Connects ListView with NoteDetailViewModel and MyNoteStorage.
 */
@Composable
fun NoteDetail(
    storage: MyNoteStorage,
    noteId: String,
    onBackClick: () -> Unit
) {
    // Create ViewModel with storage
    val viewModel: NoteDetailViewModel = viewModel(
        factory = MyViewModelFactory(storage)
    )

    val uiState by viewModel.uiState.collectAsState()
    val items by viewModel.items.collectAsState()

    // Load note when composable is first displayed
    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    when (val state = uiState) {
        is NoteDetailUiState.Loading -> {
            // Show loading state with empty ListView
            NoteDetailContent(
                noteId = noteId,
                title = "Loading...",
                items = emptyList(),
                onItemChange = { },
                onAddItem = { },
                onDeleteItem = { },
                onBackClick = onBackClick,
                lastEdited = "",
                storage = storage
            )
        }
        is NoteDetailUiState.Success -> {
            // Items are already NoteItems from the database, just use them directly
            NoteDetailContent(
                noteId = noteId,
                title = state.note.name,
                items = items,
                onItemChange = viewModel::updateItem,
                onAddItem = {
                    viewModel.addItem("New item")
                },
                onDeleteItem = { itemId ->
                    viewModel.deleteItem(itemId)
                },
                onBackClick = onBackClick,
                storage = storage,
                lastEdited = formatLastModified(state.note.lastModified)
            )
        }
        is NoteDetailUiState.Error -> {
            // Show error state
            NoteDetailContent(
                noteId = "Error",
                title = "Error",
                items = listOf(
                    NoteItem(
                        id = "error",
                        noteId = "error",
                        content = state.message,
                        isChecked = false,
                        indents = 0,
                        lastModified = 0,
                        position = 0
                    )
                ),
                onItemChange = { },
                onAddItem = { },
                onDeleteItem = { },
                onBackClick = onBackClick,
                lastEdited = "",
                storage = storage
            )
        }
    }
}

/**
 * Format timestamp to human-readable string.
 */
private fun formatLastModified(timestamp: Long): String {
    val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
    val diffMillis = now - timestamp
    val diffMinutes = diffMillis / (1000 * 60)
    val diffHours = diffMillis / (1000 * 60 * 60)
    val diffDays = diffMillis / (1000 * 60 * 60 * 24)

    return when {
        diffMinutes < 1 -> "Last edited just now"
        diffMinutes < 60 -> "Last edited $diffMinutes min ago"
        diffHours < 24 -> "Last edited $diffHours hours ago"
        diffDays < 7 -> "Last edited $diffDays days ago"
        else -> "Last edited long ago"
    }
}

