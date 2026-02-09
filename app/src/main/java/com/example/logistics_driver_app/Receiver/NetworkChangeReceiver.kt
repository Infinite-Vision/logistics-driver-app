package com.example.logistics_driver_app.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import com.example.logistics_driver_app.Listener.NetworkConnectionListener

/**
 * NetworkChangeReceiver - BroadcastReceiver for monitoring network connectivity changes.
 * Notifies registered listeners when network state changes.
 */
class NetworkChangeReceiver : BroadcastReceiver() {
    
    private var listener: NetworkConnectionListener? = null
    private var wasConnected: Boolean = false
    
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val isConnected = isNetworkConnected(it)
            
            if (isConnected != wasConnected) {
                if (isConnected) {
                    listener?.onNetworkConnected()
                    
                    // Check network type
                    val isWifi = isWifiConnected(it)
                    listener?.onNetworkTypeChanged(isWifi)
                } else {
                    listener?.onNetworkDisconnected()
                }
                
                wasConnected = isConnected
            }
        }
    }
    
    /**
     * Check if device is connected to internet.
     * @param context Context
     * @return True if connected
     */
    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network: Network? = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            
            return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }
    
    /**
     * Check if connected to Wi-Fi.
     * @param context Context
     * @return True if connected to Wi-Fi
     */
    private fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network: Network? = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            
            return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo?.type == ConnectivityManager.TYPE_WIFI
        }
    }
    
    /**
     * Set network connection listener.
     * @param listener Network connection listener
     */
    fun setListener(listener: NetworkConnectionListener) {
        this.listener = listener
    }
    
    /**
     * Remove network connection listener.
     */
    fun removeListener() {
        this.listener = null
    }
}
