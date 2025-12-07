package se.payerl.mobilenotes.ui.parser

import kotlinx.serialization.json.*
import se.payerl.mobilenotes.api.NoteDto
import se.payerl.mobilenotes.ui.models.CheckboxItem
import se.payerl.mobilenotes.ui.models.ContentItem
import se.payerl.mobilenotes.ui.models.JsonKeys

/**
 * Exception som kastas när parsing av note content misslyckas
 */
class NoteParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Interface för parsing av note content
 */
interface NoteContentParser {
    fun parseToCheckboxItems(jsonContent: String): Result<List<CheckboxItem>>
    fun parseToContentItems(jsonContent: String): Result<List<ContentItem>>
    fun convertToFreeText(jsonContent: String, availableNotes: List<NoteDto>): Result<String>
    fun serializeCheckboxItems(items: List<CheckboxItem>): String
}

/**
 * Implementation av NoteContentParser som använder JSON
 */
class JsonNoteContentParser : NoteContentParser {
    private val json = Json { ignoreUnknownKeys = true }

    override fun parseToCheckboxItems(jsonContent: String): Result<List<CheckboxItem>> {
        return runCatching {
            val elements = json.parseToJsonElement(jsonContent)

            // Kolla om det är fritext först
            if (elements is JsonObject && elements.containsKey(JsonKeys.FREE_TEXT)) {
                val freeText = elements[JsonKeys.FREE_TEXT]?.jsonPrimitive?.content
                    ?: throw NoteParseException("FREE_TEXT field is null")
                return Result.success(convertFreeTextToCheckboxItems(freeText))
            }

            val arrayToProcess = extractLinesArray(elements)
            parseCheckboxItemsFromArray(arrayToProcess)
        }.recoverCatching { exception ->
            throw NoteParseException(
                "Failed to parse checkbox items: ${exception.message}",
                exception
            )
        }
    }

    override fun parseToContentItems(jsonContent: String): Result<List<ContentItem>> {
        return runCatching {
            val elements = json.parseToJsonElement(jsonContent)
            val arrayToProcess = when {
                elements is JsonArray -> elements
                elements is JsonObject && elements.containsKey(JsonKeys.LINES) -> {
                    elements[JsonKeys.LINES] as? JsonArray
                        ?: throw NoteParseException("'${JsonKeys.LINES}' is not an array")
                }
                else -> throw NoteParseException("Unexpected format: ${jsonContent.take(100)}")
            }

            arrayToProcess.mapNotNull { element ->
                parseContentElement(element)
            }
        }.recoverCatching { exception ->
            throw NoteParseException(
                "Failed to parse content items: ${exception.message}",
                exception
            )
        }
    }

    override fun convertToFreeText(jsonContent: String, availableNotes: List<NoteDto>): Result<String> {
        return runCatching {
            val elements = json.parseToJsonElement(jsonContent)

            // Om det redan är fritext, returnera det
            if (elements is JsonObject && elements.containsKey(JsonKeys.FREE_TEXT)) {
                return Result.success(
                    elements[JsonKeys.FREE_TEXT]?.jsonPrimitive?.content ?: ""
                )
            }

            val arrayToProcess = extractLinesArray(elements)

            // Konvertera varje rad till text
            arrayToProcess.mapNotNull { element ->
                if (element !is JsonObject) return@mapNotNull null

                val text = element[JsonKeys.TEXT]?.jsonPrimitive?.content ?: ""
                val isReference = element[JsonKeys.IS_REFERENCE]?.jsonPrimitive?.booleanOrNull ?: false
                val referencedNoteId = element[JsonKeys.REFERENCED_NOTE_ID]?.jsonPrimitive?.content

                if (isReference && referencedNoteId != null) {
                    expandReference(referencedNoteId, availableNotes)
                } else {
                    text
                }
            }.joinToString("\n")
        }.recoverCatching { exception ->
            throw NoteParseException(
                "Failed to convert to free text: ${exception.message}",
                exception
            )
        }
    }

    override fun serializeCheckboxItems(items: List<CheckboxItem>): String {
        val lines = items.map { item ->
            buildJsonObject {
                put(JsonKeys.ID, item.id)
                put(JsonKeys.TEXT, item.text)
                put(JsonKeys.CHECKED, item.checked)
                if (item.isReference) {
                    put(JsonKeys.IS_REFERENCE, true)
                    item.referencedNoteId?.let { put(JsonKeys.REFERENCED_NOTE_ID, it) }
                }
            }
        }

        return buildJsonObject {
            put(JsonKeys.LINES, JsonArray(lines))
        }.toString()
    }

    // Private helper methods

    private fun extractLinesArray(elements: JsonElement): JsonArray {
        return when {
            elements is JsonArray -> elements
            elements is JsonObject && elements.containsKey(JsonKeys.LINES) -> {
                elements[JsonKeys.LINES] as? JsonArray ?: JsonArray(emptyList())
            }
            else -> JsonArray(emptyList())
        }
    }

    private fun convertFreeTextToCheckboxItems(freeText: String): List<CheckboxItem> {
        return freeText.lines().mapIndexed { index, line ->
            CheckboxItem(
                id = "line_$index",
                text = line,
                checked = false
            )
        }
    }

    private fun parseCheckboxItemsFromArray(array: JsonArray): List<CheckboxItem> {
        return array.mapNotNull { element ->
            if (element !is JsonObject) return@mapNotNull null

            val id = element[JsonKeys.ID]?.jsonPrimitive?.content
                ?: throw NoteParseException("Missing '${JsonKeys.ID}' field")
            val text = element[JsonKeys.TEXT]?.jsonPrimitive?.content ?: ""
            val checked = element[JsonKeys.CHECKED]?.jsonPrimitive?.booleanOrNull ?: false
            val isReference = element[JsonKeys.IS_REFERENCE]?.jsonPrimitive?.booleanOrNull ?: false
            val referencedNoteId = element[JsonKeys.REFERENCED_NOTE_ID]?.jsonPrimitive?.content

            CheckboxItem(
                id = id,
                text = text,
                checked = checked,
                isReference = isReference,
                referencedNoteId = referencedNoteId
            )
        }
    }

    private fun parseContentElement(element: JsonElement): ContentItem? {
        if (element !is JsonObject) return null

        return try {
            when {
                element.containsKey(JsonKeys.TEXT) -> {
                    ContentItem.Line(
                        id = element[JsonKeys.ID]?.jsonPrimitive?.content ?: "",
                        text = element[JsonKeys.TEXT]?.jsonPrimitive?.content ?: ""
                    )
                }
                element.containsKey(JsonKeys.REFERENCED_NOTE_ID) -> {
                    ContentItem.Reference(
                        id = element[JsonKeys.ID]?.jsonPrimitive?.content ?: "",
                        referencedNoteId = element[JsonKeys.REFERENCED_NOTE_ID]?.jsonPrimitive?.content ?: "",
                        displayTitle = element[JsonKeys.DISPLAY_TITLE]?.jsonPrimitive?.content ?: ""
                    )
                }
                element.containsKey(JsonKeys.TITLE) && element.containsKey(JsonKeys.CONTENT) -> {
                    val nestedContent = element[JsonKeys.CONTENT]?.jsonArray?.mapNotNull {
                        parseContentElement(it)
                    } ?: emptyList()

                    ContentItem.NestedNote(
                        id = element[JsonKeys.ID]?.jsonPrimitive?.content ?: "",
                        title = element[JsonKeys.TITLE]?.jsonPrimitive?.content ?: "",
                        content = nestedContent
                    )
                }
                else -> ContentItem.Unknown(element.toString())
            }
        } catch (e: Exception) {
            ContentItem.Unknown("Parse error: ${e.message}")
        }
    }

    private fun expandReference(referencedNoteId: String, availableNotes: List<NoteDto>): String {
        val referencedNote = availableNotes.find { it.id == referencedNoteId }
        return if (referencedNote != null) {
            // Rekursivt konvertera innehållet
            convertToFreeText(referencedNote.content, availableNotes)
                .getOrElse { "" }
        } else {
            "\${list.reference_not_found}"
        }
    }
}

