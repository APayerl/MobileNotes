package se.payerl.mobilenotes.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object för Room databas
 * Hanterar alla databasoperationer för anteckningar
 */
@Dao
interface NoteDao {
    /**
     * Hämta alla anteckningar som Flow (reaktivt)
     * Sorterade efter senaste ändring (nyaste först)
     */
    @Query("SELECT * FROM notes ORDER BY lastModified DESC")
    fun getAllNotesFlow(): Flow<List<NoteEntity>>

    /**
     * Hämta alla anteckningar (suspend för engångsanrop)
     */
    @Query("SELECT * FROM notes ORDER BY lastModified DESC")
    suspend fun getAllNotes(): List<NoteEntity>

    /**
     * Hämta en specifik anteckning
     */
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): NoteEntity?

    /**
     * Hämta en specifik anteckning som Flow
     */
    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getNoteByIdFlow(noteId: String): Flow<NoteEntity?>

    /**
     * Lägg till eller uppdatera en anteckning
     * OnConflictStrategy.REPLACE = uppdatera om den finns
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    /**
     * Lägg till eller uppdatera flera anteckningar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    /**
     * Uppdatera en befintlig anteckning
     */
    @Update
    suspend fun updateNote(note: NoteEntity)

    /**
     * Ta bort en anteckning
     */
    @Delete
    suspend fun deleteNote(note: NoteEntity)

    /**
     * Ta bort en anteckning baserat på ID
     */
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)

    /**
     * Ta bort alla anteckningar (för testing/reset)
     */
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    /**
     * Räkna antal anteckningar
     */
    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNotesCount(): Int

    /**
     * Sök efter anteckningar baserat på titel eller innehåll
     */
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY lastModified DESC")
    suspend fun searchNotes(query: String): List<NoteEntity>
}

