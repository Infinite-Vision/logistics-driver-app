package com.example.logistics_driver_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.logistics_driver_app.databinding.ActivityMainBinding
import com.example.logistics_driver_app.data.local.preferences.PreferencesManager

/**
 * Main Activity for the Logistics Driver App.
 * Handles the navigation flow between onboarding and main app screens.
 * Uses Navigation Component for fragment management.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize preferences manager
        preferencesManager = PreferencesManager.getInstance(this)

        // Setup navigation
        setupNavigation()
    }

    /**
     * Setup navigation controller and determine start destination
     * based on user's onboarding status.
     */
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Get navigation graph
        val navGraph = navController.navInflater.inflate(R.navigation.auth_navigation)

        // Determine start destination based on user state
        val startDestination = when {
            !preferencesManager.isOnboardingCompleted() -> {
                // Not completed onboarding - start with language selection
                R.id.languageSelectionFragment
            }
            !preferencesManager.isLoggedIn() -> {
                // Completed onboarding but not logged in - start with phone entry
                R.id.phoneEntryFragment
            }
            else -> {
                // Logged in - could navigate to main app (for now, driver details)
                R.id.driverDetailsFragment
            }
        }

        navGraph.setStartDestination(startDestination)
        navController.graph = navGraph
    }

    /**
     * Handle back button press through navigation component.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * Handle system back button.
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!navController.popBackStack()) {
            super.onBackPressed()
        }
    }
}
