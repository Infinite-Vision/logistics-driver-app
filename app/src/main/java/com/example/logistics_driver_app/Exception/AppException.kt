package com.example.logistics_driver_app.Exception

/**
 * AppException - Base exception class for application-specific exceptions.
 * All custom exceptions in the app should extend this class.
 */
sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    
    /**
     * Network-related exceptions.
     */
    class NetworkException(message: String = "Network error occurred", cause: Throwable? = null) :
        AppException(message, cause)
    
    /**
     * API-related exceptions.
     */
    class ApiException(message: String = "API error occurred", cause: Throwable? = null) :
        AppException(message, cause)
    
    /**
     * Database-related exceptions.
     */
    class DatabaseException(message: String = "Database error occurred", cause: Throwable? = null) :
        AppException(message, cause)
    
    /**
     * Validation exceptions.
     */
    class ValidationException(message: String = "Validation failed", cause: Throwable? = null) :
        AppException(message, cause)
    
    /**
     * Authentication exceptions.
     */
    class AuthException(message: String = "Authentication failed", cause: Throwable? = null) :
        AppException(message, cause)
    
    /**
     * Permission exceptions.
     */
    class PermissionException(message: String = "Permission denied", cause: Throwable? = null) :
        AppException(message, cause)
    
    /**
     * Unknown/Generic exceptions.
     */
    class UnknownException(message: String = "Unknown error occurred", cause: Throwable? = null) :
        AppException(message, cause)
}

/**
 * ExceptionHandler - Centralized exception handling.
 */
object ExceptionHandler {
    
    /**
     * Convert throwable to user-friendly message.
     * @param throwable Error throwable
     * @return User-friendly error message
     */
    fun getUserMessage(throwable: Throwable): String {
        return when (throwable) {
            is AppException.NetworkException -> "No internet connection. Please check your network and try again."
            is AppException.ApiException -> throwable.message ?: "Server error. Please try again later."
            is AppException.DatabaseException -> "Data storage error. Please try again."
            is AppException.ValidationException -> throwable.message ?: "Invalid input. Please check and try again."
            is AppException.AuthException -> "Authentication failed. Please login again."
            is AppException.PermissionException -> throwable.message ?: "Permission required to perform this action."
            is AppException.UnknownException -> "Something went wrong. Please try again."
            else -> "An unexpected error occurred."
        }
    }
    
    /**
     * Log exception for debugging.
     * @param throwable Error throwable
     * @param tag Optional tag for logging
     */
    fun logException(throwable: Throwable, tag: String = "AppException") {
        println("[$tag] ${throwable::class.simpleName}: ${throwable.message}")
        throwable.printStackTrace()
    }
}
