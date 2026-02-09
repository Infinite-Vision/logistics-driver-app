package com.example.logistics_driver_app.Common.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * ViewUtils - Utility class for common view operations.
 * Provides helper functions for keyboard management and UI operations.
 */
object ViewUtils {
    
    /**
     * Hide soft keyboard from view.
     * @param context Context
     * @param view Current focused view
     */
    fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    
    /**
     * Show soft keyboard on view.
     * @param context Context
     * @param view View to focus and show keyboard
     */
    fun showKeyboard(context: Context, view: View) {
        view.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
    
    /**
     * Toggle keyboard visibility.
     * @param context Context
     */
    fun toggleKeyboard(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
    
    /**
     * Set view visibility to VISIBLE.
     * @param view View to show
     */
    fun show(view: View) {
        view.visibility = View.VISIBLE
    }
    
    /**
     * Set view visibility to GONE.
     * @param view View to hide
     */
    fun hide(view: View) {
        view.visibility = View.GONE
    }
    
    /**
     * Set view visibility to INVISIBLE.
     * @param view View to make invisible
     */
    fun invisible(view: View) {
        view.visibility = View.INVISIBLE
    }
    
    /**
     * Enable view and set alpha to 1.0.
     * @param view View to enable
     */
    fun enable(view: View) {
        view.isEnabled = true
        view.alpha = 1.0f
    }
    
    /**
     * Disable view and set alpha to 0.5.
     * @param view View to disable
     */
    fun disable(view: View) {
        view.isEnabled = false
        view.alpha = 0.5f
    }
}
