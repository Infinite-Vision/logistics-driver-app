package com.example.logistics_driver_app.Common.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.ClientConfiguration
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Utility class for uploading files to AWS S3
 */
object S3UploadUtil {
    
    private const val TAG = "S3UploadUtil"
    private const val AWS_ACCESS_KEY = "REDACTED_ACCESS_KEY"
    private const val AWS_SECRET_KEY = "REDACTED_SECRET_KEY"
    private const val S3_BUCKET_NAME = "fleet-services-app"
    private const val S3_REGION_NAME = "ap-south-1" // Mumbai region
    
    private lateinit var s3Client: AmazonS3Client
    private lateinit var transferUtility: TransferUtility
    
    /**
     * Initialize S3 client (call this once in Application class or before first upload)
     */
    fun initialize(context: Context) {
        try {
            val credentials = BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)
            
            // Configure with extended timeouts for slow networks
            val clientConfig = ClientConfiguration().apply {
                connectionTimeout = 120_000 // 120 seconds
                socketTimeout = 120_000 // 120 seconds
                maxErrorRetry = 3
            }
            
            s3Client = AmazonS3Client(credentials, clientConfig)
            s3Client.setRegion(Region.getRegion(Regions.AP_SOUTH_1))
            
            transferUtility = TransferUtility.builder()
                .context(context.applicationContext)
                .s3Client(s3Client)
                .build()
            Log.d(TAG, "S3 client initialized successfully with extended timeouts (120s)")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing S3 client", e)
        }
    }
    
    /**
     * Upload a file to S3 and return the URL
     * @param context Android context
     * @param uri Uri of the file to upload
     * @param folderPath Folder path in S3 (e.g., "documents/aadhaar")
     * @return The S3 URL of the uploaded file
     */
    suspend fun uploadFile(
        context: Context,
        uri: Uri,
        folderPath: String
    ): String = suspendCoroutine { continuation ->
        var isResumed = false // Flag to prevent double resume
        
        try {
            // Initialize if not already done
            if (!::transferUtility.isInitialized) {
                initialize(context)
            }
            
            // Convert Uri to File
            val file = uriToFile(context, uri)
            
            // Generate unique filename
            val extension = getFileExtension(file.name)
            val fileName = "${UUID.randomUUID()}.$extension"
            val s3Key = "$folderPath/$fileName"
            
            Log.d(TAG, "Starting upload: $s3Key")
            
            // Start upload
            val uploadObserver: TransferObserver = transferUtility.upload(
                S3_BUCKET_NAME,
                s3Key,
                file
            )
            
            uploadObserver.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState?) {
                    when (state) {
                        TransferState.COMPLETED -> {
                            if (!isResumed) {
                                isResumed = true
                                val s3Url = "https://$S3_BUCKET_NAME.s3.$S3_REGION_NAME.amazonaws.com/$s3Key"
                                Log.d(TAG, "Upload completed: $s3Url")
                                
                                // Clean up temp file
                                file.delete()
                                
                                continuation.resume(s3Url)
                            }
                        }
                        TransferState.FAILED, TransferState.CANCELED -> {
                            if (!isResumed) {
                                isResumed = true
                                val errorMsg = "Upload failed or canceled: ${state?.name}"
                                Log.e(TAG, errorMsg)
                                
                                // Clean up temp file
                                file.delete()
                                
                                continuation.resumeWithException(Exception(errorMsg))
                            }
                        }
                        else -> {
                            Log.d(TAG, "Upload state: $state")
                        }
                    }
                }
                
                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    val percentage = ((bytesCurrent.toFloat() / bytesTotal.toFloat()) * 100).toInt()
                    Log.d(TAG, "Upload progress: $percentage%")
                }
                
                override fun onError(id: Int, ex: Exception?) {
                    if (!isResumed) {
                        isResumed = true
                        Log.e(TAG, "Upload error", ex)
                        
                        // Clean up temp file
                        file.delete()
                        
                        continuation.resumeWithException(ex ?: Exception("Unknown upload error"))
                    }
                }
            })
            
        } catch (e: Exception) {
            if (!isResumed) {
                isResumed = true
                Log.e(TAG, "Error initiating upload", e)
                continuation.resumeWithException(e)
            }
        }
    }
    
    /**
     * Convert Uri to File
     */
    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open file")
        
        val extension = getFileExtension(uri.lastPathSegment ?: "temp")
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.$extension")
        
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()
        
        return tempFile
    }
    
    /**
     * Get file extension from filename
     */
    private fun getFileExtension(filename: String): String {
        return filename.substringAfterLast('.', "jpg")
    }
    
    /**
     * Upload multiple files in parallel
     */
    suspend fun uploadFiles(
        context: Context,
        files: Map<String, Uri?>,
        folderPath: String
    ): Map<String, String> {
        val results = mutableMapOf<String, String>()
        
        for ((key, uri) in files) {
            if (uri != null) {
                try {
                    val url = uploadFile(context, uri, "$folderPath/$key")
                    results[key] = url
                } catch (e: Exception) {
                    Log.e(TAG, "Error uploading $key", e)
                    throw e
                }
            }
        }
        
        return results
    }
}
