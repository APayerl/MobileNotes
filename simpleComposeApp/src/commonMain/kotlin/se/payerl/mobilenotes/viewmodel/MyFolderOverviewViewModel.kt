package se.payerl.mobilenotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.payerl.mobilenotes.data.storage.MyNoteStorage
import se.payerl.mobilenotes.db.Folder
import se.payerl.mobilenotes.ui.OverviewUiState

/**
 * ViewModel for Folder Overview using MyNoteStorage directly.
 *
 * This implementation provides better Separation of Concerns (SoC) by:
 * - Directly using the storage layer without intermediate repository
 * - Simple, straightforward data flow
 * - Clear responsibility: fetch folders from storage and present to UI
 *
 * Following SOLID principles:
 * - Single Responsibility: Only manages folder overview UI state
 * - Dependency Inversion: Depends on MyNoteStorage abstraction
 */
class MyFolderOverviewViewModel(
    private val storage: MyNoteStorage
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<OverviewUiState<Folder>>(OverviewUiState.Loading)
    val uiState: StateFlow<OverviewUiState<Folder>> = _uiState.asStateFlow()

    // Loading indicator for refresh operations
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        // Load initial data
        loadFolders()
    }

    /**
     * Load folders from storage.
     */
    fun loadFolders() {
        viewModelScope.launch {
            _uiState.value = OverviewUiState.Loading

            try {
                val folders = storage.getFolders().map { dbFolder ->
                    // Convert database model to domain model
                    Folder(
                        id = dbFolder.id,
                        name = dbFolder.name,
                        noteCount = 0, // TODO: Get actual note count
                        lastModified = dbFolder.lastModified
                    )
                }

                _uiState.value = OverviewUiState.Success(folders)
            } catch (exception: Exception) {
                _uiState.value = OverviewUiState.Error(
                    exception.message ?: "Failed to load folders"
                )
            }
        }
    }

    /**
     * Refresh folders (pull-to-refresh).
     */
    fun refreshFolders() {
        viewModelScope.launch {
            _isRefreshing.value = true

            try {
                val folders = storage.getFolders().map { dbFolder ->
                    Folder(
                        id = dbFolder.id,
                        name = dbFolder.name,
                        noteCount = 0, // TODO: Get actual note count
                        lastModified = dbFolder.lastModified
                    )
                }

                _uiState.value = OverviewUiState.Success(folders)
            } catch (exception: Exception) {
                _uiState.value = OverviewUiState.Error(
                    exception.message ?: "Failed to refresh folders"
                )
            }

            _isRefreshing.value = false
        }
    }

    /**
     * Handle folder selection.
     */
    fun onFolderSelected(folderId: String) {
        // Navigation is handled by the UI layer
        // No business logic needed here for now
    }

    /**
     * Create a new folder.
     */
    fun createFolder(folderName: String) {
        require(folderName.isNotBlank()) { "Folder name cannot be blank" }

        viewModelScope.launch {
            try {
                storage.addFolder(folderName)
                // Reload folders to show the new folder
                loadFolders()
            } catch (exception: Exception) {
                _uiState.value = OverviewUiState.Error(
                    exception.message ?: "Failed to create folder"
                )
            }
        }
    }

    /**
     * Retry loading folders after an error.
     */
    fun retry() {
        loadFolders()
    }
}

