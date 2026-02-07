package com.example.logistics_driver_app.utils

import android.util.Patterns
import java.util.regex.Pattern

/**
 * Validation utility class for various input checks.
 * Provides methods to validate phone numbers, OTP, names, etc.
 */
object ValidationUtils {
    
    private const val INDIA_PHONE_LENGTH = 10
    private const val OTP_LENGTH = 4
    
    /**
     * Validate Indian phone number.
     * @param phoneNumber Phone number to validate
     * @return True if valid (10 digits)
     */
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.trim().length == INDIA_PHONE_LENGTH &&
                phoneNumber.all { it.isDigit() }
    }
    
    /**
     * Validate OTP code.
     * @param otp OTP to validate
     * @return True if valid (4 digits)
     */
    fun isValidOTP(otp: String): Boolean {
        return otp.trim().length == OTP_LENGTH &&
                otp.all { it.isDigit() }
    }
    
    /**
     * Validate email address.
     * @param email Email to validate
     * @return True if valid email format
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Validate name (alphabets and spaces only).
     * @param name Name to validate
     * @return True if valid name
     */
    fun isValidName(name: String): Boolean {
        val namePattern = Pattern.compile("^[a-zA-Z\\s]{2,}$")
        return name.isNotEmpty() && namePattern.matcher(name.trim()).matches()
    }
    
    /**
     * Validate vehicle number (standard Indian format).
     * @param vehicleNumber Vehicle number to validate
     * @return True if valid format
     */
    fun isValidVehicleNumber(vehicleNumber: String): Boolean {
        // Indian vehicle format: XX00XX0000 or XX-00-XX-0000
        val pattern = Pattern.compile("^[A-Z]{2}[-]?[0-9]{2}[-]?[A-Z]{1,2}[-]?[0-9]{4}$")
        return vehicleNumber.isNotEmpty() && pattern.matcher(vehicleNumber.trim().uppercase()).matches()
    }
    
    /**
     * Validate license number.
     * @param licenseNumber License number to validate
     * @return True if not empty and reasonable length
     */
    fun isValidLicenseNumber(licenseNumber: String): Boolean {
        // Basic validation - at least 5 characters
        return licenseNumber.trim().length >= 5
    }
    
    /**
     * Validate pincode (Indian 6-digit pincode).
     * @param pincode Pincode to validate
     * @return True if 6 digits
     */
    fun isValidPincode(pincode: String): Boolean {
        return pincode.trim().length == 6 && pincode.all { it.isDigit() }
    }
    
    /**
     * Format phone number to include country code.
     * @param phoneNumber Phone number without country code
     * @return Formatted phone number with +91
     */
    fun formatPhoneNumber(phoneNumber: String): String {
        val cleanNumber = phoneNumber.trim()
        return if (cleanNumber.startsWith("+91")) {
            cleanNumber
        } else if (cleanNumber.startsWith("91") && cleanNumber.length == 12) {
            "+$cleanNumber"
        } else {
            "+91 $cleanNumber"
        }
    }
    
    /**
     * Extract clean phone number without country code.
     * @param phoneNumber Phone number with/without country code
     * @return Clean 10-digit phone number
     */
    fun extractPhoneNumber(phoneNumber: String): String {
        val cleanNumber = phoneNumber.replace("[^0-9]".toRegex(), "")
        return if (cleanNumber.length == 12 && cleanNumber.startsWith("91")) {
            cleanNumber.substring(2)
        } else {
            cleanNumber
        }
    }
}
