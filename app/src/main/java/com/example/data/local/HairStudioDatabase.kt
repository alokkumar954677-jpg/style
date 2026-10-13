package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SavedStyleEntity::class, GenerationHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HairStudioDatabase : RoomDatabase() {
    abstract fun hairStudioDao(): HairStudioDao

    companion object {
        @Volatile
        private var INSTANCE: HairStudioDatabase? = null

        fun getDatabase(context: Context): HairStudioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HairStudioDatabase::class.java,
                    "hair_studio_db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
