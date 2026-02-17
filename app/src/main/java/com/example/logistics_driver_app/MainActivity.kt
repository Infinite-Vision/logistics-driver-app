package com.example.logistics_driver_app

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.logistics_driver_app.databinding.ActivityMainBinding
import com.example.logistics_driver_app.data.local.preferences.PreferencesManager
import com.example.logistics_driver_app.Common.util.S3UploadUtil
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.Common.util.LocationPermissionHelper

/**
 * Main Activity for the Logistics Driver App.
 * Handles the navigation flow between onboarding and main app screens.
 * Uses Navigation Component for fragment management.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var preferencesManager: PreferencesManager
    
    // Flag to track if location check should be done after navigation setup
    private var shouldCheckLocationAfterNavigation = true

    // Permission launcher for location permissions
    private val requestPermissionsLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results: Map<String, Boolean> ->
        val fineLocationGranted: Boolean = results[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted: Boolean = results[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted && coarseLocationGranted) {
            Log.d(TAG, "[PERMISSION] Foreground location permissions granted")
            // After foreground permissions granted, request background permission
            LocationPermissionHelper.requestBackgroundLocationPermission(
                this,
                requestPermissionsLauncher
            )
        } else {
            Log.w(TAG, "[PERMISSION] Foreground location permissions denied")
            // Check if we should show rationale or if permission is permanently denied
            if (LocationPermissionHelper.shouldShowPermissionRationale(this)) {
                LocationPermissionHelper.showLocationPermissionRationale(this) {
                    LocationPermissionHelper.requestForegroundLocationPermissions(requestPermissionsLauncher)
                }
            } else {
                LocationPermissionHelper.showPermissionDeniedDialog(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "[LIFECYCLE] onCreate called")
        
        // Set up global crash handler to log crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "[FATAL CRASH] Uncaught exception on thread: ${thread.name}", throwable)
            Log.e(TAG, "[FATAL CRASH] Exception: ${throwable.javaClass.simpleName}: ${throwable.message}")
            Log.e(TAG, "[FATAL CRASH] Stack trace:")
            throwable.printStackTrace()
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize preferences manager
        preferencesManager = PreferencesManager.getInstance(this)
        Log.d(TAG, "[LIFECYCLE] PreferencesManager initialized")
        
        // Log persisted user data for verification
        logPersistedUserData()

        // Initialize S3 upload utility
        S3UploadUtil.initialize(this)
        Log.d(TAG, "[LIFECYCLE] S3UploadUtil initialized")

        // Setup navigation
        setupNavigation()
        Log.d(TAG, "[LIFECYCLE] onCreate completed")
    }

    /**
     * Setup navigation controller and determine start destination
     * based on user's onboarding status.
     */
    private fun setupNavigation() {
        Log.d(TAG, "[NAVIGATION] Setting up navigation...")
        
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Get navigation graph
        val navGraph = navController.navInflater.inflate(R.navigation.auth_navigation)

        // Use SharedPreference (not PreferencesManager) to check login state
        // This is where OTPViewModel saves the data
        val sharedPref = SharedPreference.getInstance(this)
        val isLoggedIn = sharedPref.isLoggedIn()
        val hasToken = !sharedPref.getSessionToken().isNullOrEmpty()
        val hasPhone = !sharedPref.getPhoneNumber().isNullOrEmpty()
        
        Log.d(TAG, "[NAVIGATION] Checking persisted session...")
        Log.d(TAG, "[NAVIGATION] isLoggedIn: $isLoggedIn")
        Log.d(TAG, "[NAVIGATION] hasToken: $hasToken")
        Log.d(TAG, "[NAVIGATION] hasPhone: $hasPhone")

        // Determine start destination based on user state
        val startDestination = when {
            isLoggedIn && hasToken && hasPhone -> {
                // User is logged in with valid session - skip onboarding
                Log.d(TAG, "[NAVIGATION] User session found! Skipping to verification/main screen")
                Log.d(TAG, "[NAVIGATION] Start destination: VerificationProgressFragment")
                // TODO: Check app state API to determine exact screen
                R.id.verificationProgressFragment
            }
            else -> {
                // No valid session - start from language selection
                Log.d(TAG, "[NAVIGATION] No valid session - starting from language selection")
                R.id.languageSelectionFragment
            }
        }

        navGraph.setStartDestination(startDestination)
        navController.graph = navGraph
        Log.d(TAG, "[NAVIGATION] Navigation setup completed")
    }

    /**
     * Handle back button press through navigation component.
     */
    override fun onSupportNavigateUp(): Boolean {
        Log.d(TAG, "[NAVIGATION] onSupportNavigateUp called")
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * Handle system back button.
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        Log.d(TAG, "[NAVIGATION] onBackPressed called")
        Log.d(TAG, "[NAVIGATION] Current destination: ${navController.currentDestination?.label}")
        
        if (!navController.popBackStack()) {
            Log.d(TAG, "[NAVIGATION] No more fragments in back stack, finishing activity")
            super.onBackPressed()
        } else {
            Log.d(TAG, "[NAVIGATION] Popped back stack successfully")
        }
    }
    
    /**
     * Log persisted user data to verify data persistence across app restarts.
     * This helps confirm that token, phone number, and language are properly saved.
     */
    private fun logPersistedUserData() {
        val sharedPref = SharedPreference.getInstance(this)
        
        Log.d(TAG, "[PERSIST_CHECK] ===== CHECKING PERSISTED DATA =====")
        Log.d(TAG, "[PERSIST_CHECK] Is Logged In: ${sharedPref.isLoggedIn()}")
        Log.d(TAG, "[PERSIST_CHECK] Phone Number: ${sharedPref.getPhoneNumber()}")
        Log.d(TAG, "[PERSIST_CHECK] Session Token: ${sharedPref.getSessionToken()?.take(20)}...")
        Log.d(TAG, "[PERSIST_CHECK] Language Code: ${sharedPref.getLanguage()}")
        Log.d(TAG, "[PERSIST_CHECK] Onboarding Completed: ${sharedPref.isOnboardingCompleted()}")
        
        if (sharedPref.isLoggedIn()) {
            Log.d(TAG, "[PERSIST_CHECK] ✓ User session is ACTIVE and PERSISTED")
        } else {
            Log.d(TAG, "[PERSIST_CHECK] ✗ No active user session found")
        }
        Log.d(TAG, "[PERSIST_CHECK] ===================================")
    }
    
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "[LIFECYCLE] onStart")
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "[LIFECYCLE] onResume")
        
        // Check location permissions and settings when app comes to foreground
        if (shouldCheckLocationAfterNavigation) {
            shouldCheckLocationAfterNavigation = false
            checkLocationPermissionsAndSettings()
        }
    }
    
    /**
     * Check if location permissions are granted and location services are enabled.
     * Prompt the user if either is missing.
     */
    private fun checkLocationPermissionsAndSettings() {
        Log.d(TAG, "[PERMISSION] Checking location permissions and settings")
        
        // First check if location services are enabled
        if (!LocationPermissionHelper.isLocationEnabled(this)) {
            Log.w(TAG, "[PERMISSION] Location services are disabled")
            LocationPermissionHelper.showEnableLocationDialog(this)
            return
        }
        
        // Check foreground location permissions
        if (!LocationPermissionHelper.hasForegroundLocationPermission(this)) {
            Log.w(TAG, "[PERMISSION] Foreground location permissions not granted")
            LocationPermissionHelper.requestForegroundLocationPermissions(requestPermissionsLauncher)
            return
        }
        
        // Check background location permission (Android 10+)
        if (!LocationPermissionHelper.hasBackgroundLocationPermission(this)) {
            Log.w(TAG, "[PERMISSION] Background location permission not granted")
            LocationPermissionHelper.requestBackgroundLocationPermission(
                this,
                requestPermissionsLauncher
            )
            return
        }
        
        // All permissions granted and location enabled
        Log.d(TAG, "[PERMISSION] All location permissions granted and location is enabled")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "[LIFECYCLE] onPause")
    }
    
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "[LIFECYCLE] onStop")
    }
    
    override fun onDestroy() {
        Log.d(TAG, "[LIFECYCLE] onDestroy")
        super.onDestroy()
    }
}
