package se.payerl.mobilenotes.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConfig {
    fun init() {
        // Connect to H2 in-memory database
        Database.connect(
            url = "jdbc:h2:mem:notes;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )

        // Create tables
        transaction {
            SchemaUtils.create(NotesTable, UsersTable, NoteReferencesTable)

            // Lägg till testdata (kan tas bort i produktion)
            initTestData()
        }
    }

    private fun initTestData() {
        // Skapa en testanvändare
        UsersTable.insert {
            it[id] = "user1"
            it[username] = "Test User"
            it[email] = "test@example.com"
        }

        // Skapa några testanteckningar
        NotesTable.insert {
            it[id] = "note1"
            it[userId] = "user1"
            it[title] = "Min första anteckning"
            it[content] = """{"lines": [{"text": "Detta är min första anteckning"}]}"""
            it[lastModified] = System.currentTimeMillis()
        }

        NotesTable.insert {
            it[id] = "note2"
            it[userId] = "user1"
            it[title] = "Shoppinglista"
            it[content] = """{"lines": [{"text": "Mjölk"}, {"text": "Bröd"}, {"text": "Smör"}]}"""
            it[lastModified] = System.currentTimeMillis() - 86400000 // 1 dag sedan
        }

        NotesTable.insert {
            it[id] = "note3"
            it[userId] = "user1"
            it[title] = "Att-göra"
            it[content] = """{"lines": [{"text": "Slutföra projektet"}, {"text": "Ringa läkaren"}]}"""
            it[lastModified] = System.currentTimeMillis() - 172800000 // 2 dagar sedan
        }
    }
}

