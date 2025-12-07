package se.payerl.mobilenotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import se.payerl.mobilenotes.api.NoteDto
import se.payerl.mobilenotes.ui.dialogs.CopyNoteDialog
import se.payerl.mobilenotes.ui.dialogs.ImportListDialog
import se.payerl.mobilenotes.ui.models.CheckboxItem
import se.payerl.mobilenotes.ui.models.JsonKeys
import se.payerl.mobilenotes.ui.models.NoteViewMode
import se.payerl.mobilenotes.ui.parser.JsonNoteContentParser
import se.payerl.mobilenotes.ui.parser.NoteContentParser
import se.payerl.mobilenotes.ui.utils.formatDate

/**
 * Detaljvy för vald anteckning
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailView(
    note: NoteDto,
    onCopyNote: (String, String?) -> Unit,
    onCreateReference: (String, String) -> Unit,
    onLoadExpanded: (String) -> Unit,
    availableNotes: List<NoteDto>,
    onUpdateNote: (String, String, String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier,
    parser: NoteContentParser = remember { JsonNoteContentParser() }
) {
    var showCopyDialog by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(NoteViewMode.CHECKBOX) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header med titel
        Text(
            text = note.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Senast ändrad: ${formatDate(note.lastModified)}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Toolbar med lägesväxlare och verktyg
        NoteDetailToolbar(
            viewMode = viewMode,
            onViewModeChange = { viewMode = it },
            onCopyClick = { showCopyDialog = true },
            onExpandClick = { onLoadExpanded(note.id) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(12.dp))

        // Innehåll baserat på valt läge
        when (viewMode) {
            NoteViewMode.CHECKBOX -> {
                CheckboxListView(
                    note = note,
                    availableNotes = availableNotes,
                    onUpdateNote = onUpdateNote,
                    parser = parser,
                    modifier = Modifier.weight(1f)
                )
            }
            NoteViewMode.FREETEXT -> {
                FreeTextView(
                    note = note,
                    availableNotes = availableNotes,
                    onUpdateNote = onUpdateNote,
                    parser = parser,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Dialog för att kopiera
    if (showCopyDialog) {
        CopyNoteDialog(
            originalTitle = note.title,
            onDismiss = { showCopyDialog = false },
            onConfirm = { newTitle ->
                onCopyNote(note.id, newTitle)
                showCopyDialog = false
            }
        )
    }
}

/**
 * Toolbar för NoteDetailView
 */
@Composable
private fun NoteDetailToolbar(
    viewMode: NoteViewMode,
    onViewModeChange: (NoteViewMode) -> Unit,
    onCopyClick: () -> Unit,
    onExpandClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Toggle mellan lägen
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = viewMode == NoteViewMode.CHECKBOX,
                onClick = { onViewModeChange(NoteViewMode.CHECKBOX) },
                label = { Text("Checkboxar", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.CheckBox,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            FilterChip(
                selected = viewMode == NoteViewMode.FREETEXT,
                onClick = { onViewModeChange(NoteViewMode.FREETEXT) },
                label = { Text("Fritext", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.TextFields,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }

        // Verktygsikoner
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TooltipIconButton(
                tooltip = "Kopiera",
                icon = Icons.Default.FileCopy,
                contentDescription = "Kopiera",
                onClick = onCopyClick
            )

            TooltipIconButton(
                tooltip = "Expandera",
                icon = Icons.Default.UnfoldMore,
                contentDescription = "Expandera",
                onClick = onExpandClick
            )
        }
    }
}

/**
 * Checkbox-läge: Visar varje rad som en checkbox
 */
@Composable
fun CheckboxListView(
    note: NoteDto,
    availableNotes: List<NoteDto>,
    onUpdateNote: (String, String, String) -> Unit,
    parser: NoteContentParser,
    modifier: Modifier = Modifier
) {
    var showImportDialog by remember { mutableStateOf(false) }
    
    // Parse innehållet
    val checkboxItems = remember(note.content) { 
        parser.parseToCheckboxItems(note.content).getOrElse { emptyList() }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp)
            ) {
                items(checkboxItems.size) { index ->
                    val item = checkboxItems[index]
                    CheckboxListItem(
                        item = item,
                        onCheckedChange = { checked ->
                            val updatedItems = checkboxItems.toMutableList()
                            updatedItems[index] = item.copy(checked = checked)
                            saveCheckboxItems(note, updatedItems, onUpdateNote, parser)
                        },
                        onTextChange = { newText ->
                            val updatedItems = checkboxItems.toMutableList()
                            updatedItems[index] = item.copy(text = newText)
                            saveCheckboxItems(note, updatedItems, onUpdateNote, parser)
                        },
                        onDelete = {
                            val updatedItems = checkboxItems.toMutableList()
                            updatedItems.removeAt(index)
                            saveCheckboxItems(note, updatedItems, onUpdateNote, parser)
                        }
                    )
                    if (index < checkboxItems.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 48.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
                
                // Knapp för att lägga till ny rad
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updatedItems = checkboxItems.toMutableList()
                                updatedItems.add(CheckboxItem(
                                    id = kotlin.random.Random.nextInt().toString(),
                                    text = "",
                                    checked = false
                                ))
                                saveCheckboxItems(note, updatedItems, onUpdateNote, parser)
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Lägg till",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lägg till rad",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Knapp för att importera annan lista
        OutlinedButton(
            onClick = { showImportDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Importera lista med \${list.namn}")
        }
    }
    
    if (showImportDialog) {
        ImportListDialog(
            availableNotes = availableNotes,
            onDismiss = { showImportDialog = false },
            onConfirm = { selectedNoteId ->
                val selectedNote = availableNotes.find { it.id == selectedNoteId }
                if (selectedNote != null) {
                    val updatedItems = checkboxItems.toMutableList()
                    updatedItems.add(CheckboxItem(
                        id = "ref_${kotlin.random.Random.nextInt()}",
                        text = "\${list.${selectedNote.title}}",
                        checked = false,
                        isReference = true,
                        referencedNoteId = selectedNote.id
                    ))
                    saveCheckboxItems(note, updatedItems, onUpdateNote, parser)
                }
                showImportDialog = false
            }
        )
    }
}

/**
 * En rad i checkbox-listan
 */
@Composable
fun CheckboxListItem(
    item: CheckboxItem,
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var text by remember(item.text) { mutableStateOf(item.text) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Checkbox(
            checked = item.checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Text / TextField
        if (isEditing && !item.isReference) {
            BasicTextField(
                value = text,
                onValueChange = { 
                    text = it
                    onTextChange(it)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
        } else {
            Text(
                text = if (item.text.isEmpty()) "Tom rad..." else item.text,
                modifier = Modifier
                    .weight(1f)
                    .clickable { isEditing = true }
                    .padding(vertical = 8.dp),
                fontSize = 14.sp,
                textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (item.text.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                } else if (item.isReference) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
        
        // Ta bort-knapp
        if (isEditing || text.isEmpty()) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Ta bort",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Fritext-läge: Ett stort textfält
 */
@Composable
fun FreeTextView(
    note: NoteDto,
    availableNotes: List<NoteDto>,
    onUpdateNote: (String, String, String) -> Unit,
    parser: NoteContentParser,
    modifier: Modifier = Modifier
) {
    // Konvertera innehållet till fritext
    var freeText by remember(note.content) { 
        mutableStateOf(
            parser.convertToFreeText(note.content, availableNotes).getOrElse { "" }
        )
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium
        ) {
            BasicTextField(
                value = freeText,
                onValueChange = { newText ->
                    freeText = newText
                    // Spara direkt som fritext
                    val content = buildJsonObject {
                        put(JsonKeys.FREE_TEXT, newText)
                    }.toString()
                    onUpdateNote(note.id, note.title, content)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tips: Skriv \${list.namn} för att kopiera innehållet från en annan lista",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

/**
 * Spara checkbox items till note
 */
private fun saveCheckboxItems(
    note: NoteDto,
    items: List<CheckboxItem>,
    onUpdateNote: (String, String, String) -> Unit,
    parser: NoteContentParser
) {
    val newContent = parser.serializeCheckboxItems(items)
    onUpdateNote(note.id, note.title, newContent)
}

