package se.payerl.mobilenotes.util

/**
 * JVM/Desktop implementation using println with formatted output.
 */
actual fun platformLogDebug(tag: String, message: String) {
    println("[DEBUG] [$tag] $message")
}

actual fun platformLogInfo(tag: String, message: String) {
    println("[INFO] [$tag] $message")
}

actual fun platformLogWarn(tag: String, message: String) {
    println("[WARN] [$tag] $message")
}

actual fun platformLogError(tag: String, message: String, throwable: Throwable?) {
    println("[ERROR] [$tag] $message")
    throwable?.let {
        println("Exception: ${it.message}")
        it.printStackTrace()
    }
}

