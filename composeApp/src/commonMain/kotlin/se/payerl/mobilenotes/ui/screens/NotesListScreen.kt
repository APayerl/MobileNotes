package se.payerl.mobilenotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import se.payerl.mobilenotes.api.NoteDto
import se.payerl.mobilenotes.ui.components.NoteDetailView
import se.payerl.mobilenotes.ui.components.TooltipIconButton
import se.payerl.mobilenotes.ui.dialogs.CreateNoteDialog
import se.payerl.mobilenotes.ui.models.JsonKeys
import se.payerl.mobilenotes.ui.utils.formatDate

/**
 * Huvudskärm med lista över anteckningar och detaljvy
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    userId: String,
    notes: List<NoteDto>,
    selectedNote: NoteDto?,
    isLoading: Boolean,
    onNoteClick: (NoteDto) -> Unit,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onCreateNote: (String, String) -> Unit,
    onCopyNote: (String, String?) -> Unit,
    onCreateReference: (String, String) -> Unit,
    onLoadExpanded: (String) -> Unit,
    onUpdateNote: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var showCreateNoteDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("MobileNotes - $userId") },
            actions = {
                TooltipIconButton(
                    tooltip = "Uppdatera",
                    icon = Icons.Default.Refresh,
                    contentDescription = "Uppdatera",
                    onClick = onRefresh
                )
                TooltipIconButton(
                    tooltip = "Logga ut",
                    icon = Icons.Default.PowerSettingsNew,
                    contentDescription = "Logga ut",
                    onClick = onLogout
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        // Main content: Lista + Detaljvy
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Vänster: Lista med anteckningar med FAB
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                NotesList(
                    notes = notes,
                    selectedNote = selectedNote,
                    isLoading = isLoading,
                    onNoteClick = onNoteClick,
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Action Button för att skapa ny anteckning
                FloatingActionButton(
                    onClick = { showCreateNoteDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, "Skapa ny anteckning")
                }
            }

            // Höger: Detaljvy (endast om en anteckning är vald)
            if (selectedNote != null) {
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight()
                )
                NoteDetailView(
                    note = selectedNote,
                    onCopyNote = onCopyNote,
                    onCreateReference = onCreateReference,
                    onLoadExpanded = onLoadExpanded,
                    onUpdateNote = onUpdateNote,
                    availableNotes = notes.filter { it.id != selectedNote.id },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Dialog för att skapa ny anteckning
    if (showCreateNoteDialog) {
        CreateNoteDialog(
            onDismiss = { showCreateNoteDialog = false },
            onConfirm = { title ->
                // Skapa tom lista med "lines"-format
                val emptyContent = buildJsonObject {
                    put(JsonKeys.LINES, kotlinx.serialization.json.JsonArray(emptyList()))
                }.toString()
                onCreateNote(title, emptyContent)
                showCreateNoteDialog = false
            }
        )
    }
}

/**
 * Lista med anteckningar
 */
@Composable
fun NotesList(
    notes: List<NoteDto>,
    selectedNote: NoteDto?,
    isLoading: Boolean,
    onNoteClick: (NoteDto) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mina anteckningar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${notes.size} st",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider()

        // Lista
        if (isLoading && notes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (notes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Inga anteckningar ännu",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(notes) { note ->
                    NoteListItem(
                        note = note,
                        isSelected = note.id == selectedNote?.id,
                        onClick = { onNoteClick(note) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * En rad i listan med anteckningar
 */
@Composable
fun NoteListItem(
    note: NoteDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = note.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDate(note.lastModified),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
