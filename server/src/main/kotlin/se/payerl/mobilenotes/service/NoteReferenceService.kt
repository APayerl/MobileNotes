package se.payerl.mobilenotes.service

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import se.payerl.mobilenotes.database.NoteReferencesTable
import se.payerl.mobilenotes.database.dto.NoteDto
import se.payerl.mobilenotes.serialization.*
import kotlinx.serialization.json.*
import java.util.UUID

/**
 * Utökad NoteService med stöd för referenser mellan anteckningar
 */
class NoteReferenceService(private val noteService: NoteService) {

    /**
     * Skapar en referens från en anteckning till en annan
     */
    fun createReference(
        parentNoteId: String,
        referencedNoteId: String,
        userId: String,
        position: Int
    ): String? = transaction {
        // Verifiera att båda anteckningarna finns och tillhör användaren
        val parentNote = noteService.getNoteById(parentNoteId, userId) ?: return@transaction null
        val referencedNote = noteService.getNoteById(referencedNoteId, userId) ?: return@transaction null

        val refId = UUID.randomUUID().toString()

        // Skapa referens i references-tabellen
        NoteReferencesTable.insert {
            it[id] = refId
            it[NoteReferencesTable.parentNoteId] = parentNoteId
            it[NoteReferencesTable.referencedNoteId] = referencedNoteId
            it[NoteReferencesTable.position] = position
            it[createdAt] = System.currentTimeMillis()
        }

        // Lägg till referensen i anteckningens innehåll
        try {
            // Parse JSON content (handles both {"lines": [...]} and direct array format)
            val json = Json { ignoreUnknownKeys = true }
            val elements = json.parseToJsonElement(parentNote.content)

            // Extract the lines array
            val linesArray = when {
                elements is JsonArray -> elements
                elements is JsonObject && elements.containsKey("lines") -> {
                    elements["lines"] as? JsonArray ?: JsonArray(emptyList())
                }
                else -> JsonArray(emptyList())
            }

            // Build new array with added reference
            val newLines = buildList {
                addAll(linesArray)
                add(buildJsonObject {
                    put("id", refId)
                    put("referencedNoteId", referencedNoteId)
                    put("displayTitle", referencedNote.title)
                })
            }

            // Build updated content in {"lines": [...]} format
            val updatedContent = buildJsonObject {
                put("lines", JsonArray(newLines))
            }.toString()

            noteService.updateNote(parentNoteId, userId, parentNote.title, updatedContent)
            println("Successfully added reference to note content")
        } catch (e: Exception) {
            println("Error updating note content with reference: ${e.message}")
            e.printStackTrace()
            // Vi har fortfarande skapat referensen i tabellen
        }

        refId
    }

    /**
     * Hämtar alla referenser från en anteckning
     */
    fun getReferencesFromNote(noteId: String): List<String> = transaction {
        NoteReferencesTable.selectAll()
            .where { NoteReferencesTable.parentNoteId eq noteId }
            .orderBy(NoteReferencesTable.position to SortOrder.ASC)
            .map { it[NoteReferencesTable.referencedNoteId] }
    }

    /**
     * Hämtar alla anteckningar som refererar till en specifik anteckning
     */
    fun getReferencesToNote(noteId: String): List<String> = transaction {
        NoteReferencesTable.selectAll()
            .where { NoteReferencesTable.referencedNoteId eq noteId }
            .map { it[NoteReferencesTable.parentNoteId] }
    }

    /**
     * Tar bort en referens
     */
    fun deleteReference(referenceId: String): Boolean = transaction {
        val deleted = NoteReferencesTable.deleteWhere {
            NoteReferencesTable.id eq referenceId
        }
        deleted > 0
    }

    /**
     * Tar bort alla referenser från en anteckning
     */
    fun deleteReferencesFromNote(noteId: String): Int = transaction {
        NoteReferencesTable.deleteWhere {
            NoteReferencesTable.parentNoteId eq noteId
        }
    }

    /**
     * Expanderar referenser i en anteckning (hämtar refererat innehåll)
     * OBS: Detta expanderar bara en nivå - inte rekursivt
     */
    fun getNoteWithExpandedReferences(noteId: String, userId: String): NoteDto? = transaction {
        val note = noteService.getNoteById(noteId, userId) ?: return@transaction null

        try {
            // Parse JSON content (handles {"lines": [...]} format)
            val json = Json { ignoreUnknownKeys = true }
            val elements = json.parseToJsonElement(note.content)

            // Extract the lines array
            val linesArray = when {
                elements is JsonArray -> elements
                elements is JsonObject && elements.containsKey("lines") -> {
                    elements["lines"] as? JsonArray ?: JsonArray(emptyList())
                }
                else -> return@transaction note // Return as-is if can't parse
            }

            // Build expanded lines array
            val expandedLines = buildList {
                linesArray.forEach { lineElement ->
                    if (lineElement is JsonObject) {
                        // Check if this is a reference
                        if (lineElement.containsKey("referencedNoteId")) {
                            // This is a reference - expand it
                            val referencedNoteId = lineElement["referencedNoteId"]?.jsonPrimitive?.content
                            if (referencedNoteId != null) {
                                val referencedNote = noteService.getNoteById(referencedNoteId, userId)
                                if (referencedNote != null) {
                                    // Add a nested note object instead of just the reference
                                    add(buildJsonObject {
                                        put("id", referencedNote.id)
                                        put("title", referencedNote.title)

                                        // Parse referenced note's content
                                        val refContentJson = json.parseToJsonElement(referencedNote.content)
                                        val refLines = when {
                                            refContentJson is JsonArray -> refContentJson
                                            refContentJson is JsonObject && refContentJson.containsKey("lines") -> {
                                                refContentJson["lines"] as? JsonArray ?: JsonArray(emptyList())
                                            }
                                            else -> JsonArray(emptyList())
                                        }
                                        put("content", refLines)
                                        put("lastModified", referencedNote.lastModified)
                                    })
                                } else {
                                    // Referenced note not found, keep original reference
                                    add(lineElement)
                                }
                            } else {
                                add(lineElement)
                            }
                        } else {
                            // Regular line, keep as-is
                            add(lineElement)
                        }
                    } else {
                        add(lineElement)
                    }
                }
            }

            // Build updated content with expanded references
            val expandedContent = buildJsonObject {
                put("lines", JsonArray(expandedLines))
            }.toString()

            note.copy(content = expandedContent)
        } catch (e: Exception) {
            println("Error expanding references: ${e.message}")
            e.printStackTrace()
            // Return original note if expansion fails
            note
        }
    }

    /**
     * Kopierar en anteckning (värdekopiering)
     * Skapar en ny anteckning med samma innehåll som originalet
     */
    fun copyNote(sourceNoteId: String, userId: String, newTitle: String? = null): NoteDto? = transaction {
        val source = noteService.getNoteById(sourceNoteId, userId) ?: return@transaction null

        val title = newTitle ?: "${source.title} (kopia)"

        noteService.createNote(
            userId = userId,
            title = title,
            content = source.content  // Kopierar hela innehållet
        )
    }
}

