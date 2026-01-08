package se.payerl.mobilenotes.navigation

/**
 * Platform-specific utility to extract String arguments from navigation Bundle/Map.
 *
 * @param arguments The platform-specific arguments container (Bundle on Android, Map on other platforms)
 * @param key The argument key to extract
 * @return The String value or null if not found
 */
expect fun getNavigationArgument(arguments: Any?, key: String): String?
