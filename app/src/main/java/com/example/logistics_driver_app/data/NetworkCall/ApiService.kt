package com.example.logistics_driver_app.data.NetworkCall

import com.example.logistics_driver_app.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Service interface.
 * Defines all network API endpoints for the Logistics Driver App.
 * Based on Backend Specification v1.
 */
interface ApiService {
    
    // ============= Authentication Module =============
    
    /**
     * 1.1 Request OTP
     * Send OTP to phone number for authentication.
     * Endpoint: POST /api/v1/auth/otp/request
     */
    @POST("auth/otp/request")
    suspend fun requestOTP(
        @Body request: OTPRequest
    ): Response<ApiResponse<MessageResponse>>
    
    /**
     * 1.2 Verify OTP
     * Verify OTP and get JWT token with onboarding status.
     * Endpoint: POST /api/v1/auth/otp/verify
     */
    @POST("auth/otp/verify")
    suspend fun verifyOTP(
        @Body request: OTPVerifyRequest
    ): Response<ApiResponse<VerifyData>>
    
    /**
     * 1.3 Logout
     * Revoke current JWT token.
     * Endpoint: POST /api/v1/auth/logout
     */
    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<LogoutResponse>
    
    // ============= App State Module =============
    
    /**
     * 2.1 Get App State
     * Get user's current onboarding progress and next screen.
     * Endpoint: GET /api/v1/app/state
     */
    @GET("app/state")
    suspend fun getAppState(
        @Header("Authorization") token: String
    ): Response<ApiResponse<AppStateResponse>>
    
    // ============= User Preferences Module =============
    
    /**
     * 3.1 Update Preferred Language
     * Update user's preferred language.
     * Endpoint: POST /api/v1/users/language
     */
    @POST("users/language")
    suspend fun updateLanguage(
        @Header("Authorization") token: String,
        @Body request: UpdateLanguageRequest
    ): Response<ApiResponse<UpdateLanguageResponse>>
    
    // ============= Onboarding Owner Module =============
    
    /**
     * 4.1 Save Owner
     * Save or update vehicle owner details.
     * Endpoint: POST /api/v1/onboarding/owner
     */
    @POST("onboarding/owner")
    suspend fun saveOwner(
        @Header("Authorization") token: String,
        @Body request: SaveOwnerRequest
    ): Response<ApiResponse<OwnerResponse>>
    
    /**
     * 4.2 Get Owner
     * Get saved owner details.
     * Endpoint: GET /api/v1/onboarding/owner
     */
    @GET("onboarding/owner")
    suspend fun getOwner(
        @Header("Authorization") token: String
    ): Response<ApiResponse<OwnerResponse>>
    
    // ============= Onboarding Vehicle Module =============
    
    /**
     * 5.1 Save Vehicle
     * Save or update vehicle details.
     * Endpoint: POST /api/v1/onboarding/vehicle
     */
    @POST("onboarding/vehicle")
    suspend fun saveVehicle(
        @Header("Authorization") token: String,
        @Body request: SaveVehicleRequest
    ): Response<ApiResponse<VehicleResponse>>
    
    /**
     * 5.2 Get Vehicle
     * Get saved vehicle details.
     * Endpoint: GET /api/v1/onboarding/vehicle
     */
    @GET("onboarding/vehicle")
    suspend fun getVehicle(
        @Header("Authorization") token: String
    ): Response<ApiResponse<VehicleResponse>>
    
    // ============= Onboarding Driver Module =============
    
    /**
     * 6.1 Save Driver
     * Save or update driver details.
     * Endpoint: POST /api/v1/onboarding/driver
     */
    @POST("onboarding/driver")
    suspend fun saveDriver(
        @Header("Authorization") token: String,
        @Body request: SaveDriverRequest
    ): Response<ApiResponse<DriverResponse>>
    
    /**
     * 6.2 Get Driver
     * Get saved driver details.
     * Endpoint: GET /api/v1/onboarding/driver
     */
    @GET("onboarding/driver")
    suspend fun getDriver(
        @Header("Authorization") token: String
    ): Response<ApiResponse<DriverResponse>>
    
    // ============= Meta Module =============
    
    /**
     * 7.1 Get Vehicle Form Options
     * Get dropdown options for vehicle form (localized).
     * Endpoint: GET /api/v1/meta/vehicle-form-options
     */
    @GET("meta/vehicle-form-options")
    suspend fun getVehicleFormOptions(
        @Header("Authorization") token: String
    ): Response<ApiResponse<VehicleFormOptionsResponse>>
    
    // ============= Driver Home Module =============
    
    /**
     * 8.1 Get Driver Home Summary
     * Get driver's home screen data including status, today's summary, and eligibility to go online.
     * Endpoint: GET /api/v1/driver/home/summary
     */
    @GET("driver/home/summary")
    suspend fun getDriverHomeSummary(
        @Header("Authorization") token: String
    ): Response<ApiResponse<DriverHomeSummaryResponse>>
    
    // ============= Driver Trip Flow Module =============
    
    /**
     * 9.1 Accept Order
     * Accept an offered order.
     * Endpoint: POST /api/v1/driver/orders/{orderId}/accept
     */
    @POST("driver/orders/{orderId}/accept")
    suspend fun acceptOrder(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: Long
    ): Response<ApiResponse<TripAcceptResponse>>
    
    /**
     * 9.2 Reject Order
     * Reject an offered order.
     * Endpoint: POST /api/v1/driver/orders/{orderId}/reject
     * Returns: 204 No Content
     */
    @POST("driver/orders/{orderId}/reject")
    suspend fun rejectOrder(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: Long
    ): Response<Void>
    
    /**
     * 9.3 Mark Arrived at Pickup
     * Mark driver as arrived at pickup location (geofence check).
     * Endpoint: POST /api/v1/driver/orders/{orderId}/arrived-pickup
     */
    @POST("driver/orders/{orderId}/arrived-pickup")
    suspend fun arrivedAtPickup(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: Long,
        @Body request: ArrivedAtPickupRequest
    ): Response<ApiResponse<ArrivedAtPickupResponse>>
    
    /**
     * 9.4 Start Trip (Confirm with OTP)
     * Start the trip after verifying pickup OTP.
     * Endpoint: POST /api/v1/driver/orders/{orderId}/start-trip/confirm
     */
    @POST("driver/orders/{orderId}/start-trip/confirm")
    suspend fun startTrip(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: Long,
        @Body request: StartTripRequest
    ): Response<ApiResponse<StartTripResponse>>
    
    /**
     * 9.5 Mark Arrived at Drop
     * Mark driver as arrived at drop location (geofence check).
     * Endpoint: POST /api/v1/driver/orders/{orderId}/arrived-drop
     */
    @POST("driver/orders/{orderId}/arrived-drop")
    suspend fun arrivedAtDrop(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: Long,
        @Body request: ArrivedAtDropRequest
    ): Response<ApiResponse<ArrivedAtDropResponse>>
    
    /**
     * 9.6 End Trip
     * Complete the trip at drop location.
     * Endpoint: POST /api/v1/driver/orders/{orderId}/end-trip
     */
    @POST("driver/orders/{orderId}/end-trip")
    suspend fun endTrip(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: Long,
        @Body request: EndTripRequest
    ): Response<ApiResponse<EndTripResponse>>
    
    // ============= Driver Trip History Module =============
    
    /**
     * 10.1 Get Trip List
     * Get driver's trip history with filter and pagination.
     * Endpoint: GET /api/v1/driver/trips?filter={today|thisWeek|thisMonth}&page=0&size=20
     */
    @GET("driver/trips")
    suspend fun getTripList(
        @Header("Authorization") token: String,
        @Query("filter") filter: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<TripListResponse>>
    
    /**
     * 10.2 Get Trip Detail
     * Get detailed information about a specific trip.
     * Endpoint: GET /api/v1/driver/trips/{orderId}
     */
    @GET("driver/trips/{orderId}")
    suspend fun getTripDetail(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: Long
    ): Response<ApiResponse<TripDetailResponse>>
}
