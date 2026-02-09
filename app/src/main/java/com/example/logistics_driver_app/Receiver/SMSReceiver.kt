package com.example.logistics_driver_app.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.logistics_driver_app.Listener.OTPAutoReadListener
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern

/**
 * SMSReceiver - BroadcastReceiver for automatic OTP reading.
 * Uses SMS Retriever API to automatically read OTP from SMS.
 */
class SMSReceiver : BroadcastReceiver() {
    
    private var otpListener: OTPAutoReadListener? = null
    
    companion object {
        private const val OTP_PATTERN = "\\d{4}" // 4-digit OTP pattern
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == SmsRetriever.SMS_RETRIEVED_ACTION) {
            val extras: Bundle? = intent.extras
            val status: Status? = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status
            
            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    // Get SMS message
                    val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
                    message?.let {
                        val otp = extractOTP(it)
                        if (otp != null) {
                            otpListener?.onOTPReceived(otp)
                        }
                    }
                }
                CommonStatusCodes.TIMEOUT -> {
                    otpListener?.onOTPTimeout()
                }
            }
        }
    }
    
    /**
     * Extract OTP from SMS message using regex.
     * @param message SMS message text
     * @return Extracted OTP or null if not found
     */
    private fun extractOTP(message: String): String? {
        val pattern = Pattern.compile(OTP_PATTERN)
        val matcher = pattern.matcher(message)
        
        return if (matcher.find()) {
            matcher.group(0)
        } else {
            null
        }
    }
    
    /**
     * Set OTP listener to receive callbacks.
     * @param listener OTP auto-read listener
     */
    fun setOTPListener(listener: OTPAutoReadListener) {
        this.otpListener = listener
    }
    
    /**
     * Remove OTP listener.
     */
    fun removeOTPListener() {
        this.otpListener = null
    }
}
