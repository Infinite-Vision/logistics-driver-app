package com.example.logistics_driver_app.modules.loginModule.view.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.Common.util.ViewUtils
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentOwnerDetailsBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment

/**
 * OwnerDetailsFragment - Owner details form (Step 1 of 3).
 * Collects owner name and document uploads (Aadhaar, PAN, Selfie).
 */
class OwnerDetailsFragment : BaseFragment<FragmentOwnerDetailsBinding>() {
    
    private lateinit var sharedPreference: SharedPreference
    private var aadhaarUri: Uri? = null
    private var panUri: Uri? = null
    private var selfieUri: Uri? = null
    
    private val aadhaarPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            aadhaarUri = it
            Bakery.showToast(requireContext(), "Aadhaar uploaded")
            updateContinueButton()
        }
    }
    
    private val panPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            panUri = it
            Bakery.showToast(requireContext(), "PAN uploaded")
            updateContinueButton()
        }
    }
    
    private val selfiePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selfieUri = it
            Bakery.showToast(requireContext(), "Selfie uploaded")
            updateContinueButton()
        }
    }
    
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOwnerDetailsBinding {
        return FragmentOwnerDetailsBinding.inflate(inflater, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sharedPreference = SharedPreference.getInstance(requireContext())
        setupViews()
    }
    
    private fun setupViews() {
        binding.apply {
            // Back button
            btnBack.setOnClickListener {
                requireActivity().onBackPressed()
            }
            
            // Upload buttons
            btnUploadAadhaar.setOnClickListener {
                aadhaarPickerLauncher.launch("image/*")
            }
            
            btnUploadPAN.setOnClickListener {
                panPickerLauncher.launch("image/*")
            }
            
            btnUploadSelfie.setOnClickListener {
                selfiePickerLauncher.launch("image/*")
            }
            
            // Continue button - initially disabled
            ViewUtils.disable(btnContinue)
            btnContinue.setOnClickListener {
                if (validateForm()) {
                    saveDataLocally()
                    navigateToVehicleDetails()
                }
            }
            
            // Text change listener for name
            etName.setOnFocusChangeListener { _, _ ->
                updateContinueButton()
            }
        }
    }
    
    private fun validateForm(): Boolean {
        binding.apply {
            val name = etName.text.toString().trim()
            
            if (name.isEmpty()) {
                Bakery.showToast(requireContext(), "Please enter owner name")
                return false
            }
            
            if (aadhaarUri == null) {
                Bakery.showToast(requireContext(), "Please upload Aadhaar card")
                return false
            }
            
            if (panUri == null) {
                Bakery.showToast(requireContext(), "Please upload PAN card")
                return false
            }
            
            if (selfieUri == null) {
                Bakery.showToast(requireContext(), "Please upload selfie")
                return false
            }
            
            return true
        }
    }
    
    private fun updateContinueButton() {
        binding.apply {
            val name = etName.text.toString().trim()
            val isComplete = name.isNotEmpty() && aadhaarUri != null && 
                           panUri != null && selfieUri != null
            
            if (isComplete) {
                ViewUtils.enable(btnContinue)
            } else {
                ViewUtils.disable(btnContinue)
            }
        }
    }
    
    private fun saveDataLocally() {
        binding.apply {
            val name = etName.text.toString().trim()
            
            // Save to SharedPreferences
            sharedPreference.saveOwnerName(name)
            sharedPreference.saveOwnerAadhaarUri(aadhaarUri.toString())
            sharedPreference.saveOwnerPANUri(panUri.toString())
            sharedPreference.saveOwnerSelfieUri(selfieUri.toString())
        }
    }
    
    private fun navigateToVehicleDetails() {
        val action = OwnerDetailsFragmentDirections
            .actionOwnerDetailsToVehicleDetails()
        findNavController().navigate(action)
    }
}
