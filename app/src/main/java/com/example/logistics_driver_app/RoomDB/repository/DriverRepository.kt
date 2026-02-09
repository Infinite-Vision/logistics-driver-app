package com.example.logistics_driver_app.RoomDB.repository

import androidx.lifecycle.LiveData
import com.example.logistics_driver_app.RoomDB.dao.DriverDao
import com.example.logistics_driver_app.data.model.Driver

/**
 * DriverRepository - Functions to make DB operations for Driver entity.
 * Provides clean API for driver data operations to ViewModels.
 */
class DriverRepository(private val driverDao: DriverDao) {
    
    /**
     * Get all drivers as LiveData.
     * @return LiveData list of drivers
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
     * @return Driver or null
     */
    suspend fun getDriverByPhoneSync(phoneNumber: String): Driver? {
        return driverDao.getDriverByPhoneSync(phoneNumber)
    }
    
    /**
     * Insert new driver record.
     * @param driver Driver to insert
     * @return Row ID of inserted driver
     */
    suspend fun insertDriver(driver: Driver): Long {
        return driverDao.insertDriver(driver)
    }
    
    /**
     * Update existing driver record.
     * @param driver Driver with updated data
     */
    suspend fun updateDriver(driver: Driver) {
        driverDao.updateDriver(driver)
    }
    
    /**
     * Delete driver record.
     * @param driver Driver to delete
     */
    suspend fun deleteDriver(driver: Driver) {
        driverDao.deleteDriver(driver)
    }
    
    /**
     * Delete all driver records.
     */
    suspend fun deleteAllDrivers() {
        driverDao.deleteAllDrivers()
    }
}
