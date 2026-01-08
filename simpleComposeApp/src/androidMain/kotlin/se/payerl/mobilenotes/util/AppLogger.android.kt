package se.payerl.mobilenotes.util

import android.util.Log

/**
 * Android implementation using android.util.Log for proper filterable log levels.
 * These will show up in Logcat with proper priority levels (D, I, W, E).
 */
actual fun platformLogDebug(tag: String, message: String) {
    Log.d(tag, message)
}

actual fun platformLogInfo(tag: String, message: String) {
    Log.i(tag, message)
}

actual fun platformLogWarn(tag: String, message: String) {
    Log.w(tag, message)
}

actual fun platformLogError(tag: String, message: String, throwable: Throwable?) {
    if (throwable != null) {
        Log.e(tag, message, throwable)
    } else {
        Log.e(tag, message)
    }
}

