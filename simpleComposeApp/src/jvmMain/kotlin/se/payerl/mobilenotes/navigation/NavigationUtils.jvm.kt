package se.payerl.mobilenotes.navigation

/**
 * JVM/Desktop implementation - extracts String from Map.
 */
@Suppress("UNCHECKED_CAST")
actual fun getNavigationArgument(arguments: Any?, key: String): String? {
    return (arguments as? Map<String, Any?>)?.get(key) as? String
}

