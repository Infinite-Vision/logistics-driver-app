package com.example.logistics_driver_app.modules.loginModule.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.logistics_driver_app.Common.util.ConnectivityUtil

/**
 * BaseActivity - Base class for all activities in the application.
 * Provides common functionality like connectivity checking, lifecycle management.
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: VB
    protected lateinit var connectivityUtil: ConnectivityUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        setContentView(binding.root)
        
        connectivityUtil = ConnectivityUtil(this)
        
        initialize()
        setupObservers()
        setupListeners()
    }

    /**
     * Get view binding instance for the activity.
     * Must be implemented by child activities.
     * @return ViewBinding instance
     */
    abstract fun getViewBinding(): VB

    /**
     * Initialize activity components, setup initial data.
     * Override in child activities for initialization logic.
     */
    protected open fun initialize() {}

    /**
     * Setup LiveData observers and data binding.
     * Override in child activities to observe ViewModels.
     */
    protected open fun setupObservers() {}

    /**
     * Setup click listeners and UI interactions.
     * Override in child activities for listener setup.
     */
    protected open fun setupListeners() {}

    /**
     * Check if network is available.
     * @return True if connected to network
     */
    protected fun isNetworkAvailable(): Boolean {
        return connectivityUtil.isConnected()
    }

    /**
     * Show progress indicator.
     * Override to show custom loading UI.
     */
    protected open fun showLoading() {}

    /**
     * Hide progress indicator.
     * Override to hide custom loading UI.
     */
    protected open fun hideLoading() {}
}
