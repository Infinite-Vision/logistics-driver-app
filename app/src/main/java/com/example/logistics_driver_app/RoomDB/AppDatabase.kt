package com.example.logistics_driver_app.RoomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.logistics_driver_app.RoomDB.dao.AuthDao
import com.example.logistics_driver_app.RoomDB.dao.DriverDao
import com.example.logistics_driver_app.data.model.AuthSession
import com.example.logistics_driver_app.data.model.Driver

/**
 * AppDatabase - Room Database configuration.
 * Main database class that provides DAO instances for DB operations.
 */
@Database(
    entities = [Driver::class, AuthSession::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Get DriverDao for driver table operations.
     * @return DriverDao instance
     */
    abstract fun driverDao(): DriverDao

    /**
     * Get AuthDao for auth_sessions table operations.
     * @return AuthDao instance
     */
    abstract fun authDao(): AuthDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DATABASE_NAME = "logistics_driver_db"

        /**
         * Get singleton database instance.
         * Thread-safe with double-checked locking.
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
