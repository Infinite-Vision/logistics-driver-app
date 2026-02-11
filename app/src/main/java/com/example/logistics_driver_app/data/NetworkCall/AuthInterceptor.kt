package com.example.logistics_driver_app.data.NetworkCall

import android.content.Context
import com.example.logistics_driver_app.Common.util.SharedPreference
import okhttp3.Interceptor
import okhttp3.Response

/**
 * AuthInterceptor - Automatically adds JWT token to API requests.
 * Intercepts all outgoing requests and adds Authorization header if token exists.
 */
class AuthInterceptor(private val context: Context) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val sharedPreference = SharedPreference.getInstance(context)
        val token = sharedPreference.getSessionToken()
        
        // If token exists and request doesn't have Authorization header, add it
        val newRequest = if (token.isNotEmpty() && 
            originalRequest.header("Authorization") == null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(newRequest)
    }
}
