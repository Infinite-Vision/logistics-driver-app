package com.example.logistics_driver_app.RoomDB.repository

import androidx.lifecycle.LiveData
import com.example.logistics_driver_app.RoomDB.dao.AuthDao
import com.example.logistics_driver_app.data.model.AuthSession

/**
 * AuthRepository - Functions to make DB operations for AuthSession entity.
 * Handles authentication session data operations.
 */
class AuthRepository(private val authDao: AuthDao) {
    
    /**
     * Get current authenticated session as LiveData.
     * @return LiveData of verified AuthSession
     */
    fun getCurrentSession(): LiveData<AuthSession?> {
        return authDao.getCurrentSession()
    }
    
    /**
     * Get current authenticated session (one-time query).
     * @return Verified AuthSession or null
     */
    suspend fun getCurrentSessionSync(): AuthSession? {
        return authDao.getCurrentSessionSync()
    }
    
    /**
     * Get auth session by phone number as LiveData.
     * @param phoneNumber Phone number to lookup
     * @return LiveData of AuthSession
     */
    fun getAuthSession(phoneNumber: String): LiveData<AuthSession?> {
        return authDao.getAuthSession(phoneNumber)
    }
    
    /**
     * Get auth session by phone number (one-time query).
     * @param phoneNumber Phone number to lookup
     * @return AuthSession or null
     */
    suspend fun getAuthSessionSync(phoneNumber: String): AuthSession? {
        return authDao.getAuthSessionSync(phoneNumber)
    }
    
    /**
     * Save or update auth session.
     * @param authSession AuthSession to save
     */
    suspend fun saveAuthSession(authSession: AuthSession) {
        authDao.insertAuthSession(authSession)
    }
    
    /**
     * Update language preference for session.
     * @param phoneNumber Phone number of session
     * @param languageCode New language code
     */
    suspend fun updateLanguage(phoneNumber: String, languageCode: String) {
        authDao.updateLanguage(phoneNumber, languageCode)
    }
    
    /**
     * Logout - delete all sessions.
     */
    suspend fun logout() {
        authDao.deleteAllSessions()
    }
    
    /**
     * Delete specific session.
     * @param phoneNumber Phone number of session to delete
     */
    suspend fun deleteSession(phoneNumber: String) {
        authDao.deleteSession(phoneNumber)
    }
}
