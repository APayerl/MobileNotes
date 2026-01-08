package se.payerl.mobilenotes.api

/**
 * JS (Web) implementation of API base URL.
 *
 * NOTE: Frontend (JS app) and Backend (Ktor server) run on DIFFERENT PORTS during development!
 * - Frontend: typically runs on webpack-dev-server (port 8081, 3000, etc.)
 * - Backend: runs on port 8080 (as configured in server/main.kt)
 *
 * Therefore we CANNOT use window.location.origin - it would point to the frontend server,
 * not the backend API!
 *
 * For production: Configure this based on your deployment setup (e.g., use environment variable
 * or detect hostname to switch between dev/prod URLs).
 */
actual fun getApiBaseUrl(): String {
    // Point to the backend server explicitly
    // This matches the JVM configuration (http://localhost:8080)
    return "http://localhost:8080"

    // Alternative for production: Use window.location.hostname to detect environment
    // val hostname = js("window.location.hostname") as String
    // return if (hostname == "localhost") {
    //     "http://localhost:8080"  // Development
    // } else {
    //     "https://api.yourdomain.com"  // Production
    // }
}

