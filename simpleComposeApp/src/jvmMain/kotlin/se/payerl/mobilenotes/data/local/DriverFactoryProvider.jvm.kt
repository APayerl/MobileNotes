package se.payerl.mobilenotes.data.local

/**
 * JVM (Desktop) implementation of the driver factory provider.
 * The factory itself handles RepositoryProvider initialization in createDriver().
 */
actual fun createDatabaseDriverFactory(): DatabaseDriverFactory {
    return JvmDatabaseDriverFactory()
}
