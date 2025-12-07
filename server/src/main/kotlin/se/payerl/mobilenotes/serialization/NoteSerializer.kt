package se.payerl.mobilenotes.serialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Serializable variants av modellerna för JSON-konvertering
 */
@Serializable
sealed class SerializableNoteElement {
    abstract val id: String
}

@Serializable
data class SerializableNoteLine(
    override val id: String,
    val text: String
) : SerializableNoteElement()

@Serializable
data class SerializableNoteReference(
    override val id: String,
    val referencedNoteId: String,
    val displayTitle: String = ""
) : SerializableNoteElement()

@Serializable
data class SerializableNote(
    override val id: String,
    val title: String,
    val content: List<SerializableNoteElement>,
    val lastModified: Long
) : SerializableNoteElement()

/**
 * JSON-konfiguration med stöd för polymorfa typer
 */
val noteJson = Json {
    serializersModule = SerializersModule {
        polymorphic(SerializableNoteElement::class) {
            subclass(SerializableNoteLine::class)
            subclass(SerializableNoteReference::class)
            subclass(SerializableNote::class)
        }
    }
    prettyPrint = true
    ignoreUnknownKeys = true
}

/**
 * Hjälpfunktioner för serialisering
 */
object NoteSerializer {

    fun serializeElements(elements: List<SerializableNoteElement>): String {
        return noteJson.encodeToString(elements)
    }

    fun deserializeElements(json: String): List<SerializableNoteElement> {
        return noteJson.decodeFromString(json)
    }
}

