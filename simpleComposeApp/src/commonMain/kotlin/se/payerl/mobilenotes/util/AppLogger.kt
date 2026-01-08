package se.payerl.mobilenotes.util

/**
 * Platform-specific logging functions.
 * On Android, these will use android.util.Log for proper log levels.
 * On other platforms, these will use println.
 */
expect fun platformLogDebug(tag: String, message: String)
expect fun platformLogInfo(tag: String, message: String)
expect fun platformLogWarn(tag: String, message: String)
expect fun platformLogError(tag: String, message: String, throwable: Throwable?)

/**
 * Simple logger for Kotlin Multiplatform with support for different log levels.
 * Works across all platforms (Android, iOS, Desktop, Web).
 *
 * On Android: Uses android.util.Log for proper filterable log levels
 * On other platforms: Uses println with formatted output
 */
object AppLogger {

    enum class Level {
        DEBUG, INFO, WARN, ERROR
    }

    // Set minimum log level (default is DEBUG to show everything)
    var minLevel: Level = Level.DEBUG

    private fun shouldLog(level: Level): Boolean {
        return level.ordinal >= minLevel.ordinal
    }

    fun debug(tag: String, message: String) {
        if (shouldLog(Level.DEBUG)) {
            platformLogDebug(tag, message)
        }
    }

    fun info(tag: String, message: String) {
        if (shouldLog(Level.INFO)) {
            platformLogInfo(tag, message)
        }
    }

    fun warn(tag: String, message: String) {
        if (shouldLog(Level.WARN)) {
            platformLogWarn(tag, message)
        }
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        if (shouldLog(Level.ERROR)) {
            platformLogError(tag, message, throwable)
        }
    }
}

