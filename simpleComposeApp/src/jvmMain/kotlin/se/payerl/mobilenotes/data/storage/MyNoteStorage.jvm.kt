package se.payerl.mobilenotes.data.storage

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import se.payerl.mobilenotes.db.Folder
import se.payerl.mobilenotes.db.MobileNotesDatabase
import se.payerl.mobilenotes.db.Note
import se.payerl.mobilenotes.db.NoteItem
import java.util.UUID

/**
 * JVM/Desktop implementation of MyNoteStorage using SQLDelight with JDBC SQLite driver.
 */
actual class MyNoteStorage {
    private val database: MobileNotesDatabase

    constructor() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        database = MobileNotesDatabase(driver = driver)
    }

    actual fun getFolders(): List<Folder> {
        return database.mobileNotesQueries.getFolders {
            id, name, noteCount, lastModified -> Folder(id, name, noteCount, lastModified)
        }.executeAsList()
    }

    actual fun getFolder(folderId: String): Folder {
        return database.mobileNotesQueries.getFolderById(folderId) {
            id, name, noteCount, lastModified -> Folder(id, name, noteCount, lastModified)
        }.executeAsOne()
    }

    actual fun addFolder(name: String): Folder {
        val folderId = UUID.randomUUID().toString()
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()

        val inserted = database.mobileNotesQueries.addFolder(
            id = folderId,
            name = name,
            lastModified = now
        )

        if(inserted.value < 1) throw Exception("Failed to insert Folder with id $folderId")

        return getFolder(folderId)
    }

    actual fun getNotes(folderId: String): List<Note> {
        return database.mobileNotesQueries.getNotesByFolder(folderId).executeAsList()
    }

    actual fun getNote(noteId: String): Note {
        return database.mobileNotesQueries.getNoteById(noteId).executeAsOne()
    }

    actual fun addNote(folderId: String, name: String): Note {
        val noteId = UUID.randomUUID().toString()
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
        val noteItemId = UUID.randomUUID().toString()

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
