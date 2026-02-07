package com.example.logistics_driver_app.data.remote

import com.example.logistics_driver_app.data.model.AuthSession
import com.example.logistics_driver_app.data.model.Driver
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for backend communication.
 * Currently not in use as there's no backend API.
 * Prepared for future integration with actual API.
 */
interface ApiService {
    
    /**
     * Send OTP to phone number.
     * @param phoneNumber Phone number to send OTP
     * @return Response with OTP details
     */
    @POST("auth/send-otp")
    suspend fun sendOTP(
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>
    
    /**
     * Verify OTP.
     * @param phoneNumber Phone number
     * @param otp OTP code
     * @return Response with auth token
     */
    @POST("auth/verify-otp")
    suspend fun verifyOTP(
        @Body request: Map<String, String>
    ): Response<AuthSession>
    
    /**
     * Get driver profile.
     * @param driverId Driver ID
     * @return Driver profile data
     */
    @GET("driver/{id}")
    suspend fun getDriverProfile(
        @Path("id") driverId: Int
    ): Response<Driver>
    
    /**
     * Create or update driver profile.
     * @param driver Driver data
     * @return Updated driver data
     */
    @POST("driver")
    suspend fun saveDriverProfile(
        @Body driver: Driver
    ): Response<Driver>
    
    /**
     * Update driver profile.
     * @param driverId Driver ID
     * @param driver Updated driver data
     * @return Updated driver data
     */
    @PUT("driver/{id}")
    suspend fun updateDriverProfile(
        @Path("id") driverId: Int,
        @Body driver: Driver
    ): Response<Driver>
    
    /**
     * Delete driver profile.
     * @param driverId Driver ID
     * @return Success response
     */
    @DELETE("driver/{id}")
    suspend fun deleteDriverProfile(
        @Path("id") driverId: Int
    ): Response<Map<String, Any>>
}
