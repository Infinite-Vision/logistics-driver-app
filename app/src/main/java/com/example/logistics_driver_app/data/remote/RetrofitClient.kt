package com.example.logistics_driver_app.data.remote

import com.example.logistics_driver_app.utils.Constants
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client singleton for API communication.
 * Configures OkHttp client with interceptors and timeout settings.
 * Currently prepared for future API integration.
 */
object RetrofitClient {
    
    private var retrofit: Retrofit? = null
    
    /**
     * Get configured Retrofit instance.
     * @return Retrofit instance
     */
    fun getRetrofitInstance(): Retrofit {
        if (retrofit == null) {
            // Logging interceptor for debugging
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            // OkHttp client configuration
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(Constants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.API_TIMEOUT, TimeUnit.SECONDS)
                .build()
            
            // Gson configuration
            val gson = GsonBuilder()
                .setLenient()
                .create()
            
            // Retrofit instance
            retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        
        return retrofit!!
    }
    
    /**
     * Get API service instance.
     * @return ApiService instance
     */
    fun getApiService(): ApiService {
        return getRetrofitInstance().create(ApiService::class.java)
    }
}
