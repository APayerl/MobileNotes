package se.payerl.mobilenotes.database.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    val id: String,
    val userId: String,
    val title: String,
    val content: String, // JSON string representation
    val lastModified: Long
)

