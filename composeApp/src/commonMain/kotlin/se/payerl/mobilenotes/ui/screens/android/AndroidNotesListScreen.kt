package se.payerl.mobilenotes.ui.screens.android

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
import se.payerl.mobilenotes.ui.dialogs.CreateNoteDialog
import se.payerl.mobilenotes.ui.models.JsonKeys
import se.payerl.mobilenotes.ui.utils.formatDate

/**
 * Android-specifik lista över anteckningar (fullskärmsläge)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidNotesListScreen(
    userId: String,
    notes: List<NoteDto>,
    isLoading: Boolean,
    onNoteClick: (NoteDto) -> Unit,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onCreateNote: (String, String) -> Unit
) {
    var showCreateNoteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mina anteckningar") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Uppdatera")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = "Logga ut")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateNoteDialog = true }
            ) {
                Icon(Icons.Default.Add, "Skapa ny anteckning")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Notes,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Inga anteckningar ännu",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Tryck på + för att skapa en",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        // Header med användarinfo och antal
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = userId,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${notes.size} anteckningar",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                    
                    items(notes) { note ->
                        AndroidNoteListItem(
                            note = note,
                            onClick = { onNoteClick(note) }
                        )
                        HorizontalDivider()
                    }
                }
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
 * En rad i listan med anteckningar (Android-stil)
 */
@Composable
fun AndroidNoteListItem(
    note: NoteDto,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text
            Column(
                modifier = Modifier.weight(1f)
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
            
            // Chevron
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

