package se.payerl.mobilenotes.data.storage

import se.payerl.mobilenotes.db.Folder
import se.payerl.mobilenotes.db.Note
import se.payerl.mobilenotes.db.NoteItem

expect class MyNoteStorage {
    fun getFolders(): List<Folder>
    fun getFolder(folderId: String): Folder
    fun addFolder(name: String): Folder
    fun getNotes(folderId: String): List<Note>
    fun getNote(noteId: String): Note
    fun addNote(folderId: String, name: String): Note
    fun getNoteItems(noteId: String): List<NoteItem>
    fun getNoteItem(noteItemId: String): NoteItem
    fun addNoteItem(noteItem: NoteItem): NoteItem
}
