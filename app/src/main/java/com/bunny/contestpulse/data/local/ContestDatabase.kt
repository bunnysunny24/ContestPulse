package com.bunny.contestpulse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bunny.contestpulse.data.model.Contest

/**
 * Room database for storing contest information locally.
 */
@Database(
    entities = [Contest::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ContestDatabase : RoomDatabase() {

    abstract fun contestDao(): ContestDao

    companion object {
        @Volatile
        private var INSTANCE: ContestDatabase? = null

        fun getInstance(context: Context): ContestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContestDatabase::class.java,
                    "contestpulse_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
