package com.example.logistics_driver_app.modules.loginModule.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * DriverEntity - Room database entity for driver table.
 * Represents driver information stored in local database.
 */
@Entity(tableName = "drivers")
data class DriverEntity(
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
