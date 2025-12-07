package se.payerl.mobilenotes.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import se.payerl.mobilenotes.service.NoteReferenceService
import se.payerl.mobilenotes.service.NoteService

@Serializable
data class CreateReferenceRequest(
    val referencedNoteId: String,
    val position: Int = 0
)

@Serializable
data class CopyNoteRequest(
    val newTitle: String? = null
)

/**
 * Routes för avancerade note-operationer:
 * - Kopiera listor
 * - Referera listor
 * - Expandera referenser
 */
fun Route.noteAdvancedRoutes(
    noteService: NoteService,
    noteReferenceService: NoteReferenceService
) {
    route("/users/{userId}/notes") {

        // Kopiera en anteckning (värdekopiering)
        post("/{noteId}/copy") {
            val userId = call.parameters["userId"] ?: return@post call.respondText(
                "Missing userId",
                status = HttpStatusCode.BadRequest
            )
            val noteId = call.parameters["noteId"] ?: return@post call.respondText(
                "Missing noteId",
                status = HttpStatusCode.BadRequest
            )

            val request = try {
                call.receive<CopyNoteRequest>()
            } catch (e: Exception) {
                CopyNoteRequest()
            }

            val copiedNote = noteReferenceService.copyNote(noteId, userId, request.newTitle)

            if (copiedNote != null) {
                call.respond(HttpStatusCode.Created, copiedNote)
            } else {
                call.respondText(
                    "Failed to copy note",
                    status = HttpStatusCode.NotFound
                )
            }
        }

        // Skapa en referens från en anteckning till en annan
        post("/{noteId}/reference") {
            val userId = call.parameters["userId"] ?: return@post call.respondText(
                "Missing userId",
                status = HttpStatusCode.BadRequest
            )
            val noteId = call.parameters["noteId"] ?: return@post call.respondText(
                "Missing noteId",
                status = HttpStatusCode.BadRequest
            )

            val request = try {
                call.receive<CreateReferenceRequest>()
            } catch (e: Exception) {
                return@post call.respondText(
                    "Invalid request body",
                    status = HttpStatusCode.BadRequest
                )
            }

            val refId = noteReferenceService.createReference(
                parentNoteId = noteId,
                referencedNoteId = request.referencedNoteId,
                userId = userId,
                position = request.position
            )

            if (refId != null) {
                call.respond(HttpStatusCode.Created, mapOf("referenceId" to refId))
            } else {
                call.respondText(
                    "Failed to create reference. Make sure both notes exist.",
                    status = HttpStatusCode.BadRequest
                )
            }
        }

        // Hämta anteckning med expanderade referenser
        get("/{noteId}/expanded") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "Missing userId",
                status = HttpStatusCode.BadRequest
            )
            val noteId = call.parameters["noteId"] ?: return@get call.respondText(
                "Missing noteId",
                status = HttpStatusCode.BadRequest
            )

            val note = noteReferenceService.getNoteWithExpandedReferences(noteId, userId)

            if (note != null) {
                call.respond(note)
            } else {
                call.respondText(
                    "Note not found",
                    status = HttpStatusCode.NotFound
                )
            }
        }

        // Hämta alla referenser från en anteckning
        get("/{noteId}/references") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "Missing userId",
                status = HttpStatusCode.BadRequest
            )
            val noteId = call.parameters["noteId"] ?: return@get call.respondText(
                "Missing noteId",
                status = HttpStatusCode.BadRequest
            )

            // Verifiera att anteckningen finns och tillhör användaren
            val note = noteService.getNoteById(noteId, userId)
            if (note == null) {
                return@get call.respondText(
                    "Note not found",
                    status = HttpStatusCode.NotFound
                )
            }

            val references = noteReferenceService.getReferencesFromNote(noteId)
            call.respond(mapOf("referencedNoteIds" to references))
        }

        // Hämta alla anteckningar som refererar till denna anteckning
        get("/{noteId}/referenced-by") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "Missing userId",
                status = HttpStatusCode.BadRequest
            )
            val noteId = call.parameters["noteId"] ?: return@get call.respondText(
                "Missing noteId",
                status = HttpStatusCode.BadRequest
            )

            // Verifiera att anteckningen finns och tillhör användaren
            val note = noteService.getNoteById(noteId, userId)
            if (note == null) {
                return@get call.respondText(
                    "Note not found",
                    status = HttpStatusCode.NotFound
                )
            }

            val referencedBy = noteReferenceService.getReferencesToNote(noteId)
            call.respond(mapOf("referencedByNoteIds" to referencedBy))
        }

        // Ta bort alla referenser från en anteckning
        delete("/{noteId}/references") {
            val userId = call.parameters["userId"] ?: return@delete call.respondText(
                "Missing userId",
                status = HttpStatusCode.BadRequest
            )
            val noteId = call.parameters["noteId"] ?: return@delete call.respondText(
                "Missing noteId",
                status = HttpStatusCode.BadRequest
            )

            // Verifiera att anteckningen finns och tillhör användaren
            val note = noteService.getNoteById(noteId, userId)
            if (note == null) {
                return@delete call.respondText(
                    "Note not found",
                    status = HttpStatusCode.NotFound
                )
            }

            val deletedCount = noteReferenceService.deleteReferencesFromNote(noteId)
            call.respond(mapOf("message" to "Deleted $deletedCount references"))
        }
    }
}

