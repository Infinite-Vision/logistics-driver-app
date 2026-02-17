package com.example.locationtracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * Receives heartbeat alarms and (re)starts LocationService so the service
 * keeps running even when the app is killed on aggressive devices.
 */
class LocationServiceKeepAliveReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_KEEP_ALIVE) return
        Log.d(TAG, "Keep-alive: starting LocationService")
        val serviceIntent = Intent(context, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    companion object {
        const val ACTION_KEEP_ALIVE = "com.example.locationtracker.ACTION_KEEP_ALIVE"
        private const val TAG = "LocationKeepAlive"
    }
}
