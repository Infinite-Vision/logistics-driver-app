package com.example.logistics_driver_app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AuthSession entity to track user authentication sessions.
 * Stores login state and session information for the driver.
 */
@Entity(tableName = "auth_sessions")
data class AuthSession(
    @PrimaryKey
    val phoneNumber: String,
    val otp: String? = null,
    val isVerified: Boolean = false,
    val sessionToken: String? = null,
    val languageCode: String = "en",
    val loginTimestamp: Long = System.currentTimeMillis(),
    val expiryTimestamp: Long = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L) // 30 days
)
