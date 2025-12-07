package se.payerl.mobilenotes.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * DTO för API-kommunikation (matchar server-sidan)
 */
@Serializable
data class NoteDto(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val lastModified: Long
)

@Serializable
data class CreateNoteRequest(
    val title: String,
    val content: String
)

@Serializable
data class UpdateNoteRequest(
    val title: String,
    val content: String
)

/**
 * API-klient för att kommunicera med MobileNotes backend
 */
class NotesApiClient(
    private val baseUrl: String = getApiBaseUrl()
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    /**
     * Hämta alla anteckningar för en användare
     */
    suspend fun getNotes(userId: String): List<NoteDto> {
        return client.get("$baseUrl/users/$userId/notes").body()
    }

    /**
     * Hämta en specifik anteckning
     */
    suspend fun getNote(userId: String, noteId: String): NoteDto {
        return client.get("$baseUrl/users/$userId/notes/$noteId").body()
    }

    /**
     * Skapa en ny anteckning
     */
    suspend fun createNote(userId: String, title: String, content: String): NoteDto {
        return client.post("$baseUrl/users/$userId/notes") {
            contentType(ContentType.Application.Json)
            setBody(CreateNoteRequest(title, content))
        }.body()
    }

    /**
     * Uppdatera en anteckning
     */
    suspend fun updateNote(userId: String, noteId: String, title: String, content: String): Boolean {
        return try {
            client.put("$baseUrl/users/$userId/notes/$noteId") {
                contentType(ContentType.Application.Json)
                setBody(UpdateNoteRequest(title, content))
            }
            true
        } catch (e: Exception) {
            println("Error updating note: ${e.message}")
            false
        }
    }

    /**
     * Kopiera en anteckning (värdekopiering)
     */
    suspend fun copyNote(userId: String, noteId: String, newTitle: String? = null): NoteDto {
        return client.post("$baseUrl/users/$userId/notes/$noteId/copy") {
            contentType(ContentType.Application.Json)
            newTitle?.let {
                setBody(mapOf("newTitle" to it))
            }
        }.body()
    }

    /**
     * Skapa en referens till en annan anteckning
     */
    suspend fun createReference(
        userId: String,
        parentNoteId: String,
        referencedNoteId: String
    ): Map<String, String> {
        return client.post("$baseUrl/users/$userId/notes/$parentNoteId/reference") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("referencedNoteId" to referencedNoteId))
        }.body()
    }

    /**
     * Hämta anteckning med expanderade referenser
     */
    suspend fun getNoteExpanded(userId: String, noteId: String): NoteDto {
        return client.get("$baseUrl/users/$userId/notes/$noteId/expanded").body()
    }

    fun close() {
        client.close()
    }
}

