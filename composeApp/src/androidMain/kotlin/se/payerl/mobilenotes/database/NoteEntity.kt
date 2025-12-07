package se.payerl.mobilenotes.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import se.payerl.mobilenotes.api.NoteDto

/**
 * Room Entity för lokal lagring av anteckningar
 * Offline-first: Data sparas lokalt på enheten
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val lastModified: Long
) {
    /**
     * Konvertera till NoteDto för kompatibilitet med befintlig kod
     */
    fun toDto(): NoteDto {
        return NoteDto(
            id = id,
            userId = "local", // Offline mode: single user per device
            title = title,
            content = content,
            lastModified = lastModified
        )
    }

    companion object {
        /**
         * Skapa NoteEntity från NoteDto
         */
        fun fromDto(dto: NoteDto): NoteEntity {
            return NoteEntity(
                id = dto.id,
                title = dto.title,
                content = dto.content,
                lastModified = dto.lastModified
            )
        }

        /**
         * Skapa NoteEntity från individuella fält
         */
        fun create(
            id: String,
            title: String,
            content: String,
            lastModified: Long = System.currentTimeMillis()
        ): NoteEntity {
            return NoteEntity(
                id = id,
                title = title,
                content = content,
                lastModified = lastModified
            )
        }
    }
}

