package com.example.logistics_driver_app.data.NetworkCall

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitClient - Singleton for Retrofit instance management.
 * Configures HTTP client, logging, and converters for API calls.
 */
object RetrofitClient {
    
    private const val BASE_URL = "https://hyperactively-florescent-addilyn.ngrok-free.dev/api/v1/"
    private const val TIMEOUT = 30L
    
    private var retrofit: Retrofit? = null
    
    /**
     * Get configured Retrofit instance.
     * Creates singleton instance with OkHttp and Gson configuration.
     * @return Retrofit instance
     */
    fun getRetrofitInstance(): Retrofit {
        if (retrofit == null) {
            // Logging interceptor for debugging network calls
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            // OkHttp client with interceptors and timeouts
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build()
            
            // Gson for JSON serialization/deserialization
            val gson = GsonBuilder()
                .setLenient()
                .create()
            
            // Build Retrofit instance
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        
        return retrofit!!
    }
    
    /**
     * Get API service instance for making network calls.
     * @return ApiService implementation
     */
    fun getApiService(): ApiService {
        return getRetrofitInstance().create(ApiService::class.java)
    }
}
