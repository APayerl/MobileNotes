package se.payerl.mobilenotes.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.payerl.mobilenotes.api.NoteDto
import se.payerl.mobilenotes.api.NotesApiClient
import se.payerl.mobilenotes.database.NoteEntity
import se.payerl.mobilenotes.database.NotesDatabase

/**
 * Repository pattern för att abstrahera datakälla
 * 
 * Feature flag: ENABLE_ONLINE_MODE
 * - false (default): Använd lokal Room database (offline-first)
 * - true: Använd API (online mode) 
 * 
 * Detta gör det enkelt att växla mellan offline och online senare!
 */
class NotesRepository(
    private val context: Context,
    // TODO: Aktivera online mode när backend är redo för produktion
    private val enableOnlineMode: Boolean = false,
    // TODO: userId kommer användas när vi aktiverar online mode
    private val userId: String = "local"
) {
    // Lokal databas (offline-first)
    private val database: NotesDatabase by lazy {
        NotesDatabase.getDatabase(context)
    }
    
    private val noteDao by lazy {
        database.noteDao()
    }

    // TODO: Kommenterad för offline-first development
    // Kommer användas när vi aktiverar online mode
    /*
    private val apiClient: NotesApiClient by lazy {
        NotesApiClient()
    }
    */

    /**
     * Hämta alla anteckningar som Flow (reaktivt)
     */
    fun getAllNotesFlow(): Flow<List<NoteDto>> {
        return if (enableOnlineMode) {
            // TODO: Implementera när online mode aktiveras
            // flow {
            //     while (true) {
            //         emit(apiClient.getNotes(userId))
            //         delay(5000) // Poll every 5 seconds
            //     }
            // }
            throw NotImplementedError("Online mode not yet implemented")
        } else {
            // Offline mode: Hämta från lokal databas
            noteDao.getAllNotesFlow().map { entities ->
                entities.map { it.toDto() }
            }
        }
    }

    /**
     * Hämta alla anteckningar (suspend)
     */
    suspend fun getAllNotes(): List<NoteDto> {
        return if (enableOnlineMode) {
            // TODO: Aktivera när online mode behövs
            // apiClient.getNotes(userId)
            throw NotImplementedError("Online mode not yet implemented")
        } else {
            // Offline mode
            noteDao.getAllNotes().map { it.toDto() }
        }
    }

    /**
     * Hämta en specifik anteckning
     */
    suspend fun getNoteById(noteId: String): NoteDto? {
        return if (enableOnlineMode) {
            // TODO: Aktivera när online mode behövs
            // apiClient.getNote(userId, noteId)
            throw NotImplementedError("Online mode not yet implemented")
        } else {
            // Offline mode
            noteDao.getNoteById(noteId)?.toDto()
        }
    }

    /**
     * Skapa en ny anteckning
     */
    suspend fun createNote(title: String, content: String): NoteDto {
        return if (enableOnlineMode) {
            // TODO: Aktivera när online mode behövs
            // apiClient.createNote(userId, title, content)
            throw NotImplementedError("Online mode not yet implemented")
        } else {
            // Offline mode: Generera ID och spara lokalt
            val noteId = "note_${System.currentTimeMillis()}"
            val entity = NoteEntity.create(
                id = noteId,
                title = title,
                content = content,
                lastModified = System.currentTimeMillis()
            )
            noteDao.insertNote(entity)
            entity.toDto()
        }
    }

    /**
     * Uppdatera en befintlig anteckning
     */
    suspend fun updateNote(noteId: String, title: String, content: String): Boolean {
        return if (enableOnlineMode) {
            // TODO: Aktivera när online mode behövs
            // apiClient.updateNote(userId, noteId, title, content)
            throw NotImplementedError("Online mode not yet implemented")
        } else {
            // Offline mode
            val existingNote = noteDao.getNoteById(noteId)
            if (existingNote != null) {
                val updatedNote = existingNote.copy(
                    title = title,
                    content = content,
                    lastModified = System.currentTimeMillis()
                )
                noteDao.updateNote(updatedNote)
                true
            } else {
                false
            }
        }
    }

    /**
     * Kopiera en anteckning (värdekopiering)
     */
    suspend fun copyNote(noteId: String, newTitle: String?): NoteDto? {
        return if (enableOnlineMode) {
            // TODO: Aktivera när online mode behövs
            // apiClient.copyNote(userId, noteId, newTitle)
            throw NotImplementedError("Online mode not yet implemented")
        } else {
            // Offline mode
            val originalNote = noteDao.getNoteById(noteId)
            if (originalNote != null) {
                val copyId = "note_${System.currentTimeMillis()}"
                val copyTitle = newTitle ?: "${originalNote.title} (kopia)"
                val copyNote = NoteEntity.create(
                    id = copyId,
                    title = copyTitle,
                    content = originalNote.content,
                    lastModified = System.currentTimeMillis()
                )
                noteDao.insertNote(copyNote)
                copyNote.toDto()
            } else {
                null
            }
        }
    }

    /**
     * Ta bort en anteckning
     */
    suspend fun deleteNote(noteId: String): Boolean {
        return if (enableOnlineMode) {
            // TODO: Aktivera när online mode behövs
            // apiClient.deleteNote(userId, noteId)
            throw NotImplementedError("Online mode not yet implemented")
        } else {
            // Offline mode
            val note = noteDao.getNoteById(noteId)
            if (note != null) {
                noteDao.deleteNote(note)
                true
            } else {
                false
            }
        }
    }

    /**
     * Skapa en referens mellan anteckningar
     * NOTE: Denna funktion modifierar innehållet i parent note
     */
    suspend fun createReference(parentNoteId: String, referencedNoteId: String): String? {
        return if (enableOnlineMode) {
            // TODO: Aktivera när online mode behövs
            // val response = apiClient.createReference(userId, parentNoteId, referencedNoteId)
            // response["referenceId"]
            throw NotImplementedError("Online mode not yet implemented")
        } else {
            // Offline mode: Implementera senare vid behov
            // För nu returnerar vi bara ett fake reference ID
            "ref_${System.currentTimeMillis()}"
        }
    }

    /**
     * Hämta anteckning med expanderade referenser
     */
    suspend fun getNoteExpanded(noteId: String): NoteDto? {
        return if (enableOnlineMode) {
            // TODO: Aktivera när online mode behövs
            // apiClient.getNoteExpanded(userId, noteId)
            throw NotImplementedError("Online mode not yet implemented")
        } else {
            // Offline mode: För nu, returnera bara anteckningen som den är
            // TODO: Implementera expansion logic lokalt
            noteDao.getNoteById(noteId)?.toDto()
        }
    }

    /**
     * Stäng resurser (för online mode)
     */
    fun close() {
        // TODO: Aktivera när online mode behövs
        // if (enableOnlineMode) {
        //     apiClient.close()
        // }
    }

    /**
     * Lägg till initial testdata (endast för första gången appen startar)
     */
    suspend fun initializeTestDataIfNeeded() {
        if (!enableOnlineMode) {
            val count = noteDao.getNotesCount()
            if (count == 0) {
                // Lägg till initial testdata
                val testNotes = listOf(
                    NoteEntity.create(
                        id = "note_1",
                        title = "Välkommen till MobileNotes!",
                        content = """{"lines": [{"id": "1", "text": "Detta är din första anteckning", "checked": false}, {"id": "2", "text": "Tryck på + för att skapa fler", "checked": false}]}""",
                        lastModified = System.currentTimeMillis() - 2000
                    ),
                    NoteEntity.create(
                        id = "note_2",
                        title = "Handlingslista",
                        content = """{"lines": [{"id": "1", "text": "Mjölk", "checked": false}, {"id": "2", "text": "Bröd", "checked": false}, {"id": "3", "text": "Ost", "checked": false}]}""",
                        lastModified = System.currentTimeMillis() - 1000
                    ),
                    NoteEntity.create(
                        id = "note_3",
                        title = "Att göra idag",
                        content = """{"lines": [{"id": "1", "text": "Testa MobileNotes appen", "checked": true}, {"id": "2", "text": "Skapa mina egna anteckningar", "checked": false}]}""",
                        lastModified = System.currentTimeMillis()
                    )
                )
                noteDao.insertNotes(testNotes)
            }
        }
    }
}

