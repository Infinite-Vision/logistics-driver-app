package com.example.logistics_driver_app.modules.loginModule.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AuthSessionEntity - Room database entity for auth_sessions table.
 * Stores authentication session information in local database.
 */
@Entity(tableName = "auth_sessions")
data class AuthSessionEntity(
    @PrimaryKey
    val phoneNumber: String,
    val otp: String? = null,
    val isVerified: Boolean = false,
    val sessionToken: String? = null,
    val languageCode: String = "en",
    val loginTimestamp: Long = System.currentTimeMillis(),
    val expiryTimestamp: Long = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L)
)
