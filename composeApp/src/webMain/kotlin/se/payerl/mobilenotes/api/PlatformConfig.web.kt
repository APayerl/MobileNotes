package se.payerl.mobilenotes.api

/**
 * Web implementation av API base URL.
 * Använder standard localhost för webbutveckling.
 */
actual fun getApiBaseUrl(): String {
    return "http://localhost:8080"
}

