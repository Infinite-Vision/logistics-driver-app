package com.example.logistics_driver_app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Driver entity representing driver details in the database.
 * Stores all information related to a driver in the logistics system.
 */
@Entity(tableName = "drivers")
data class Driver(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val phoneNumber: String,
    val name: String? = null,
    val email: String? = null,
    val vehicleNumber: String? = null,
    val vehicleType: String? = null,
    val licenseNumber: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    val profileImageUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
