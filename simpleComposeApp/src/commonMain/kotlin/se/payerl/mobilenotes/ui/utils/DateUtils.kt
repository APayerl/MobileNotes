package se.payerl.mobilenotes.ui.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Formatera tidsstämpel till läsbar text
 * Visar relativ tid (t.ex. "2 min sedan") eller datum beroende på ålder
 */
@OptIn(ExperimentalTime::class)
fun formatDate(timestamp: Long): String {
    val now = Clock.System.now()
    val then = Instant.fromEpochMilliseconds(timestamp)
    val diffMillis = now.toEpochMilliseconds() - timestamp
    val diffSeconds = diffMillis / 1000
    val diffMinutes = diffSeconds / 60
    val diffHours = diffMinutes / 60
    val diffDays = diffHours / 24

    return when {
        // Mindre än en minut
        diffSeconds < 60 -> "Nu"
        
        // Mindre än en timme
        diffMinutes < 60 -> {
            val minutes = diffMinutes.toInt()
            if (minutes == 1) "1 min sedan" else "$minutes min sedan"
        }
        
        // Mindre än 24 timmar
        diffHours < 24 -> {
            val hours = diffHours.toInt()
            if (hours == 1) "1 timme sedan" else "$hours timmar sedan"
        }
        
        // Mindre än 7 dagar
        diffDays < 7 -> {
            val days = diffDays.toInt()
            if (days == 1) "Igår" else "$days dagar sedan"
        }
        
        // Äldre än 7 dagar - visa datum
        else -> {
            val systemTimeZone = TimeZone.currentSystemDefault()
            val dateTime = then.toLocalDateTime(systemTimeZone)
            val currentYear = now.toLocalDateTime(systemTimeZone).year

            val month = when (dateTime.month.ordinal) {
                1 -> "jan"
                2 -> "feb"
                3 -> "mar"
                4 -> "apr"
                5 -> "maj"
                6 -> "jun"
                7 -> "jul"
                8 -> "aug"
                9 -> "sep"
                10 -> "okt"
                11 -> "nov"
                12 -> "dec"
                else -> ""
            }
            
            // Visa år bara om det inte är innevarande år
            if (dateTime.year == currentYear) {
                "${dateTime.day} $month"
            } else {
                "${dateTime.day} $month ${dateTime.year}"
            }
        }
    }
}

