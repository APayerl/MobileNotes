package se.payerl.mobilenotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import se.payerl.mobilenotes.data.storage.MyNoteStorage
import kotlin.reflect.KClass

/**
 * ViewModelFactory for ViewModels using MyNoteStorage directly.
 *
 * Simple and straightforward - no singletons, no providers, just dependency injection.
 */
class MyViewModelFactory(
    private val storage: MyNoteStorage
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        return when (modelClass) {
            MyFolderOverviewViewModel::class -> MyFolderOverviewViewModel(storage) as T
            MyNotesListViewModel::class -> MyNotesListViewModel(storage) as T
            NoteDetailViewModel::class -> NoteDetailViewModel(storage) as T
            // Add more ViewModels here as needed
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
        }
    }
}

