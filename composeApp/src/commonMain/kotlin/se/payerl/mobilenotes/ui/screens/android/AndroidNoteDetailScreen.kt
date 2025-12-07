package se.payerl.mobilenotes.ui.screens.android

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.payerl.mobilenotes.api.NoteDto
import se.payerl.mobilenotes.ui.components.NoteDetailView
import se.payerl.mobilenotes.ui.utils.formatDate

/**
 * Android-specifik detaljvy för anteckning (fullskärmsläge med back-knapp)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidNoteDetailScreen(
    userId: String,
    note: NoteDto,
    onBack: () -> Unit,
    onCopyNote: (String, String?) -> Unit,
    onCreateReference: (String, String) -> Unit,
    onLoadExpanded: (String) -> Unit,
    onUpdateNote: (String, String, String) -> Unit,
    availableNotes: List<NoteDto>
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = note.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Text(
                            text = formatDate(note.lastModified),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tillbaka"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NoteDetailView(
                note = note,
                onCopyNote = onCopyNote,
                onCreateReference = onCreateReference,
                onLoadExpanded = onLoadExpanded,
                onUpdateNote = onUpdateNote,
                availableNotes = availableNotes,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

