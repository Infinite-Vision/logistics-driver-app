package com.example.logistics_driver_app.modules.loginModule.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.logistics_driver_app.Common.util.ConnectivityUtil

/**
 * BaseFragment - Base class for all fragments in the application.
 * Provides common functionality and reduces boilerplate code.
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    protected var _binding: VB? = null
    protected val binding get() = _binding!!
    protected lateinit var connectivityUtil: ConnectivityUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        connectivityUtil = ConnectivityUtil(requireContext())
        
        initialize()
        setupObservers()
        setupListeners()
    }

    /**
     * Get view binding instance for the fragment.
     * Must be implemented by child fragments.
     * @param inflater LayoutInflater for inflating views
     * @param container Parent view container
     * @return ViewBinding instance
     */
    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    /**
     * Initialize fragment components, setup initial data.
     * Override in child fragments for initialization logic.
     */
    protected open fun initialize() {}

    /**
     * Setup LiveData observers and data binding.
     * Override in child fragments to observe ViewModels.
     */
    protected open fun setupObservers() {}

    /**
     * Setup click listeners and UI interactions.
     * Override in child fragments for listener setup.
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
