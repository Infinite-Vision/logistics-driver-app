package com.example.logistics_driver_app.RoomDB.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.logistics_driver_app.data.model.Driver

/**
 * DriverDao - Data Access Object for Driver entity.
 * Provides all CRUD operation calls for driver data in Room database.
 */
@Dao
interface DriverDao {
    
    /**
     * Insert a new driver record.
     * @param driver Driver entity to insert
     * @return Row ID of inserted driver
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: Driver): Long
    
    /**
     * Update existing driver record.
     * @param driver Driver entity with updated data
     */
    @Update
    suspend fun updateDriver(driver: Driver)
    
    /**
     * Delete driver record.
     * @param driver Driver entity to delete
     */
    @Delete
    suspend fun deleteDriver(driver: Driver)
    
    /**
     * Get driver by phone number (LiveData - reactive).
     * @param phoneNumber Phone number to search
     * @return LiveData of Driver or null
     */
    @Query("SELECT * FROM drivers WHERE phoneNumber = :phoneNumber LIMIT 1")
    fun getDriverByPhone(phoneNumber: String): LiveData<Driver?>
    
    /**
     * Get driver by phone number (suspend - one-time query).
     * @param phoneNumber Phone number to search
     * @return Driver or null
     */
    @Query("SELECT * FROM drivers WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getDriverByPhoneSync(phoneNumber: String): Driver?
    
    /**
     * Get all drivers (LiveData - reactive).
     * @return LiveData list of all drivers
     */
    @Query("SELECT * FROM drivers ORDER BY createdAt DESC")
    fun getAllDrivers(): LiveData<List<Driver>>
    
    /**
     * Get all active drivers.
     * @return LiveData list of active drivers
     */
    @Query("SELECT * FROM drivers WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveDrivers(): LiveData<List<Driver>>
    
    /**
     * Delete all driver records.
     */
    @Query("DELETE FROM drivers")
    suspend fun deleteAllDrivers()
}
