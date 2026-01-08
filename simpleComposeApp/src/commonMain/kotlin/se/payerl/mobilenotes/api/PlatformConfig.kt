package se.payerl.mobilenotes.api

/**
 * Plattformsspecifik konfiguration för API-anslutning.
 * Varje plattform kan implementera sin egen baseUrl för att hantera
 * skillnader i nätverkskonfiguration (t.ex. Android emulator).
 */
expect fun getApiBaseUrl(): String

