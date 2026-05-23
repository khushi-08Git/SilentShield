package com.example.silentshield.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AlertEntity::class], version = 1, exportSchema = false)
abstract class AlertDatabase : RoomDatabase() {

    abstract fun alertDao(): AlertDao

    companion object {
        @Volatile private var INSTANCE: AlertDatabase? = null

        fun getInstance(context: Context): AlertDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AlertDatabase::class.java,
                    "alert_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}