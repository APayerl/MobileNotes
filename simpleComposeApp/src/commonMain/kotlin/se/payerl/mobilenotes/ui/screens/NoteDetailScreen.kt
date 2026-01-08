package se.payerl.mobilenotes.ui.screens

import CheckboxView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import se.payerl.mobilenotes.data.storage.MyNoteStorage
import se.payerl.mobilenotes.db.Note
import se.payerl.mobilenotes.db.NoteItem
import se.payerl.mobilenotes.viewmodel.MyViewModelFactory
import se.payerl.mobilenotes.viewmodel.NoteDetailViewModel
import kotlin.time.Clock

/**
 * Note Detail screen - simplified version with all UI in one file.
 * No complex state management, just simple flows.
 */
@Composable
fun NoteDetailScreen(
    storage: MyNoteStorage,
    noteId: String,
    onBackClick: () -> Unit
) {
    // Create ViewModel with storage
    val viewModel: NoteDetailViewModel = viewModel(
        factory = MyViewModelFactory(storage)
    )

    val note by viewModel.note.collectAsState()
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Load note when composable is first displayed
    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    // Show error as snackbar if present
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading || note == null) {
            // Simple loading indicator
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            NoteDetailContent(
                note = note!!,
                items = items,
                storage = storage,
                onItemChange = viewModel::updateItem,
                onAddItem = { viewModel.addItem("New item") },
                onDeleteItem = viewModel::deleteItem,
                onMoveItem = viewModel::moveItem,
                onBackClick = onBackClick
            )
        }
    }
}

/**
 * Visningsläge för ListView
 */
enum class ListViewMode {
    CHECKLIST,
    PLAIN_TEXT
}

@Composable
private fun NoteDetailContent(
    note: Note,
    items: List<NoteItem>,
    storage: MyNoteStorage,
    onItemChange: (NoteItem) -> Unit,
    onAddItem: () -> Unit,
    onDeleteItem: (String) -> Unit,
    onMoveItem: (fromIndex: Int, toIndex: Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(ListViewMode.CHECKLIST) }
    val titleFieldState = rememberTextFieldState(note.name)
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragTargetIndex by remember { mutableStateOf<Int?>(null) }
    var draggedFromIndex by remember { mutableStateOf<Int?>(null) }
    var cumulativeDragOffset by remember { mutableStateOf(0f) }
    val density = androidx.compose.ui.platform.LocalDensity.current

    // Update titleFieldState when title parameter changes
    LaunchedEffect(note.name) {
        if (titleFieldState.text.toString() != note.name) {
            titleFieldState.edit {
                replace(0, length, note.name)
            }
        }
    }

    // Calculate drop target based on cumulative drag offset
    LaunchedEffect(cumulativeDragOffset, draggedFromIndex) {
        val fromIndex = draggedFromIndex
        if (fromIndex != null) {
            val rowHeightPx = with(density) { 40.dp.toPx() }
            val offsetRows = (cumulativeDragOffset / rowHeightPx).toInt()
            val newTargetIndex = (fromIndex + offsetRows).coerceIn(0, items.size - 1)
            dragTargetIndex = newTargetIndex
        }
    }

    // Format last edited
    val lastEdited = remember(note.lastModified) {
        val now = Clock.System.now().toEpochMilliseconds()
        val diffMillis = now - note.lastModified
        val diffMinutes = diffMillis / (1000 * 60)
        val diffHours = diffMillis / (1000 * 60 * 60)
        val diffDays = diffMillis / (1000 * 60 * 60 * 24)

        when {
            diffMinutes < 1 -> "Last edited just now"
            diffMinutes < 60 -> "Last edited $diffMinutes min ago"
            diffHours < 24 -> "Last edited $diffHours hours ago"
            diffDays < 7 -> "Last edited $diffDays days ago"
            else -> "Last edited long ago"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBar(
            title = titleFieldState.text.toString(),
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
                modifier = Modifier.align(Alignment.End).padding(end = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Titel
            BasicTextField(
                state = titleFieldState,
                textStyle = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            LaunchedEffect(titleFieldState.text.toString()) {
                if (titleFieldState.text.toString() != note.name) {
                    val newNote = note.copy(
                        name = titleFieldState.text.toString(),
                        lastModified = Clock.System.now().toEpochMilliseconds()
                    )
                    storage.updateNote(newNote)
                }
            }

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

                items(
                    count = items.size,
                    key = { index -> items[index].id }
                ) { index ->
                    // Show ghost/shadow of dragged item at drop target position
                    if (dragTargetIndex == index && draggedItemIndex != null && draggedItemIndex != index) {
                        val draggedItem = items[draggedItemIndex!!]
                        NoteDetailContentRow(
                            item = draggedItem,
                            itemIndex = -1,
                            isDragging = false,
                            isDropTarget = false,
                            onDragStart = { },
                            onDrag = { },
                            onDragEnd = { },
                            onChange = { },
                            onDelete = { },
                            storage = storage,
                            modifier = Modifier
                                .height(40.dp)
                                .alpha(0.6f)
                                .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                        )
                    }

                    NoteDetailContentRow(
                        item = items[index],
                        itemIndex = index,
                        isDragging = draggedItemIndex == index,
                        isDropTarget = false,
                        onDragStart = {
                            draggedItemIndex = it
                            draggedFromIndex = it
                            cumulativeDragOffset = 0f
                        },
                        onDrag = { offset ->
                            cumulativeDragOffset += offset
                        },
                        onDragEnd = {
                            val fromIndex = draggedFromIndex
                            val toIndex = dragTargetIndex

                            draggedItemIndex = null
                            dragTargetIndex = null
                            draggedFromIndex = null
                            cumulativeDragOffset = 0f

                            if (fromIndex != null && toIndex != null && fromIndex != toIndex) {
                                onMoveItem(fromIndex, toIndex)
                            }
                        },
                        onChange = { noteItem ->
                            onItemChange(noteItem)
                        },
                        onDelete = {
                            onDeleteItem(items[index].id)
                        },
                        storage = storage,
                        modifier = Modifier
                            .height(40.dp)
                            .alpha(if (draggedItemIndex == index) 0.3f else 1f)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            // Bottom toolbar
            ListFooter(
                viewMode = viewMode,
                onViewModeChange = { viewMode = it },
                onShare = { },
                onTools = { },
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
private fun TopBar(
    title: String,
    navBack: () -> Unit = {},
    sort: () -> Unit = {},
    addItem: () -> Unit = {},
    menu: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
            containerColor = Color(0xFF4CAF50)
        )
    )
}

@Composable
private fun ListFooter(
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

        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
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

@Composable
private fun NoteDetailContentRow(
    item: NoteItem,
    itemIndex: Int,
    isDragging: Boolean,
    isDropTarget: Boolean,
    onDragStart: (Int) -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    storage: MyNoteStorage,
    backColor: Color = Color(0xFFE8F5E9),
    onChange: (NoteItem) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var accumulatedSwipeOffset by remember { mutableStateOf(0f) }
    val swipeThreshold = 50f // pixels needed to trigger indent change

    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                color = when {
                    isDragging -> Color(0xFFBBDEFB)
                    isDropTarget -> Color(0xFFC8E6C9)
                    else -> Color.White
                }
            )
            .pointerInput(itemIndex) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        onDragStart(itemIndex)
                    },
                    onDragEnd = {
                        onDragEnd()
                    },
                    onDragCancel = {
                        onDragEnd()
                    },
                    onDrag = { _, dragAmount ->
                        onDrag(dragAmount.y)
                    }
                )
            }
            .pointerInput(itemIndex, item.indents) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // Check if swipe was significant enough
                        if (accumulatedSwipeOffset > swipeThreshold) {
                            // Swipe right - increase indent
                            val newIndent = (item.indents + 1).coerceAtMost(10) // Max 10 indents
                            if (newIndent != item.indents) {
                                onChange(item.copy(indents = newIndent))
                            }
                        } else if (accumulatedSwipeOffset < -swipeThreshold) {
                            // Swipe left - decrease indent
                            val newIndent = (item.indents - 1).coerceAtLeast(0) // Min 0 indents
                            if (newIndent != item.indents) {
                                onChange(item.copy(indents = newIndent))
                            }
                        }
                        accumulatedSwipeOffset = 0f
                    },
                    onDragCancel = {
                        accumulatedSwipeOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        accumulatedSwipeOffset += dragAmount
                    }
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Spacers for indentation
        repeat(item.indents.toInt()) {
            Spacer(
                modifier = Modifier
                    .aspectRatio(1.0f)
                    .fillMaxHeight()
            )
        }

        // Checkbox
        CheckboxView(
            checked = item.isChecked,
            onItemCheckedChange = { checked ->
                onChange(item.copy(isChecked = checked))
            },
            modifier = Modifier.fillMaxHeight(),
            backColor = backColor
        )

        // Editable text field
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
                    onChange(item.copy(content = textFieldState.text.toString()))
                }
            }
        }
    }
}

