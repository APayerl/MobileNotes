package se.payerl.mobilenotes.ui.components

import CheckboxView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.payerl.mobilenotes.data.storage.MyNoteStorage
import se.payerl.mobilenotes.db.NoteItem

/**
 * Visningsläge för ListView
 */
enum class ListViewMode {
    CHECKLIST,
    PLAIN_TEXT
}

@Composable
fun NoteDetailContent(
    title: String,
    items: List<NoteItem>,
    storage: MyNoteStorage,
    onItemCheckedChange: (String, Boolean) -> Unit,
    onItemTextChange: (String, String) -> Unit,
    onAddItem: () -> Unit,
    onDeleteItem: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    lastEdited: String = "Last edited ----"
) {
    var viewMode by remember { mutableStateOf(ListViewMode.CHECKLIST) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBar(
            title,
            navBack = onBackClick,
            sort = {},
            addItem = onAddItem,
            menu = {}
        )

        // Innehåll
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Senast redigerad info
            Text(
                text = lastEdited,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Titel
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Lista med items
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFE8F5E9)),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(1.dp))
                }

                items(items.size) { index ->
                    NoteDetailContentRow(
                        item = items[index],
                        onCheckedChange = { checked ->
                            onItemCheckedChange(items[index].id, checked)
                        },
                        onTextChange = { newText ->
                            onItemTextChange(items[index].id, newText)
                        },
                        onDelete = {
                            onDeleteItem(items[index].id)
                        },
                        storage = storage,
                        modifier = Modifier.height(40.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            // Bottom toolbar med lägesväxlare
            ListFooter(
                viewMode = viewMode,
                onViewModeChange = { viewMode = it },
                onShare = { /* Dela */ },
                onTools = { /* Verktyg */ },
                verticalPadding = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String,
           navBack: () -> Unit = {},
           sort: () -> Unit = {},
           addItem: () -> Unit = {},
           menu: () -> Unit = {}) {
    // Topbar med grön bakgrund
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = navBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        actions = {
            IconButton(onClick = sort) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sortera",
                    tint = Color.White
                )
            }
            IconButton(onClick = addItem) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Lägg till",
                    tint = Color.White
                )
            }
            IconButton(onClick = menu) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Mer",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF4CAF50) // Grön färg som i skärmdumpen
        )
    )
}

/**
 * Footer-komponent för ShoppingListView
 * Innehåller dela-knapp, lägesväxlare och verktygs-knapp
 */
@Composable
fun ListFooter(
    viewMode: ListViewMode,
    onViewModeChange: (ListViewMode) -> Unit,
    onShare: () -> Unit,
    onTools: () -> Unit,
    foregroundColor: Color = Color(0xFF4CAF50),
    verticalPadding: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color.White)
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(vertical = verticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dela-knapp
        IconButton(
            onClick = onShare,
            modifier = Modifier.fillMaxHeight()
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Dela",
                tint = foregroundColor,
                modifier = Modifier.fillMaxHeight()
            )
        }

        // Lägesväxlare
        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Button(
                onClick = { onViewModeChange(ListViewMode.CHECKLIST) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewMode == ListViewMode.CHECKLIST)
                        foregroundColor
                    else
                        Color.White,
                    contentColor = if (viewMode == ListViewMode.CHECKLIST)
                        Color.White
                    else
                        Color.Black
                ),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 0.dp, bottomEnd = 0.dp, bottomStart = 8.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .border(
                        width = 2.dp,
                        color = foregroundColor,
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 0.dp, bottomEnd = 0.dp, bottomStart = 8.dp)
                    )
                    .padding(2.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "Checklist",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = { onViewModeChange(ListViewMode.PLAIN_TEXT) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewMode == ListViewMode.PLAIN_TEXT)
                        foregroundColor
                    else
                        Color.White,
                    contentColor = if (viewMode == ListViewMode.PLAIN_TEXT)
                        Color.White
                    else
                        Color.Black
                ),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 8.dp, bottomEnd = 8.dp, bottomStart = 0.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .border(
                        width = 2.dp,
                        color = foregroundColor,
                        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 8.dp, bottomEnd = 8.dp, bottomStart = 0.dp)
                    )
                    .padding(2.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "Plain text",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Verktygs-knapp
        IconButton(
            onClick = onTools,
            modifier = Modifier.fillMaxHeight()
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "Verktyg",
                tint = foregroundColor,
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}

/**
 * En rad i listan
 */
@Composable
fun NoteDetailContentRow(
    item: NoteItem,
    storage: MyNoteStorage,
    backColor: Color = Color(0xFFE8F5E9),
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until item.indents) {
            Spacer(modifier = Modifier
                .aspectRatio(1.0f)
                .fillMaxHeight())
        }

        // Checkbox
        CheckboxView(
            checked = item.isChecked,
            onItemCheckedChange = onCheckedChange,
            modifier = Modifier.fillMaxHeight(),
            backColor = backColor
        )

        // Editable text field using BasicTextField for better control
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val textFieldState = rememberTextFieldState(item.content)

            BasicTextField(
                state = textFieldState,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = if (item.isChecked) Color.Gray else Color.Black,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                ),
                modifier = Modifier.fillMaxWidth(),
                lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 5)
            )

            LaunchedEffect(textFieldState.text.toString()) {
                if (textFieldState.text.toString() != item.content) {
                    //Get instance of MyNoteStorage from corect platform
                    storage.addNote(item.id, textFieldState.text.toString())
                    onTextChange(textFieldState.text.toString())
                }
            }
        }
    }
}
