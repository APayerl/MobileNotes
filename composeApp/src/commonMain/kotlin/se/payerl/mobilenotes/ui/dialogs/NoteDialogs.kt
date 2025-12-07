package se.payerl.mobilenotes.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.payerl.mobilenotes.api.NoteDto
import se.payerl.mobilenotes.ui.components.TooltipTextButton

/**
 * Dialog för att kopiera en anteckning
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyNoteDialog(
    originalTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var newTitle by remember { mutableStateOf("$originalTitle (kopia)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kopiera anteckning") },
        text = {
            Column {
                Text("Ange ny titel för kopian:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Titel") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TooltipTextButton(
                tooltip = "Kopiera",
                text = "Kopiera",
                onClick = { onConfirm(newTitle) }
            )
        },
        dismissButton = {
            TooltipTextButton(
                tooltip = "Avbryt",
                text = "Avbryt",
                onClick = onDismiss
            )
        }
    )
}

/**
 * Dialog för att skapa ny anteckning
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Skapa ny anteckning") },
        text = {
            Column {
                Text("Ange titel för den nya anteckningen:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TooltipTextButton(
                tooltip = "Skapa",
                text = "Skapa",
                onClick = { onConfirm(title) },
                enabled = title.isNotBlank()
            )
        },
        dismissButton = {
            TooltipTextButton(
                tooltip = "Avbryt",
                text = "Avbryt",
                onClick = onDismiss
            )
        }
    )
}

/**
 * Dialog för att lägga till ny rad
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLineDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lägg till ny rad") },
        text = {
            Column {
                Text("Ange text för den nya raden:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Text") },
                    singleLine = false,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TooltipTextButton(
                tooltip = "Lägg till",
                text = "Lägg till",
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            )
        },
        dismissButton = {
            TooltipTextButton(
                tooltip = "Avbryt",
                text = "Avbryt",
                onClick = onDismiss
            )
        }
    )
}

/**
 * Dialog för att skapa en referens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReferenceDialog(
    availableNotes: List<NoteDto>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedNoteId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Skapa referens") },
        text = {
            Column {
                Text("Välj vilken anteckning som ska refereras:")
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    items(availableNotes) { note ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedNoteId = note.id }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedNoteId == note.id,
                                onClick = { selectedNoteId = note.id }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(note.title)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TooltipTextButton(
                tooltip = "Skapa referens",
                text = "Skapa referens",
                onClick = { selectedNoteId?.let { onConfirm(it) } },
                enabled = selectedNoteId != null
            )
        },
        dismissButton = {
            TooltipTextButton(
                tooltip = "Avbryt",
                text = "Avbryt",
                onClick = onDismiss
            )
        }
    )
}

/**
 * Dialog för att importera en lista
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportListDialog(
    availableNotes: List<NoteDto>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedNoteId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Importera lista") },
        text = {
            Column {
                Text("Välj vilken lista som ska importeras:")
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    items(availableNotes) { note ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedNoteId = note.id }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedNoteId == note.id,
                                onClick = { selectedNoteId = note.id }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(note.title, fontWeight = FontWeight.Medium)
                                Text(
                                    "Syntax: \${list.${note.title}}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TooltipTextButton(
                tooltip = "Importera",
                text = "Importera",
                onClick = { selectedNoteId?.let { onConfirm(it) } },
                enabled = selectedNoteId != null
            )
        },
        dismissButton = {
            TooltipTextButton(
                tooltip = "Avbryt",
                text = "Avbryt",
                onClick = onDismiss
            )
        }
    )
}

