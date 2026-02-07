package com.example.logistics_driver_app.data.repository

import androidx.lifecycle.LiveData
import com.example.logistics_driver_app.data.local.dao.DriverDao
import com.example.logistics_driver_app.data.model.Driver

/**
 * Repository class for Driver data operations.
 * Provides a clean API for data access to the ViewModel layer.
 * Handles data operations from local database and (future) remote API.
 */
class DriverRepository(private val driverDao: DriverDao) {
    
    /**
     * Get all drivers as LiveData.
     * @return LiveData list of all drivers
     */
    fun getAllDrivers(): LiveData<List<Driver>> {
        return driverDao.getAllDrivers()
    }
    
    /**
     * Get active drivers as LiveData.
     * @return LiveData list of active drivers
     */
    fun getActiveDrivers(): LiveData<List<Driver>> {
        return driverDao.getActiveDrivers()
    }
    
    /**
     * Get driver by phone number as LiveData.
     * @param phoneNumber Phone number to search
     * @return LiveData of Driver or null
     */
    fun getDriverByPhone(phoneNumber: String): LiveData<Driver?> {
        return driverDao.getDriverByPhone(phoneNumber)
    }
    
    /**
     * Get driver by phone number (one-time query).
     * @param phoneNumber Phone number to search
     * @return Driver object or null
     */
    suspend fun getDriverByPhoneSync(phoneNumber: String): Driver? {
        return driverDao.getDriverByPhoneSync(phoneNumber)
    }
    
    /**
     * Insert a new driver.
     * @param driver Driver object to insert
     * @return Row ID of inserted driver
     */
    suspend fun insertDriver(driver: Driver): Long {
        return driverDao.insertDriver(driver)
    }
    
    /**
     * Update an existing driver.
     * @param driver Driver object with updated data
     */
    suspend fun updateDriver(driver: Driver) {
        driverDao.updateDriver(driver)
    }
    
    /**
     * Delete a driver.
     * @param driver Driver object to delete
     */
    suspend fun deleteDriver(driver: Driver) {
        driverDao.deleteDriver(driver)
    }
    
    /**
     * Delete all drivers from database.
     */
    suspend fun deleteAllDrivers() {
        driverDao.deleteAllDrivers()
    }
}
