package com.example.logistics_driver_app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.logistics_driver_app.data.local.dao.AuthDao
import com.example.logistics_driver_app.data.local.dao.DriverDao
import com.example.logistics_driver_app.data.model.AuthSession
import com.example.logistics_driver_app.data.model.Driver

/**
 * Room Database class for the Logistics Driver App.
 * Contains all database entities and provides DAO instances.
 */
@Database(
    entities = [Driver::class, AuthSession::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun driverDao(): DriverDao
    abstract fun authDao(): AuthDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private const val DATABASE_NAME = "logistics_driver_db"
        
        /**
         * Get singleton instance of the database.
         * Thread-safe implementation using double-checked locking.
         * @param context Application context
         * @return AppDatabase instance
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
