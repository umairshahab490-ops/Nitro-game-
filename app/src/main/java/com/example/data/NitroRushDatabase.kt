package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RunRecordEntity::class, CachedRouteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NitroRushDatabase : RoomDatabase() {
    abstract fun nitroRushDao(): NitroRushDao

    companion object {
        @Volatile
        private var instance: NitroRushDatabase? = null

        fun getInstance(context: Context): NitroRushDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NitroRushDatabase::class.java,
                    "nitro_rush.db"
                ).build().also { instance = it }
            }
        }
    }
}
