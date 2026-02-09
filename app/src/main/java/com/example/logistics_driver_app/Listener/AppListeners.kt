package com.example.logistics_driver_app.Listener

/**
 * NetworkConnectionListener - Interface for monitoring network connectivity changes.
 * Implement this interface to receive network state updates.
 */
interface NetworkConnectionListener {
    
    /**
     * Called when network connection is established.
     */
    fun onNetworkConnected()
    
    /**
     * Called when network connection is lost.
     */
    fun onNetworkDisconnected()
    
    /**
     * Called when network type changes (WiFi <-> Mobile Data).
     * @param isWifi True if connected to WiFi, false if mobile data
     */
    fun onNetworkTypeChanged(isWifi: Boolean)
}

/**
 * OTPAutoReadListener - Interface for automatic OTP reading.
 * Implement this interface to receive OTP when automatically read from SMS.
 */
interface OTPAutoReadListener {
    
    /**
     * Called when OTP is successfully read from SMS.
     * @param otp The OTP code read from message
     */
    fun onOTPReceived(otp: String)
    
    /**
     * Called when OTP reading fails or times out.
     */
    fun onOTPTimeout()
}

/**
 * LocationUpdateListener - Interface for location updates.
 * Implement this interface to receive location updates.
 */
interface LocationUpdateListener {
    
    /**
     * Called when location is updated.
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     */
    fun onLocationUpdated(latitude: Double, longitude: Double)
    
    /**
     * Called when location update fails.
     * @param error Error message
     */
    fun onLocationError(error: String)
}
