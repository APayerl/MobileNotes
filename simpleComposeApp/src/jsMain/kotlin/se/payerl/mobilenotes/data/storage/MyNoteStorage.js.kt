package se.payerl.mobilenotes.data.storage

import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker
import se.payerl.mobilenotes.data.adapters.BooleanColumnAdapter
import se.payerl.mobilenotes.db.Folder
import se.payerl.mobilenotes.db.MobileNotesDatabase
import se.payerl.mobilenotes.db.Note
import se.payerl.mobilenotes.db.NoteItem

/**
 * JS/Browser implementation of MyNoteStorage using SQLDelight with WebWorkerDriver.
 */
actual class MyNoteStorage {
    private val database: MobileNotesDatabase

    constructor() {
        // Create WebWorker for SQLDelight
        val worker = Worker(
            js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)""")
        )

        // Create WebWorkerDriver for persistent storage via IndexedDB
        val driver = WebWorkerDriver(worker)
        MobileNotesDatabase.Schema.create(driver)
        database = MobileNotesDatabase(
            driver = driver,
            NoteItemAdapter = NoteItem.Adapter(
                isCheckedAdapter = BooleanColumnAdapter
            )
        )
    }

    actual fun getFolders(): List<Folder> {
        return database.mobileNotesQueries.getFolders().executeAsList().map { result ->
            Folder(
                id = result.id,
                name = result.name,
                lastModified = result.lastModified
            )
        }
    }

    actual fun getFolder(folderId: String): Folder {
        return database.mobileNotesQueries.getFolderById(folderId).executeAsOne()
    }

    actual fun addFolder(name: String): Folder {
        val folderId = generateUuid()
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()

        database.mobileNotesQueries.addFolder(
            id = folderId,
            name = name,
            lastModified = now
        )

        return getFolder(folderId)
    }

    actual fun getNotes(folderId: String): List<Note> {
        return database.mobileNotesQueries.getNotesByFolder(folderId).executeAsList()
    }

    actual fun getNote(noteId: String): Note {
        return database.mobileNotesQueries.getNoteById(noteId).executeAsOne()
    }

    actual fun addNote(folderId: String, name: String): Note {
        val noteId = generateUuid()
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()

        val inserted = database.mobileNotesQueries.addNote(
            id = noteId,
            folderId = folderId,
            name = name,
            lastModified = now
        )
        if(inserted.value < 1) throw Exception("Failed to insert Note with id $noteId")

        return getNote(noteId)
    }

    actual fun getNoteItems(noteId: String): List<NoteItem> {
        return database.mobileNotesQueries.getNoteItemsByNoteId(noteId).executeAsList()
    }

    actual fun getNoteItem(noteItemId: String): NoteItem {
        return database.mobileNotesQueries.getNoteItemById(noteItemId).executeAsOne()
    }

    actual fun addNoteItem(noteItem: NoteItem): NoteItem {
        val noteItemId = generateUuid()
        val inserted = database.mobileNotesQueries.addNoteItem(
            id = noteItemId,
            noteId = noteItem.noteId,
            content = noteItem.content,
            lastModified = noteItem.lastModified,
            isChecked = noteItem.isChecked,
            indents = noteItem.indents
        )
        if(inserted.value < 1) throw Exception("Failed to insert NoteItem with id $noteItemId")
        return getNoteItem(noteItemId)
    }
}

/**
 * Generate a UUID for JS platform.
 */
private fun generateUuid(): String {
    return js("crypto.randomUUID()") as String
}
