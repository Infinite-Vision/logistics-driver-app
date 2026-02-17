package com.example.locationtracker

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.location.Geocoder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.locationtracker.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

// LocationService.kt
class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val channelId = "location_tracking_channel"

    /** Interval after which we reschedule to keep the service alive (restart if killed). */
    private val heartbeatIntervalMs = 4 * 60 * 1000L
    private val heartbeatRequestCode = 100

    private var floatingView: View? = null
    private var windowManager: WindowManager? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialX = 0
    private var initialY = 0
    private var moved = false

    private val openAppChannelId = "open_app_channel"
    private val openAppNotificationId = 2

    // Keep a single callback instance so we can properly remove updates
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let {
                val lat = it.latitude
                val lon = it.longitude

                Log.d("LOCATION_UPDATE", "$lat, $lon")
                //showToast("LOCATION_UPDATE = $lat, $lon")

                // Also resolve a human-readable address for easier understanding
                resolveAndLogAddress(lat, lon)
                // TODO: Save to database or send to API here
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Active")
            .setContentText("Tracking location in the background...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
        requestLocationUpdates()
        showFloatingWidget()
        scheduleHeartbeatAlarm()

        return START_STICKY
    }

    private fun showFloatingWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            return
        }
        if (floatingView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget, null).apply {
            isClickable = true
            isFocusable = true
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        overlayParams?.let { initialX = it.x; initialY = it.y }
                        moved = false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY
                        if (kotlin.math.abs(dx) > 15 || kotlin.math.abs(dy) > 15) moved = true
                        overlayParams?.let { params ->
                            params.x = initialX + dx.toInt()
                            params.y = initialY + dy.toInt()
                            windowManager?.updateViewLayout(this, params)
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!moved) {
                            removeFloatingWidget()
                            openAppFromBackground()
                        }
                    }
                }
                true
            }
        }
        overlayParams?.let { windowManager?.addView(floatingView, it) }
    }

    private fun removeFloatingWidget() {
        try {
            floatingView?.let { windowManager?.removeView(it) }
        } catch (e: Exception) {
            Log.e("LocationService", "Error removing floating widget", e)
        }
        floatingView = null
        windowManager = null
        overlayParams = null
    }

    /**
     * Open the app when user taps the floating widget. On Android 10+ background
     * activity start is restricted, so we use a full-screen intent via notification.
     */
    private fun openAppFromBackground() {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            startActivity(openIntent)
            return
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        createOpenAppChannel()
        val notification = NotificationCompat.Builder(this, openAppChannelId)
            .setContentTitle("Location Tracker")
            .setContentText("Tap to open app")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(openAppNotificationId, notification)

        Handler(Looper.getMainLooper()).postDelayed({
            nm.cancel(openAppNotificationId)
        }, 2000)
    }

    private fun createOpenAppChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                openAppChannelId,
                "Open App",
                NotificationManager.IMPORTANCE_HIGH
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    /**
     * Ensure the service restarts itself if the user removes the app
     * from the recent apps screen.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        scheduleRestartAlarm(1000L)
    }

    /** Schedules a one-shot alarm to restart the service (e.g. after task removed). */
    private fun scheduleRestartAlarm(delayMs: Long) {
        val restartIntent = Intent(applicationContext, LocationService::class.java).apply {
            `package` = applicationContext.packageName
        }
        val pending = PendingIntent.getService(
            applicationContext,
            1,
            restartIntent,
            PendingIntent.FLAG_ONE_SHOT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayMs, pending)
    }

    /**
     * Schedules a recurring "heartbeat" alarm using setAlarmClock (highest priority;
     * least likely to be killed by OEMs). When it fires, the keep-alive receiver
     * starts this service again, so we survive even if the process is killed.
     */
    private fun scheduleHeartbeatAlarm() {
        val intent = Intent(this, LocationServiceKeepAliveReceiver::class.java).apply {
            action = LocationServiceKeepAliveReceiver.ACTION_KEEP_ALIVE
            setPackage(packageName)
        }
        val pending = PendingIntent.getBroadcast(
            this,
            heartbeatRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + heartbeatIntervalMs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, null), pending)
        } else {
            @Suppress("DEPRECATION")
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    private fun cancelHeartbeatAlarm() {
        val intent = Intent(this, LocationServiceKeepAliveReceiver::class.java).apply {
            action = LocationServiceKeepAliveReceiver.ACTION_KEEP_ALIVE
            setPackage(packageName)
        }
        val pending = PendingIntent.getBroadcast(
            this,
            heartbeatRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
    }

    private fun requestLocationUpdates() {
        // Request high-accuracy GPS-based updates, tuned for better precision
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateIntervalMillis(2_000L)        // try for updates as often as every 2s
            .setGranularity(Granularity.GRANULARITY_FINE) // fine (GPS-level) granularity
            .setWaitForAccurateLocation(true)          // wait for a more accurate first fix
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        }
    }

    /**
     * Reverse-geocode the latitude/longitude into a readable address and log it
     * alongside LOCATION_UPDATE. Runs on a background thread to avoid blocking
     * the main thread.
     */
    private fun resolveAndLogAddress(lat: Double, lon: Double) {
        Thread {
            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                val results = geocoder.getFromLocation(lat, lon, 1)
                val addressLine = results?.firstOrNull()?.getAddressLine(0)

                if (!addressLine.isNullOrEmpty()) {
                    Log.d("LOCATION_UPDATE", "Address: $addressLine")
                    showToast("LOCATION_UPDATE Address := $addressLine")
                } else {
                    Log.d("LOCATION_UPDATE", "Address: (not available for $lat, $lon)")
                    showToast("LOCATION_UPDATE Address not available")
                }
            } catch (e: Exception) {
                Log.e("LOCATION_UPDATE", "Failed to resolve address for $lat, $lon", e)
            }
        }.start()
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                this@LocationService,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelHeartbeatAlarm()
        removeFloatingWidget()
        // Remove foreground notification and stop being a foreground service
        stopForeground(STOP_FOREGROUND_REMOVE)
        // Cleanly stop location updates when the service is destroyed
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}