package com.example.logistics_driver_app.Common.util

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar

/**
 * Bakery - Utility class for displaying UI messages.
 * Provides toast, snackbar, and dialogue helpers (like a bakery serves treats!).
 */
object Bakery {
    
    /**
     * Show a short toast message.
     * @param context Context
     * @param message Message to display
     */
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Show a long toast message.
     * @param context Context
     * @param message Message to display
     */
    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Show a snackbar with short duration.
     * @param view View to attach snackbar to
     * @param message Message to display
     */
    fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }
    
    /**
     * Show a snackbar with long duration.
     * @param view View to attach snackbar to
     * @param message Message to display
     */
    fun showLongSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }
    
    /**
     * Show a snackbar with action button.
     * @param view View to attach snackbar to
     * @param message Message to display
     * @param actionText Action button text
     * @param action Action to perform on button click
     */
    fun showSnackbarWithAction(
        view: View,
        message: String,
        actionText: String,
        action: () -> Unit
    ) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setAction(actionText) { action() }
            .show()
    }
    
    /**
     * Show a simple alert dialog.
     * @param context Context
     * @param title Dialog title
     * @param message Dialog message
     * @param positiveButtonText Positive button text
     * @param onPositiveClick Action on positive button click
     */
    fun showAlertDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String = "OK",
        onPositiveClick: () -> Unit = {}
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                onPositiveClick()
                dialog.dismiss()
            }
            .show()
    }
    
    /**
     * Show a confirmation dialog with positive and negative buttons.
     * @param context Context
     * @param title Dialog title
     * @param message Dialog message
     * @param positiveButtonText Positive button text
     * @param negativeButtonText Negative button text
     * @param onPositiveClick Action on positive button click
     * @param onNegativeClick Action on negative button click
     */
    fun showConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String = "Yes",
        negativeButtonText: String = "No",
        onPositiveClick: () -> Unit = {},
        onNegativeClick: () -> Unit = {}
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                onPositiveClick()
                dialog.dismiss()
            }
            .setNegativeButton(negativeButtonText) { dialog, _ ->
                onNegativeClick()
                dialog.dismiss()
            }
            .show()
    }
    
    /**
     * Create a progress dialog.
     * @param context Context
     * @param message Loading message
     * @return Dialog instance
     */
    fun createProgressDialog(context: Context, message: String = "Loading..."): Dialog {
        return AlertDialog.Builder(context)
            .setMessage(message)
            .setCancelable(false)
            .create()
    }
}
