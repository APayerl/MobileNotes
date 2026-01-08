package se.payerl.mobilenotes.data.storage

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import se.payerl.mobilenotes.db.Folder
import se.payerl.mobilenotes.db.MobileNotesDatabase
import se.payerl.mobilenotes.db.Note
import se.payerl.mobilenotes.db.NoteItem
import java.util.UUID

/**
 * Android implementation of MyNoteStorage using SQLDelight with AndroidSqliteDriver.
 *
 * This class provides platform-specific database access for Android.
 * It requires an Android Context to initialize the SQLite driver.
 */
actual class MyNoteStorage {
    private val database: MobileNotesDatabase

    /**
     * Constructor that requires Android Context to initialize the database driver.
     * Fails fast if context is not provided.
     *
     * @param context Android application context
     * @throws IllegalArgumentException if context is null
     */
    constructor(context: Context) {
        val driver = AndroidSqliteDriver(
            schema = MobileNotesDatabase.Schema,
            context = context.applicationContext,
            name = "mobilenotes.db"
        )

        database = MobileNotesDatabase(driver = driver)
    }

    actual fun getFolders(): List<Folder> {
        return database.mobileNotesQueries.getFolders() {
            id, name, noteCount, lastModified ->
            Folder(
                id = id,
                name = name,
                lastModified = lastModified,
                noteCount = noteCount
            )
        }.executeAsList()
    }

    actual fun getFolder(folderId: String): Folder {
        return database.mobileNotesQueries.getFolderById(folderId) {
            id, name, noteCount, lastModified ->
            Folder(
                id = id,
                name = name,
                lastModified = lastModified,
                noteCount = noteCount
            )
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