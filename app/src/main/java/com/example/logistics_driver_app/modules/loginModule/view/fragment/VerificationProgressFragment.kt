package com.example.logistics_driver_app.modules.loginModule.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentVerificationProgressBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment

/**
 * VerificationProgressFragment - Shows verification status after submission.
 * Displays estimated verification time and next steps.
 */
class VerificationProgressFragment : BaseFragment<FragmentVerificationProgressBinding>() {
    
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVerificationProgressBinding {
        return FragmentVerificationProgressBinding.inflate(inflater, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
    }
    
    private fun setupViews() {
        binding.apply {
            // Contact Support
            btnContactSupport.setOnClickListener {
                // TODO: Open contact support (phone/email)
                // For now, just show a message
                android.widget.Toast.makeText(
                    requireContext(),
                    "Contact Support: +91-1234567890",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            
            // Skip to Home (Demo only)
            tvSkipDemo.setOnClickListener {
                // TODO: Navigate to MainActivity or Home Screen
                // For now, just show a message
                android.widget.Toast.makeText(
                    requireContext(),
                    "Demo: Would navigate to Home Screen",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
