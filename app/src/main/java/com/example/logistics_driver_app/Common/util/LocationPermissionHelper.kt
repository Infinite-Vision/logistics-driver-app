package com.example.logistics_driver_app.Common.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Helper class to handle location permissions and settings.
 * Handles foreground, background location permissions and location service checks.
 */
object LocationPermissionHelper {

    private const val TAG = "LocationPermissionHelper"

    /**
     * Check if foreground location permissions are granted
     */
    fun hasForegroundLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if background location permission is granted (Android 10+)
     */
    fun hasBackgroundLocationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Below Android 10, background location is granted with foreground permission
            true
        }
    }

    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Below Android 13, notification permission is not required
            true
        }
    }

    /**
     * Check if location services are enabled on the device
     */
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Get required foreground location permissions
     */
    fun getForegroundLocationPermissions(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        // Add notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        return permissions.toTypedArray()
    }

    /**
     * Request foreground location permissions
     */
    fun requestForegroundLocationPermissions(
        requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
    ) {
        requestPermissionsLauncher.launch(getForegroundLocationPermissions())
    }

    /**
     * Request background location permission with explanation dialog (Android 10+)
     */
    fun requestBackgroundLocationPermission(
        activity: Activity,
        requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasBackgroundLocationPermission(activity)) {
                // Show explanation dialog before requesting permission
                AlertDialog.Builder(activity)
                    .setTitle("Background Location Required")
                    .setMessage("This app needs 'Allow all the time' location access to track your trips even when the app is in the background or closed.")
                    .setPositiveButton("Continue") { _, _ ->
                        requestPermissionsLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        )
                    }
                    .setNegativeButton("Not Now", null)
                    .show()
            }
        }
    }

    /**
     * Show dialog to prompt user to enable location services
     */
    fun showEnableLocationDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Location Services Disabled")
            .setMessage("Location services are turned off. Please enable location to use this app.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show()
    }

    /**
     * Check if all necessary permissions are granted and location is enabled
     */
    fun hasAllPermissionsAndLocationEnabled(
        context: Context,
        requireBackground: Boolean = false
    ): Boolean {
        val hasForeground = hasForegroundLocationPermission(context)
        val hasBackground = if (requireBackground) hasBackgroundLocationPermission(context) else true
        val hasNotification = hasNotificationPermission(context)
        val locationEnabled = isLocationEnabled(context)

        return hasForeground && hasBackground && hasNotification && locationEnabled
    }

    /**
     * Show rationale dialog explaining why location permission is needed
     */
    fun showLocationPermissionRationale(
        activity: Activity,
        onPositive: () -> Unit
    ) {
        AlertDialog.Builder(activity)
            .setTitle("Location Permission Required")
            .setMessage("This app requires location access to track your delivery trips and provide accurate navigation.")
            .setPositiveButton("Grant Permission") { _, _ ->
                onPositive()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Check if permission rationale should be shown
     */
    fun shouldShowPermissionRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    /**
     * Open app settings page
     */
    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }

    /**
     * Show dialog when permission is permanently denied
     */
    fun showPermissionDeniedDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Permission Required")
            .setMessage("Location permission is required for this app to function. Please enable it in app settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings(activity)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
