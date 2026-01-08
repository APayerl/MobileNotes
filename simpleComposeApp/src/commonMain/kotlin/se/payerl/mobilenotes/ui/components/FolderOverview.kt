package se.payerl.mobilenotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mobilenotes.simplecomposeapp.generated.resources.Res
import mobilenotes.simplecomposeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource
import se.payerl.mobilenotes.data.storage.MyNoteStorage
import se.payerl.mobilenotes.db.Folder
import se.payerl.mobilenotes.ui.OverviewUiState
import se.payerl.mobilenotes.viewmodel.MyFolderOverviewViewModel
import se.payerl.mobilenotes.viewmodel.MyViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderOverview(
    storage: MyNoteStorage,
    onFolderClick: (String) -> Unit = {}
) {
    // Simple and direct - create ViewModel with storage, no providers!
    val viewModel: MyFolderOverviewViewModel = viewModel(
        factory = MyViewModelFactory(storage)
    )

    val uiState by viewModel.uiState.collectAsState()


    when (val state = uiState) {
        is OverviewUiState.Loading -> {
            FolderOverviewContent(
                folders = emptyList(),
                isLoading = true,
                onFolderClick = {},
                onCreateFolder = {},
                onRefresh = { viewModel.refreshFolders() }
            )
        }
        is OverviewUiState.Success -> {
            FolderOverviewContent(
                folders = storage.getFolders(),
                isLoading = false,
                onFolderClick = { folderId ->
                    viewModel.onFolderSelected(folderId)
                    onFolderClick(folderId)
                },
                onCreateFolder = { viewModel.createFolder("New Folder") },
                onRefresh = { viewModel.refreshFolders() }
            )
        }
        is OverviewUiState.Error -> {
            FolderOverviewContent(
                folders = emptyList(),
                isLoading = false,
                error = state.message,
                onFolderClick = {},
                onCreateFolder = {},
                onRefresh = { viewModel.retry() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderOverviewContent(
    folders: List<Folder>,
    isLoading: Boolean = false,
    error: String? = null,
    onFolderClick: (String) -> Unit,
    onCreateFolder: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier
        .background(MaterialTheme.colorScheme.primary)) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.app_name),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* Sortera */ }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Sortera",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(onClick = onCreateFolder) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "Lägg till",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(onClick = { /* Mer */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Mer",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary // Grön färg som i skärmdumpen
            )
        )

        // Content area
        when {
            isLoading -> {
                // Loading state
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Loading folders...",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            error != null -> {
                // Error state
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Retry",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            folders.isEmpty() -> {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No folders yet",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create your first folder to get started",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp
                    )
                }
            }
            else -> {
                // Folders list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(count = folders.size) { index ->
                        FolderRow(
                            folder = folders[index],
                            onClick = { onFolderClick(folders[index].id) },
                            modifier = Modifier.height(60.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FolderRow(folder: Folder, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(4.dp, 0.dp)
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable(onClick = onClick)  // Make the entire row clickable!
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .padding(4.dp, 4.dp)
                .weight(1F)
                .fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = "Folder to select",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(2.dp)
                    .fillMaxHeight()
                    .aspectRatio(1f))
            Text(
                text = folder.name,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .weight(1f))
            Text(
                text = folder.noteCount.toString(),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(2.dp, 0.dp)
                    .wrapContentSize())
        }
        Row(modifier = Modifier
            .height(1.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)) {}
    }
}
