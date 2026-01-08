package se.payerl.mobilenotes.util

/**
 * JS implementation of platform logging functions using browser console.
 */
actual fun platformLogDebug(tag: String, message: String) {
    console.log("[DEBUG] [$tag] $message")
}

actual fun platformLogInfo(tag: String, message: String) {
    console.info("[INFO] [$tag] $message")
}

actual fun platformLogWarn(tag: String, message: String) {
    console.warn("[WARN] [$tag] $message")
}

actual fun platformLogError(tag: String, message: String, throwable: Throwable?) {
    console.error("[ERROR] [$tag] $message")
    throwable?.let {
        console.error("Exception: ${it.message}")
        console.error(it.stackTraceToString())
    }
}

