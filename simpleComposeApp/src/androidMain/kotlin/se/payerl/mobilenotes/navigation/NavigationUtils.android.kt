package se.payerl.mobilenotes.navigation

import android.os.Bundle

/**
 * Android implementation - extracts String from Bundle.
 */
actual fun getNavigationArgument(arguments: Any?, key: String): String? {
    return (arguments as? Bundle)?.getString(key)
}
