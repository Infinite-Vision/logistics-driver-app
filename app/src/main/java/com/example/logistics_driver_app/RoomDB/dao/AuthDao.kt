package com.example.logistics_driver_app.RoomDB.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.logistics_driver_app.data.model.AuthSession

/**
 * AuthDao - Data Access Object for AuthSession entity.
 * Provides all CRUD operation calls for authentication data.
 */
@Dao
interface AuthDao {
    
    /**
     * Insert or update authentication session.
     * @param authSession AuthSession entity to save
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthSession(authSession: AuthSession)
    
    /**
     * Get auth session by phone number (LiveData - reactive).
     * @param phoneNumber Phone number to lookup
     * @return LiveData of AuthSession or null
     */
    @Query("SELECT * FROM auth_sessions WHERE phoneNumber = :phoneNumber LIMIT 1")
    fun getAuthSession(phoneNumber: String): LiveData<AuthSession?>
    
    /**
     * Get auth session by phone number (suspend - one-time query).
     * @param phoneNumber Phone number to lookup
     * @return AuthSession or null
     */
    @Query("SELECT * FROM auth_sessions WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getAuthSessionSync(phoneNumber: String): AuthSession?
    
    /**
     * Get current verified session (LiveData - reactive).
     * @return LiveData of verified AuthSession
     */
    @Query("SELECT * FROM auth_sessions WHERE isVerified = 1 LIMIT 1")
    fun getCurrentSession(): LiveData<AuthSession?>
    
    /**
     * Get current verified session (suspend - one-time query).
     * @return Verified AuthSession or null
     */
    @Query("SELECT * FROM auth_sessions WHERE isVerified = 1 LIMIT 1")
    suspend fun getCurrentSessionSync(): AuthSession?
    
    /**
     * Update language preference for session.
     * @param phoneNumber Phone number of session
     * @param languageCode New language code
     */
    @Query("UPDATE auth_sessions SET languageCode = :languageCode WHERE phoneNumber = :phoneNumber")
    suspend fun updateLanguage(phoneNumber: String, languageCode: String)
    
    /**
     * Delete all authentication sessions (logout all).
     */
    @Query("DELETE FROM auth_sessions")
    suspend fun deleteAllSessions()
    
    /**
     * Delete specific auth session by phone number.
     * @param phoneNumber Phone number of session to delete
     */
    @Query("DELETE FROM auth_sessions WHERE phoneNumber = :phoneNumber")
    suspend fun deleteSession(phoneNumber: String)
}
