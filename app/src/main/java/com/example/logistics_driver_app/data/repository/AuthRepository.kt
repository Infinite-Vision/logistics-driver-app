package com.example.logistics_driver_app.data.repository

import androidx.lifecycle.LiveData
import com.example.logistics_driver_app.data.local.dao.AuthDao
import com.example.logistics_driver_app.data.model.AuthSession

/**
 * Repository class for Authentication operations.
 * Handles auth session management and provides clean API to ViewModel.
 */
class AuthRepository(private val authDao: AuthDao) {
    
    /**
     * Get current authenticated session.
     * @return LiveData of current verified AuthSession
     */
    fun getCurrentSession(): LiveData<AuthSession?> {
        return authDao.getCurrentSession()
    }
    
    /**
     * Get current authenticated session (one-time query).
     * @return Current verified AuthSession or null
     */
    suspend fun getCurrentSessionSync(): AuthSession? {
        return authDao.getCurrentSessionSync()
    }
    
    /**
     * Get auth session by phone number.
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
     * Create or update auth session.
     * @param authSession AuthSession object to save
     */
    suspend fun saveAuthSession(authSession: AuthSession) {
        authDao.insertAuthSession(authSession)
    }
    
    /**
     * Update language preference for a session.
     * @param phoneNumber Phone number of the session
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
     * @param phoneNumber Phone number of the session to delete
     */
    suspend fun deleteSession(phoneNumber: String) {
        authDao.deleteSession(phoneNumber)
    }
}
