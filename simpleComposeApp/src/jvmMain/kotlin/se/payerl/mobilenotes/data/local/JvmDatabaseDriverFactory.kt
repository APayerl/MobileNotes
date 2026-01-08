package se.payerl.mobilenotes.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import se.payerl.mobilenotes.db.MobileNotesDatabase
import java.io.File

/**
 * JVM (Desktop) specific implementation of DatabaseDriverFactory.
 * Uses JdbcSqliteDriver for persistent storage.
 */
class JvmDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        // Store database in user's home directory
        val dbPath = System.getProperty("user.home") + File.separator + ".mobilenotes" + File.separator + "mobilenotes.db"
        val dbFile = File(dbPath)

        // Create directory if it doesn't exist
        dbFile.parentFile?.mkdirs()

        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")

        // Create tables if they don't exist
        MobileNotesDatabase.Schema.create(driver)

        return driver
    }
}

