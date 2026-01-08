package se.payerl.mobilenotes

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import se.payerl.mobilenotes.data.storage.MyNoteStorage

fun main() {
    // Create storage - simple and direct!
    val storage = MyNoteStorage()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "MobileNotes",
        ) {
            App(storage = storage)
        }
    }
}

