package se.payerl.mobilenotes

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import se.payerl.mobilenotes.data.storage.MyNoteStorage
import se.payerl.mobilenotes.navigation.getNavigationArgument
import se.payerl.mobilenotes.ui.components.FolderOverview
import se.payerl.mobilenotes.ui.components.NoteDetail
import se.payerl.mobilenotes.ui.components.NotesOverview
import se.payerl.mobilenotes.ui.theme.MobileNotesTheme
import se.payerl.mobilenotes.util.AppLogger

@Composable
fun App(storage: MyNoteStorage) {
    MobileNotesTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "folders"
        ) {
            // Screen 1: Folder Overview
            composable("folders") {
                FolderOverview(
                    storage = storage,
                    onFolderClick = { folderId ->
                        AppLogger.info("Navigation", "Navigating to notes for folder: $folderId")
                        navController.navigate("notes/$folderId")
                    }
                )
            }

            // Screen 2: Notes in Folder
            composable(
                route = "notes/{folderId}",
                arguments = listOf(navArgument("folderId") { type = NavType.StringType })
            ) { backStackEntry ->
                // Extract folderId using platform-specific utility
                val folderId = getNavigationArgument(backStackEntry.arguments, "folderId")

                if (folderId.isNullOrBlank()) {
                    AppLogger.error("Navigation", "Invalid or missing folderId")
                    return@composable
                }

                AppLogger.info("Navigation", "Extracted folderId: $folderId")

                NotesOverview(
                    storage = storage,
                    folderId = folderId,
                    onBackClick = {
                        AppLogger.info("Navigation", "Navigating back to folder overview")
                        navController.popBackStack()
                    },
                    onNoteClick = { noteId ->
                        AppLogger.info("Navigation", "Navigating to note detail: $noteId")
                        navController.navigate("note/$noteId")
                    }
                )
            }

            // Screen 3: Note Detail
            composable(
                route = "note/{noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.StringType })
            ) { backStackEntry ->
                // Extract noteId using platform-specific utility
                val noteId = getNavigationArgument(backStackEntry.arguments, "noteId")

                if (noteId.isNullOrBlank()) {
                    AppLogger.error("Navigation", "Invalid or missing noteId")
                    return@composable
                }

                AppLogger.info("Navigation", "Displaying note detail: $noteId")

                // Use NoteDetail with ListView as GUI
                NoteDetail(
                    storage = storage,
                    noteId = noteId,
                    onBackClick = {
                        AppLogger.info("Navigation", "Navigating back from note detail")
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
