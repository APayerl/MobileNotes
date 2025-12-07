package se.payerl.mobilenotes.service

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import se.payerl.mobilenotes.database.NotesTable
import se.payerl.mobilenotes.database.dto.NoteDto
import java.util.UUID

class NoteService {

    /**
     * Hämtar alla anteckningar för en specifik användare
     */
    fun getNotesByUserId(userId: String): List<NoteDto> = transaction {
        NotesTable.selectAll()
            .where { NotesTable.userId eq userId }
            .orderBy(NotesTable.lastModified to SortOrder.DESC)
            .map { rowToNoteDto(it) }
    }

    /**
     * Hämtar en specifik anteckning baserat på id och userId
     */
    fun getNoteById(noteId: String, userId: String): NoteDto? = transaction {
        NotesTable.selectAll()
            .where { (NotesTable.id eq noteId) and (NotesTable.userId eq userId) }
            .map { rowToNoteDto(it) }
            .singleOrNull()
    }

    /**
     * Skapar en ny anteckning
     */
    fun createNote(userId: String, title: String, content: String): NoteDto = transaction {
        val noteId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        NotesTable.insert {
            it[id] = noteId
            it[NotesTable.userId] = userId
            it[NotesTable.title] = title
            it[NotesTable.content] = content
            it[lastModified] = now
        }

        NoteDto(noteId, userId, title, content, now)
    }

    /**
     * Uppdaterar en befintlig anteckning
     */
    fun updateNote(noteId: String, userId: String, title: String, content: String): Boolean = transaction {
        val updated = NotesTable.update({
            (NotesTable.id eq noteId) and (NotesTable.userId eq userId)
        }) {
            it[NotesTable.title] = title
            it[NotesTable.content] = content
            it[lastModified] = System.currentTimeMillis()
        }
        updated > 0
    }

    /**
     * Raderar en anteckning
     */
    fun deleteNote(noteId: String, userId: String): Boolean = transaction {
        val deleted = NotesTable.deleteWhere {
            (id eq noteId) and (NotesTable.userId eq userId)
        }
        deleted > 0
    }

    /**
     * Konverterar en databasrad till NoteDto
     */
    private fun rowToNoteDto(row: ResultRow): NoteDto {
        return NoteDto(
            id = row[NotesTable.id],
            userId = row[NotesTable.userId],
            title = row[NotesTable.title],
            content = row[NotesTable.content],
            lastModified = row[NotesTable.lastModified]
        )
    }
}

