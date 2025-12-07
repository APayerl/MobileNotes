package se.payerl.mobilenotes.database

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = varchar("id", 50)
    val username = varchar("username", 100)
    val email = varchar("email", 100)

    override val primaryKey = PrimaryKey(id)
}

object NotesTable : Table("notes") {
    val id = varchar("id", 50)
    val userId = varchar("user_id", 50)
    val title = varchar("title", 255)
    val content = text("content") // Stored as JSON string
    val lastModified = long("last_modified")

    override val primaryKey = PrimaryKey(id)
}

// Tabell för att spåra referenser mellan anteckningar
object NoteReferencesTable : Table("note_references") {
    val id = varchar("id", 50)  // ID för själva referensen
    val parentNoteId = varchar("parent_note_id", 50)  // Anteckningen som innehåller referensen
    val referencedNoteId = varchar("referenced_note_id", 50)  // Anteckningen som refereras till
    val position = integer("position")  // Position i parent note's content
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

