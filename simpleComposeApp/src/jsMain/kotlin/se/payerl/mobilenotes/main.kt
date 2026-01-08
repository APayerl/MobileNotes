package se.payerl.mobilenotes

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import se.payerl.mobilenotes.data.storage.MyNoteStorage

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Create storage - simple and direct!
    val storage = MyNoteStorage()

    ComposeViewport {
        App(storage = storage)
    }
}

