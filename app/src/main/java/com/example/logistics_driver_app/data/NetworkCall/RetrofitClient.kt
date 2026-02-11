package com.example.logistics_driver_app.data.NetworkCall

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitClient - Singleton for Retrofit instance management.
 * Configures HTTP client, logging, auth interceptor, and converters for API calls.
 */
object RetrofitClient {
    
    private const val BASE_URL = "http://43.205.235.73:8080/api/v1/"
    private const val TIMEOUT = 30L
    
    private var retrofit: Retrofit? = null
    private var context: Context? = null
    
    /**
     * Initialize RetrofitClient with application context.
     * Should be called once in Application class.
     */
    fun initialize(appContext: Context) {
        context = appContext.applicationContext
        retrofit = null // Reset retrofit to rebuild with new context
    }
    
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
            val okHttpClientBuilder = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            
            // Add auth interceptor if context is available
            context?.let {
                okHttpClientBuilder.addInterceptor(AuthInterceptor(it))
            }
            
            val okHttpClient = okHttpClientBuilder.build()
            
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
