package se.payerl.mobilenotes.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room Database för MobileNotes
 * Offline-first: All data lagras lokalt på enheten
 * 
 * Version 1: Initial schema med notes table
 */
@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NotesDatabase? = null

        /**
         * Singleton pattern för databas-instans
         * Thread-safe med double-checked locking
         */
        fun getDatabase(context: Context): NotesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "mobile_notes_database"
                )
                    // TODO: När vi implementerar online-funktionalitet senare,
                    // kan vi lägga till migration strategies här
                    .fallbackToDestructiveMigration()
                    .build()
                
                INSTANCE = instance
                instance
            }
        }

        /**
         * För testing: Återställ databas-instansen
         */
        fun resetInstance() {
            INSTANCE = null
        }
    }
}

