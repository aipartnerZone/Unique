package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CommandHistoryEntity::class,
        ChatMessageEntity::class,
        UserMemoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MunnaDatabase : RoomDatabase() {
    abstract fun munnaDao(): MunnaDao

    companion object {
        @Volatile
        private var INSTANCE: MunnaDatabase? = null

        fun getInstance(context: Context): MunnaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MunnaDatabase::class.java,
                    "munna_ai_database.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
