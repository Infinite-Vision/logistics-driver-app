package com.example.logistics_driver_app.Common

import java.text.SimpleDateFormat
import java.util.*

/**
 * CommonFunctions - Common utility functions used across the application.
 * Provides reusable helper methods for various operations.
 */
object CommonFunctions {
    
    /**
     * Generate random 4-digit OTP for testing.
     * @return Random OTP string
     */
    fun generateOTP(): String {
        return (1000..9999).random().toString()
    }
    
    /**
     * Format timestamp to readable date string.
     * @param timestamp Timestamp in milliseconds
     * @param pattern Date format pattern (default: dd MMM yyyy)
     * @return Formatted date string
     */
    fun formatDate(timestamp: Long, pattern: String = "dd MMM yyyy"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    /**
     * Format timestamp to date and time string.
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date-time string
     */
    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    /**
     * Get time remaining in MM:SS format.
     * @param seconds Total seconds
     * @return Formatted time string
     */
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
    }
    
    /**
     * Capitalize first letter of each word.
     * @param text Text to capitalize
     * @return Capitalized text
     */
    fun capitalizeWords(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) 
                else it.toString() 
            }
        }
    }
    
    /**
     * Get current timestamp in milliseconds.
     * @return Current timestamp
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
    
    /**
     * Check if timestamp is expired.
     * @param expiryTimestamp Expiry timestamp to check
     * @return True if expired
     */
    fun isExpired(expiryTimestamp: Long): Boolean {
        return System.currentTimeMillis() > expiryTimestamp
    }
    
    /**
     * Get time difference in days.
     * @param fromTimestamp Start timestamp
     * @param toTimestamp End timestamp
     * @return Number of days difference
     */
    fun getDaysDifference(fromTimestamp: Long, toTimestamp: Long): Long {
        val diff = toTimestamp - fromTimestamp
        return diff / (1000 * 60 * 60 * 24)
    }
    
    /**
     * Mask phone number for display (show last 4 digits).
     * @param phoneNumber Phone number to mask
     * @return Masked phone number (e.g., ******1234)
     */
    fun maskPhoneNumber(phoneNumber: String): String {
        return if (phoneNumber.length >= 4) {
            "*".repeat(phoneNumber.length - 4) + phoneNumber.takeLast(4)
        } else {
            phoneNumber
        }
    }
    
    /**
     * Generate unique session token.
     * @return Unique token string
     */
    fun generateSessionToken(): String {
        return "token_${UUID.randomUUID()}_${System.currentTimeMillis()}"
    }
}
