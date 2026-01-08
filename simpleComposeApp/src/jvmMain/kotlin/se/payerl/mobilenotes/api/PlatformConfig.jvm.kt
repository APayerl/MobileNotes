package se.payerl.mobilenotes.api

/**
 * JVM (Desktop) implementation av API base URL.
 * Använder standard localhost eftersom desktop körs på samma maskin som servern.
 */
actual fun getApiBaseUrl(): String {
    return "http://localhost:8080"
}

