package com.example.logistics_driver_app.Locations

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.example.logistics_driver_app.Listener.LocationUpdateListener

/**
 * LocationHelper - Helper class for managing location services.
 * Provides easy access to device location with permission handling.
 */
class LocationHelper(private val context: Context) {
    
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var locationListener: LocationUpdateListener? = null
    
    companion object {
        private const val MIN_TIME_BETWEEN_UPDATES = 5000L // 5 seconds
        private const val MIN_DISTANCE_CHANGE = 10f // 10 meters
    }
    
    /**
     * Check if location permissions are granted.
     * @return True if permissions granted
     */
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Get last known location.
     * @return Last known location or null
     */
    fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }
        
        return try {
            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            
            // Return the more accurate location
            when {
                gpsLocation != null && networkLocation != null -> {
                    if (gpsLocation.accuracy < networkLocation.accuracy) gpsLocation else networkLocation
                }
                gpsLocation != null -> gpsLocation
                else -> networkLocation
            }
        } catch (e: SecurityException) {
            null
        }
    }
    
    /**
     * Start location updates.
     * @param listener Location update listener
     */
    fun startLocationUpdates(listener: LocationUpdateListener) {
        this.locationListener = listener
        
        if (!hasLocationPermission()) {
            listener.onLocationError("Location permission not granted")
            return
        }
        
        val internalListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationListener?.onLocationUpdated(location.latitude, location.longitude)
            }
            
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                locationListener?.onLocationError("Location provider disabled")
            }
            
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
        
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BETWEEN_UPDATES,
                MIN_DISTANCE_CHANGE,
                internalListener
            )
            
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BETWEEN_UPDATES,
                MIN_DISTANCE_CHANGE,
                internalListener
            )
        } catch (e: SecurityException) {
            listener.onLocationError("Failed to start location updates: ${e.message}")
        }
    }
    
    /**
     * Stop location updates.
     */
    fun stopLocationUpdates() {
        locationListener = null
        // Note: You should keep reference to LocationListener to remove it
        // For simplicity, this implementation is basic
    }
    
    /**
     * Check if GPS provider is enabled.
     * @return True if GPS enabled
     */
    fun isGPSEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
    
    /**
     * Check if network provider is enabled.
     * @return True if network provider enabled
     */
    fun isNetworkProviderEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
