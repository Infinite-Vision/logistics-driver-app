package com.example.logistics_driver_app.data.NetworkCall

import com.example.logistics_driver_app.data.model.AuthSession
import com.example.logistics_driver_app.data.model.Driver
import com.example.logistics_driver_app.data.model.OTPRequest
import com.example.logistics_driver_app.data.model.OTPResponse
import com.example.logistics_driver_app.data.model.OTPVerifyRequest
import com.example.logistics_driver_app.data.model.OTPVerifyResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Service interface.
 * Defines all network API endpoints for the application.
 * Currently prepared for future backend integration.
 */
interface ApiService {
    
    /**
     * Send OTP to phone number for authentication.
     * @param request OTP request containing country code and phone number
     * @return Response with OTP result
     */
    @POST("auth/otp/request")
    suspend fun requestOTP(
        @Body request: OTPRequest
    ): Response<OTPResponse>
    
    /**
     * Verify OTP for phone number authentication.
     * @param request OTP verification data
     * @return Response with auth token and onboarding status
     */
    @POST("auth/otp/verify")
    suspend fun verifyOTP(
        @Body request: OTPVerifyRequest
    ): Response<OTPVerifyResponse>
    
    /**
     * Get driver profile by ID.
     * @param driverId Driver unique identifier
     * @return Driver profile data
     */
    @GET("driver/{id}")
    suspend fun getDriverProfile(
        @Path("id") driverId: Int
    ): Response<Driver>
    
    /**
     * Create new driver profile.
     * @param driver Driver data to create
     * @return Created driver data with ID
     */
    @POST("driver")
    suspend fun createDriverProfile(
        @Body driver: Driver
    ): Response<Driver>
    
    /**
     * Update existing driver profile.
     * @param driverId Driver ID to update
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
     * @param driverId Driver ID to delete
     * @return Success response
     */
    @DELETE("driver/{id}")
    suspend fun deleteDriverProfile(
        @Path("id") driverId: Int
    ): Response<Map<String, Any>>
}
