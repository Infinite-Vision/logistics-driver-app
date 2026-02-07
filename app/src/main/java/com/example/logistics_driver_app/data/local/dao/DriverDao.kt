package com.example.logistics_driver_app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.logistics_driver_app.data.model.Driver

/**
 * Data Access Object for Driver entity.
 * Provides methods for CRUD operations on driver data.
 */
@Dao
interface DriverDao {
    
    /**
     * Insert a new driver into the database.
     * @param driver Driver object to insert
     * @return Row ID of the inserted driver
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: Driver): Long
    
    /**
     * Update an existing driver's information.
     * @param driver Driver object with updated information
     */
    @Update
    suspend fun updateDriver(driver: Driver)
    
    /**
     * Delete a driver from the database.
     * @param driver Driver object to delete
     */
    @Delete
    suspend fun deleteDriver(driver: Driver)
    
    /**
     * Get a driver by phone number.
     * @param phoneNumber Phone number of the driver
     * @return LiveData of Driver object
     */
    @Query("SELECT * FROM drivers WHERE phoneNumber = :phoneNumber LIMIT 1")
    fun getDriverByPhone(phoneNumber: String): LiveData<Driver?>
    
    /**
     * Get a driver by phone number (suspend function for one-time query).
     * @param phoneNumber Phone number of the driver
     * @return Driver object or null
     */
    @Query("SELECT * FROM drivers WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getDriverByPhoneSync(phoneNumber: String): Driver?
    
    /**
     * Get all drivers from the database.
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
     * Delete all drivers from the database.
     */
    @Query("DELETE FROM drivers")
    suspend fun deleteAllDrivers()
}
