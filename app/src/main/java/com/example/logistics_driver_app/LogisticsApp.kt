package com.example.logistics_driver_app

import android.app.Application
import android.util.Log
import com.example.logistics_driver_app.data.NetworkCall.RetrofitClient
import com.example.logistics_driver_app.data.service.DriverLocationService

/**
 * Application class — runs before any Activity or Fragment.
 * Initialises RetrofitClient with a Context so the AuthInterceptor is
 * always attached regardless of which screen the app opens on.
 */
class LogisticsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("LogisticsApp", "[INIT] Application onCreate — initialising RetrofitClient")
        RetrofitClient.initialize(this)
        Log.d("LogisticsApp", "[INIT] RetrofitClient initialised")
        // Always reset online flag on fresh process start.
        // If the app was force-killed, onDestroy() of DriverLocationService may
        // not have run, leaving driver_is_online=true stale in SharedPreferences.
        DriverLocationService.resetOnlineState(this)
        Log.d("LogisticsApp", "[INIT] Driver online state reset to OFFLINE")
    }
}
