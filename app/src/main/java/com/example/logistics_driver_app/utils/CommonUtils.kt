package com.example.logistics_driver_app.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

/**
 * Common utility functions for UI operations and general helpers.
 */
object CommonUtils {
    
    /**
     * Show a toast message.
     * @param context Context
     * @param message Message to display
     * @param duration Toast duration (default: SHORT)
     */
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }
    
    /**
     * Show a snackbar message.
     * @param view View to attach snackbar to
     * @param message Message to display
     * @param duration Snackbar duration (default: SHORT)
     */
    fun showSnackbar(view: View, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(view, message, duration).show()
    }
    
    /**
     * Hide soft keyboard.
     * @param context Context
     * @param view Current focused view
     */
    fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    
    /**
     * Show soft keyboard.
     * @param context Context
     * @param view View to focus
     */
    fun showKeyboard(context: Context, view: View) {
        view.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
    
    /**
     * Format timestamp to readable date string.
     * @param timestamp Timestamp in milliseconds
     * @param pattern Date format pattern (default: dd MMM yyyy)
     * @return Formatted date string
     */
    fun formatDate(timestamp: Long, pattern: String = "dd MMM yyyy"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    /**
     * Format timestamp to readable date and time string.
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date-time string
     */
    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    /**
     * Check if string is null or empty.
     * @param text String to check
     * @return True if null or empty
     */
    fun isNullOrEmpty(text: String?): Boolean {
        return text.isNullOrEmpty() || text.trim().isEmpty()
    }
    
    /**
     * Capitalize first letter of each word.
     * @param text Text to capitalize
     * @return Capitalized text
     */
    fun capitalizeWords(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) 
                else it.toString() 
            }
        }
    }
    
    /**
     * Generate a random OTP for testing (since no API).
     * @return Random 4-digit OTP
     */
    fun generateOTP(): String {
        return (1000..9999).random().toString()
    }
    
    /**
     * Get time remaining in MM:SS format.
     * @param seconds Total seconds
     * @return Formatted time string
     */
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
    }
}
