package com.example.logistics_driver_app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.logistics_driver_app.data.model.AuthSession

/**
 * Data Access Object for AuthSession entity.
 * Handles authentication session data operations.
 */
@Dao
interface AuthDao {
    
    /**
     * Insert or update an auth session.
     * @param authSession AuthSession object to insert/update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthSession(authSession: AuthSession)
    
    /**
     * Get auth session by phone number.
     * @param phoneNumber Phone number to lookup
     * @return LiveData of AuthSession
     */
    @Query("SELECT * FROM auth_sessions WHERE phoneNumber = :phoneNumber LIMIT 1")
    fun getAuthSession(phoneNumber: String): LiveData<AuthSession?>
    
    /**
     * Get auth session by phone number (suspend function).
     * @param phoneNumber Phone number to lookup
     * @return AuthSession or null
     */
    @Query("SELECT * FROM auth_sessions WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getAuthSessionSync(phoneNumber: String): AuthSession?
    
    /**
     * Get the current verified session.
     * @return LiveData of verified AuthSession
     */
    @Query("SELECT * FROM auth_sessions WHERE isVerified = 1 LIMIT 1")
    fun getCurrentSession(): LiveData<AuthSession?>
    
    /**
     * Get the current verified session (suspend function).
     * @return Verified AuthSession or null
     */
    @Query("SELECT * FROM auth_sessions WHERE isVerified = 1 LIMIT 1")
    suspend fun getCurrentSessionSync(): AuthSession?
    
    /**
     * Update language code for a session.
     * @param phoneNumber Phone number of the session
     * @param languageCode New language code
     */
    @Query("UPDATE auth_sessions SET languageCode = :languageCode WHERE phoneNumber = :phoneNumber")
    suspend fun updateLanguage(phoneNumber: String, languageCode: String)
    
    /**
     * Delete all auth sessions (logout all).
     */
    @Query("DELETE FROM auth_sessions")
    suspend fun deleteAllSessions()
    
    /**
     * Delete specific auth session.
     * @param phoneNumber Phone number of the session to delete
     */
    @Query("DELETE FROM auth_sessions WHERE phoneNumber = :phoneNumber")
    suspend fun deleteSession(phoneNumber: String)
}
