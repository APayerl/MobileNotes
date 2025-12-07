package se.payerl.mobilenotes.ui.models

/**
 * JSON-nycklar för note content parsing
 */
object JsonKeys {
    const val LINES = "lines"
    const val FREE_TEXT = "freeText"
    const val ID = "id"
    const val TEXT = "text"
    const val CHECKED = "checked"
    const val IS_REFERENCE = "isReference"
    const val REFERENCED_NOTE_ID = "referencedNoteId"
    const val DISPLAY_TITLE = "displayTitle"
    const val TITLE = "title"
    const val CONTENT = "content"
}

/**
 * Visningslägen för anteckningar
 */
enum class NoteViewMode {
    CHECKBOX,  // Varje rad är en checkbox
    FREETEXT   // Fritextfält
}

/**
 * Innehållstyper som kan visas
 */
sealed class ContentItem {
    data class Line(val id: String, val text: String) : ContentItem()
    data class Reference(
        val id: String,
        val referencedNoteId: String,
        val displayTitle: String
    ) : ContentItem()
    data class NestedNote(
        val id: String,
        val title: String,
        val content: List<ContentItem>
    ) : ContentItem()
    data class Unknown(val raw: String) : ContentItem()
}

/**
 * Checkbox item för checkbox-läget
 */
data class CheckboxItem(
    val id: String,
    val text: String,
    val checked: Boolean = false,
    val isReference: Boolean = false,
    val referencedNoteId: String? = null
)

