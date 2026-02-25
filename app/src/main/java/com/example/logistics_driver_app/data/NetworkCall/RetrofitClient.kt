package com.example.logistics_driver_app.data.NetworkCall

import android.content.Context
import android.util.Log
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

    private const val TAG = "OkHttp"
    private const val BASE_URL = "https://f3m8w0mx-8080.inc1.devtunnels.ms/api/v1/"
    private const val TIMEOUT = 30L

    private var retrofit: Retrofit? = null
    private var context: Context? = null

    /**
     * Initialize RetrofitClient with application context.
     * Must be called once on app startup (Application class) so every API call
     * has a context and the AuthInterceptor is always attached.
     */
    fun initialize(appContext: Context) {
        context = appContext.applicationContext
        retrofit = null // Force rebuild with new context
    }

    /**
     * Get configured Retrofit instance.
     * Lazily creates a singleton; call initialize() first to attach the AuthInterceptor.
     */
    private fun getRetrofitInstance(): Retrofit {
        if (retrofit == null) {
            // Route OkHttp logs through android.util.Log so they appear in logcat
            val loggingInterceptor = HttpLoggingInterceptor { message ->
                Log.d(TAG, message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val builder = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)

            context?.let { builder.addInterceptor(AuthInterceptor(it)) }
                ?: Log.e(TAG, "[WARN] RetrofitClient has no context — AuthInterceptor skipped. " +
                        "Call RetrofitClient.initialize(context) on app startup.")

            val gson = GsonBuilder().setLenient().create()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }

    /**
     * Get API service. Optionally pass a context to ensure AuthInterceptor
     * is attached even the first time (useful when called before initialize()).
     */
    fun getApiService(ctx: Context? = null): ApiService {
        if (ctx != null && context == null) {
            initialize(ctx)
        }
        return getRetrofitInstance().create(ApiService::class.java)
    }
}
