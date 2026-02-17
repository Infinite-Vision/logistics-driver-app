package com.example.locationtracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private var isServiceRunning = false
    private lateinit var btnStart: Button
    private var pendingStartServiceAfterOverlay = false

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            // Fine location granted, now ask for Background Location
            requestBackgroundPermission()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Trigger the flow via a button (start/stop toggle)
        btnStart = findViewById(R.id.btnStart)
        updateButtonLabel()
        btnStart.setOnClickListener {
            if (isServiceRunning) {
                stopLocationService()
            } else {
                checkAndStartPermissions()
            }
        }
        
        // Ask user to exclude app from battery optimizations (only if not already ignored)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(PowerManager::class.java)
            if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    private fun checkAndStartPermissions() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionsLauncher.launch(permissions.toTypedArray())
    }

    private fun requestBackgroundPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // You MUST show a dialog explaining WHY you need "Allow all the time"
                // before sending them to settings.
                AlertDialog.Builder(this)
                    .setTitle("Background Location Required")
                    .setMessage("This app needs 'Allow all the time' location access to track you even when the app is closed.")
                    .setPositiveButton("Settings") { _, _ ->
                        requestPermissionsLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                    }
                    .show()
            } else {
                ensureOverlayAndStartService()
            }
        } else {
            ensureOverlayAndStartService()
        }
    }

    override fun onResume() {
        super.onResume()
        if (pendingStartServiceAfterOverlay) {
            pendingStartServiceAfterOverlay = false
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || android.provider.Settings.canDrawOverlays(this)) {
                startLocationService()
            }
        }
    }

    private fun ensureOverlayAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle("Display Over Other Apps")
                .setMessage("Allow this app to show a floating widget when the app is closed, so you can tap it to reopen.")
                .setPositiveButton("Open Settings") { _, _ ->
                    pendingStartServiceAfterOverlay = true
                    val intent = Intent(
                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
                .setNegativeButton("Skip") { _, _ ->
                    // Continue without floating widget; service will still run normally
                    startLocationService()
                }
                .show()
            return
        }
        startLocationService()
    }

    private fun startLocationService() {
        Intent(this, LocationService::class.java).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
            } else {
                startService(it)
            }
        }
        isServiceRunning = true
        updateButtonLabel()
    }

    private fun stopLocationService() {
        Intent(this, LocationService::class.java).also {
            stopService(it)
        }
        isServiceRunning = false
        updateButtonLabel()
    }

    private fun updateButtonLabel() {
        if (::btnStart.isInitialized) {
            btnStart.text = if (isServiceRunning) {
                "Stop Location Service"
            } else {
                "Start Location Service"
            }
        }
    }
}