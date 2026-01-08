package se.payerl.mobilenotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import se.payerl.mobilenotes.data.storage.MyNoteStorage
import se.payerl.mobilenotes.db.Note
import se.payerl.mobilenotes.ui.OverviewUiState
import se.payerl.mobilenotes.viewmodel.MyNotesListViewModel
import se.payerl.mobilenotes.viewmodel.MyViewModelFactory

/**
 * Notes overview screen - displays all notes within a folder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesOverview(
    storage: MyNoteStorage,
    folderId: String,
    onBackClick: () -> Unit,
    onNoteClick: (String) -> Unit
) {
    // Simple and direct - create ViewModel with storage
    val viewModel: MyNotesListViewModel = viewModel(
        factory = MyViewModelFactory(storage)
    )

    val uiState by viewModel.uiState.collectAsState()

    // Load notes for this folder
    LaunchedEffect(folderId) {
        viewModel.loadNotesForFolder(folderId)
    }

    when (val state = uiState) {
        is OverviewUiState.Loading -> {
            NotesOverviewContent(
                folderName = "Loading...",
                notes = emptyList(),
                isLoading = true,
                onBackClick = onBackClick,
                onNoteClick = {},
                onAddNote = {}
            )
        }
        is OverviewUiState.Success -> {
            NotesOverviewContent(
                folderName = state.name ?: "@@@null@@@",
                notes = state.list,
                isLoading = false,
                onBackClick = onBackClick,
                onNoteClick = onNoteClick,
                onAddNote = { viewModel.createNote("New Note") }
            )
        }
        is OverviewUiState.Error -> {
            NotesOverviewContent(
                folderName = "Error",
                notes = emptyList(),
                isLoading = false,
                error = state.message,
                onBackClick = onBackClick,
                onNoteClick = {},
                onAddNote = {}
            )
        }
    }
}

/**
 * Content component for the notes overview screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesOverviewContent(
    folderName: String,
    notes: List<Note>,
    isLoading: Boolean = false,
    error: String? = null,
    onBackClick: () -> Unit,
    onNoteClick: (String) -> Unit,
    onAddNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Topbar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = folderName,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* Sort */ }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Sort",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(onClick = onAddNote) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(onClick = { /* More */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        )

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
                }
            }
            notes.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No notes yet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to create your first note",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(items = notes, key = { it.id }) { note ->
                        NoteOverviewRow(
                            note = note,
                            onClick = { onNoteClick(note.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * A row displaying a note in the overview
 */
@Composable
fun NoteOverviewRow(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatLastModified(note.lastModified),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Format timestamp to human-readable string
 */
private fun formatLastModified(timestamp: Long): String {
    val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}

//@Preview
//@Composable
//fun Preview() {
//    val lns = LocalNotesStorage()
//    val repo = NotesRepository(lns)
//    val nlvm = NotesListViewModel(repo)
//    MobileNotesTheme {
//        NotesOverview(
//            folderId = "Demo folder",
//            onBackClick = {},
//            onNoteClick = {},
//            viewModel = nlvm)
//    }
//}
