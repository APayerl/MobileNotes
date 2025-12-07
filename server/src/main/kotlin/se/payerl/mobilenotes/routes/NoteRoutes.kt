package se.payerl.mobilenotes.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import se.payerl.mobilenotes.service.NoteService

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
 * Konfigurerar alla routes för anteckningar
 * Dessa endpoints matchar OpenAPI-specifikationen i documentation.yaml
 */
fun Route.noteRoutes(noteService: NoteService) {
    route("/users/{userId}/notes") {
        // Hämta alla anteckningar för en användare
        get {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "Missing userId",
                status = HttpStatusCode.BadRequest
            )

            val notes = noteService.getNotesByUserId(userId)
            call.respond(notes)
        }

        // Hämta en specifik anteckning
        get("/{noteId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "Missing userId",
                status = HttpStatusCode.BadRequest
            )
            val noteId = call.parameters["noteId"] ?: return@get call.respondText(
                "Missing noteId",
                status = HttpStatusCode.BadRequest
            )

            val note = noteService.getNoteById(noteId, userId)
            if (note != null) {
                call.respond(note)
            } else {
                call.respondText(
                    "Note not found",
                    status = HttpStatusCode.NotFound
                )
            }
        }

        // Skapa en ny anteckning
        post {
            val userId = call.parameters["userId"] ?: return@post call.respondText(
                "Missing userId",
                status = HttpStatusCode.BadRequest
            )

            val request = try {
                call.receive<CreateNoteRequest>()
            } catch (e: Exception) {
                return@post call.respondText(
                    "Invalid request body",
                    status = HttpStatusCode.BadRequest
                )
            }

            val note = noteService.createNote(
                userId = userId,
                title = request.title,
                content = request.content
            )
            call.respond(HttpStatusCode.Created, note)
        }

        // Uppdatera en anteckning
        put("/{noteId}") {
            val userId = call.parameters["userId"] ?: return@put call.respondText(
                "Missing userId",
                status = HttpStatusCode.BadRequest
            )
            val noteId = call.parameters["noteId"] ?: return@put call.respondText(
                "Missing noteId",
                status = HttpStatusCode.BadRequest
            )

            val request = try {
                call.receive<UpdateNoteRequest>()
            } catch (e: Exception) {
                return@put call.respondText(
                    "Invalid request body",
                    status = HttpStatusCode.BadRequest
                )
            }

            val success = noteService.updateNote(
                noteId = noteId,
                userId = userId,
                title = request.title,
                content = request.content
            )

            if (success) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Note updated successfully"))
            } else {
                call.respondText(
                    "Note not found or update failed",
                    status = HttpStatusCode.NotFound
                )
            }
        }

        // Radera en anteckning
        delete("/{noteId}") {
            val userId = call.parameters["userId"] ?: return@delete call.respondText(
                "Missing userId",
                status = HttpStatusCode.BadRequest
            )
            val noteId = call.parameters["noteId"] ?: return@delete call.respondText(
                "Missing noteId",
                status = HttpStatusCode.BadRequest
            )

            val success = noteService.deleteNote(noteId, userId)

            if (success) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Note deleted successfully"))
            } else {
                call.respondText(
                    "Note not found or deletion failed",
                    status = HttpStatusCode.NotFound
                )
            }
        }
    }
}

