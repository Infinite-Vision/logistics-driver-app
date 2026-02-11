package com.example.logistics_driver_app.modules.loginModule.view.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.Common.util.ViewUtils
import com.example.logistics_driver_app.Common.util.S3UploadUtil
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentOwnerDetailsBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.loginModule.viewModel.OnboardingViewModel
import com.example.logistics_driver_app.modules.loginModule.viewModel.AppStateViewModel
import kotlinx.coroutines.launch

/**
 * OwnerDetailsFragment - Owner details form (Step 1 of 3).
 * Collects owner name and document uploads (Aadhaar, PAN, Selfie).
 * Uploads images to AWS S3 and saves the URLs.
 */
class OwnerDetailsFragment : BaseFragment<FragmentOwnerDetailsBinding>() {
    
    private companion object {
        const val TAG = "OwnerDetailsFragment"
    }
    
    private lateinit var sharedPreference: SharedPreference
    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private val appStateViewModel: AppStateViewModel by viewModels()
    
    private var aadhaarUri: Uri? = null
    private var panUri: Uri? = null
    private var selfieUri: Uri? = null
    
    // Flag to prevent multiple navigation attempts
    private var hasNavigated = false
    
    private val aadhaarPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            aadhaarUri = it
            binding.ivAadhaarPreview.visibility = View.VISIBLE
            binding.ivAadhaarPreview.setImageURI(it)
            binding.btnUploadAadhaar.text = "Change"
            Bakery.showToast(requireContext(), "Aadhaar uploaded")
            updateContinueButton()
        }
    }
    
    private val panPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            panUri = it
            binding.ivPANPreview.visibility = View.VISIBLE
            binding.ivPANPreview.setImageURI(it)
            binding.btnUploadPAN.text = "Change"
            Bakery.showToast(requireContext(), "PAN uploaded")
            updateContinueButton()
        }
    }
    
    private val selfiePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selfieUri = it
            binding.ivSelfiePreview.visibility = View.VISIBLE
            binding.ivSelfiePreview.setImageURI(it)
            binding.btnUploadSelfie.text = "Change"
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
        // Initialize sharedPreference BEFORE super.onViewCreated() because
        // BaseFragment calls setupObservers() in super.onViewCreated()
        sharedPreference = SharedPreference.getInstance(requireContext())
        
        // Reset navigation flag
        hasNavigated = false
        
        super.onViewCreated(view, savedInstanceState)
        // NOTE: Do NOT call setupObservers() here - BaseFragment already calls it
        
        Log.d(TAG, "=== onViewCreated ===")
        setupViews()
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "=== onResume ===")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "=== onPause ===")
    }
    
    override fun onDestroyView() {
        Log.d(TAG, "=== onDestroyView ===")
        super.onDestroyView()
    }
    
    override fun onDestroy() {
        Log.d(TAG, "=== onDestroy ===")
        super.onDestroy()
    }
    
    private fun setupViews() {
        binding.apply {
            // Back button
            btnBack.setOnClickListener {
                findNavController().navigateUp()
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
                Log.d(TAG, "[USER ACTION] ========== Continue button clicked ==========")
                Log.d(TAG, "[DEBUG] Current hasNavigated flag: $hasNavigated")
                
                if (hasNavigated) {
                    Log.w(TAG, "[USER ACTION] Already navigating/navigated, ignoring click")
                    return@setOnClickListener
                }
                
                if (validateForm()) {
                    Log.d(TAG, "[VALIDATION] Form validation passed")
                    uploadToS3AndProceed()
                } else {
                    Log.w(TAG, "[VALIDATION] Form validation failed")
                }
            }
            
            // Text change listener for name - real-time validation
            etName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                
                override fun afterTextChanged(s: Editable?) {
                    updateContinueButton()
                }
            })
        }
    }
    
    override fun setupObservers() {
        Log.d(TAG, "=== Setting up observers ===")
        Log.d(TAG, "[DEBUG] hasNavigated flag: $hasNavigated")
        
        // Observe owner save success
        onboardingViewModel.ownerSaveSuccess.observe(viewLifecycleOwner) { success ->
            Log.d(TAG, "[OBSERVER] ownerSaveSuccess triggered: $success, hasNavigated: $hasNavigated")
            
            // Prevent multiple executions
            if (hasNavigated) {
                Log.w(TAG, "[OBSERVER] Already navigated, ignoring ownerSaveSuccess")
                return@observe
            }
            
            if (success) {
                Log.d(TAG, "[FLOW] Step 1: Owner details saved successfully")
                Log.d(TAG, "[FLOW] Step 2: Fetching app state...")
                try {
                    // After owner is saved, get app state to determine next screen
                    appStateViewModel.getAppState()
                    Log.d(TAG, "[FLOW] Step 3: getAppState() called successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "[ERROR] Failed to call getAppState()", e)
                    if (isAdded && view != null) {
                        binding.progressBar.visibility = View.GONE
                        ViewUtils.enable(binding.btnContinue)
                        Bakery.showToast(requireContext(), "Error: ${e.message}")
                    }
                }
            } else {
                Log.w(TAG, "[OBSERVER] ownerSaveSuccess is false, not proceeding")
            }
        }
        
        // Observe app state next screen
        appStateViewModel.nextScreen.observe(viewLifecycleOwner) { nextScreen ->
            Log.d(TAG, "[OBSERVER] nextScreen triggered: '$nextScreen', hasNavigated: $hasNavigated")
            
            // Prevent multiple navigation attempts
            if (hasNavigated) {
                Log.w(TAG, "[NAVIGATION] Already navigated, ignoring nextScreen event")
                return@observe
            }
            
            if (nextScreen.isNullOrEmpty()) {
                Log.w(TAG, "[WARNING] Next screen is null or empty, ignoring...")
                return@observe
            }
            
            // Check if fragment is in valid state for navigation
            if (!isAdded || isDetached || isRemoving) {
                Log.e(TAG, "[NAVIGATION] Fragment not in valid state - isAdded: $isAdded, isDetached: $isDetached, isRemoving: $isRemoving")
                return@observe
            }
            
            // Check activity state
            if (activity == null || activity?.isFinishing == true) {
                Log.e(TAG, "[NAVIGATION] Activity invalid or finishing")
                return@observe
            }
            
            Log.d(TAG, "[FLOW] Step 4: Received next screen: $nextScreen")
            
            // Mark that we're about to navigate
            hasNavigated = true
            Log.d(TAG, "[DEBUG] Set hasNavigated = true")
            
            // Hide loader
            binding.progressBar.visibility = View.GONE
            
            // Determine destination action
            val screenKey = nextScreen.lowercase().replace("_", "")
            Log.d(TAG, "[NAVIGATION] Normalized screen key: $screenKey")
            
            val action = when {
                screenKey.contains("vehicle") -> {
                    Log.d(TAG, "[NAVIGATION] Navigating to Vehicle Details")
                    OwnerDetailsFragmentDirections.actionOwnerDetailsToVehicleDetails()
                }
                screenKey.contains("driver") -> {
                    Log.d(TAG, "[NAVIGATION] Navigating to Driver Details")
                    OwnerDetailsFragmentDirections.actionOwnerDetailsToDriverDetails()
                }
                screenKey.contains("verification") -> {
                    Log.d(TAG, "[NAVIGATION] Navigating to Verification Progress")
                    OwnerDetailsFragmentDirections.actionOwnerDetailsToVerificationProgress()
                }
                else -> {
                    Log.w(TAG, "[WARNING] Unknown screen: $nextScreen, defaulting to vehicle details")
                    OwnerDetailsFragmentDirections.actionOwnerDetailsToVehicleDetails()
                }
            }
            
            // Navigate
            try {
                findNavController().navigate(action)
                Log.d(TAG, "[NAVIGATION] Navigation executed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "[ERROR] Navigation failed", e)
                e.printStackTrace()
                hasNavigated = false
                
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    ViewUtils.enable(binding.btnContinue)
                    Bakery.showToast(requireContext(), "Navigation failed: ${e.message}")
                }
            }
        }
        
        // Observe errors
        onboardingViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            Log.d(TAG, "[OBSERVER] errorMessage triggered: $error")
            error?.let {
                Log.e(TAG, "[ERROR] Onboarding error: $it")
                if (isAdded && view != null) {
                    binding.progressBar.visibility = View.GONE
                    ViewUtils.enable(binding.btnContinue)
                    try {
                        Bakery.showToast(requireContext(), it)
                    } catch (e: Exception) {
                        Log.e(TAG, "[ERROR] Cannot show error toast", e)
                    }
                }
            }
        }
        
        appStateViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            Log.d(TAG, "[OBSERVER] appState errorMessage triggered: $error")
            error?.let {
                Log.e(TAG, "[ERROR] App state error: $it")
                if (isAdded && view != null) {
                    binding.progressBar.visibility = View.GONE
                    ViewUtils.enable(binding.btnContinue)
                    try {
                        Bakery.showToast(requireContext(), it)
                    } catch (e: Exception) {
                        Log.e(TAG, "[ERROR] Cannot show error toast", e)
                    }
                }
            }
        }
        
        Log.d(TAG, "=== All observers setup complete ===")
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
    
    /**
     * Upload images to S3 and proceed to next screen
     */
    private fun uploadToS3AndProceed() {
        binding.apply {
            Log.d(TAG, "[UPLOAD] ========== Starting upload process ==========")
            
            try {
                // Show loader
                progressBar.visibility = View.VISIBLE
                ViewUtils.disable(btnContinue)
                
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        Log.d(TAG, "[UPLOAD] Coroutine launched, uploading to S3...")
                        
                        // Check if fragment is still alive before proceeding
                        if (!isAdded) {
                            Log.e(TAG, "[UPLOAD] Fragment not added, aborting upload")
                            return@launch
                        }
                        
                        // Upload Aadhaar
                        Log.d(TAG, "[UPLOAD] Uploading Aadhaar...")
                        val aadhaarUrl = aadhaarUri?.let {
                            S3UploadUtil.uploadFile(requireContext(), it, "documents/aadhaar")
                        } ?: ""
                        Log.d(TAG, "[UPLOAD] \u2713 Aadhaar uploaded: $aadhaarUrl")
                        
                        // Check again after async operation
                        if (!isAdded) {
                            Log.e(TAG, "[UPLOAD] Fragment destroyed during aadhaar upload")
                            return@launch
                        }
                        
                        // Upload PAN
                        Log.d(TAG, "[UPLOAD] Uploading PAN...")
                        val panUrl = panUri?.let {
                            S3UploadUtil.uploadFile(requireContext(), it, "documents/pan")
                        } ?: ""
                        Log.d(TAG, "[UPLOAD] \u2713 PAN uploaded: $panUrl")
                        
                        // Check again
                        if (!isAdded) {
                            Log.e(TAG, "[UPLOAD] Fragment destroyed during PAN upload")
                            return@launch
                        }
                        
                        // Upload Selfie
                        Log.d(TAG, "[UPLOAD] Uploading Selfie...")
                        val selfieUrl = selfieUri?.let {
                            S3UploadUtil.uploadFile(requireContext(), it, "documents/selfie")
                        } ?: ""
                        Log.d(TAG, "[UPLOAD] ✓ Selfie uploaded: $selfieUrl")
                        
                        Log.d(TAG, "[UPLOAD] ✓✓✓ ALL S3 UPLOADS COMPLETED ✓✓✓")
                        Log.d(TAG, "[UPLOAD] Aadhaar URL: $aadhaarUrl")
                        Log.d(TAG, "[UPLOAD] PAN URL: $panUrl")
                        Log.d(TAG, "[UPLOAD] Selfie URL: $selfieUrl")
                        
                        // Final check before proceeding
                        if (!isAdded) {
                            Log.e(TAG, "[UPLOAD] Fragment destroyed after S3 upload, aborting API call")
                            return@launch
                        }
                        
                        // Save URLs locally
                        Log.d(TAG, "[SAVE] Saving data locally...")
                        saveDataLocally(aadhaarUrl, panUrl, selfieUrl)
                        Log.d(TAG, "[SAVE] ✓ Data saved locally")
                        
                        // Call API to save owner details
                        val name = etName.text.toString().trim()
                        Log.d(TAG, "[API] Preparing to submit owner details...")
                        Log.d(TAG, "[API] Owner name: $name")
                        Log.d(TAG, "[API] Selfie URL: $selfieUrl")
                        Log.d(TAG, "[API] Aadhaar URL: $aadhaarUrl")
                        Log.d(TAG, "[API] PAN URL: $panUrl")
                        
                        // Show success notification
                        showTopNotification("Documents uploaded successfully")
                        
                        val token = sharedPreference.getSessionToken()
                        Log.d(TAG, "[API] Token length: ${token.length} chars")
                        Log.d(TAG, "[API] Token exists: ${token.isNotEmpty()}")
                        
                        if (!isAdded) {
                            Log.e(TAG, "[API] Fragment destroyed before API call")
                            return@launch
                        }
                        
                        Log.d(TAG, "[API] Calling saveOwner()...")
                        onboardingViewModel.saveOwner(
                            name = name,
                            ownerSelfieUrl = selfieUrl,
                            ownerAdharUrl = aadhaarUrl,
                            ownerPanUrl = panUrl
                        )
                        Log.d(TAG, "[API] saveOwner() invoked, waiting for observer callbacks...")
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "[ERROR] ✗✗✗ Exception during upload process ✗✗✗", e)
                        Log.e(TAG, "[ERROR] Exception type: ${e.javaClass.simpleName}")
                        Log.e(TAG, "[ERROR] Exception message: ${e.message}")
                        Log.e(TAG, "[ERROR] Stack trace:")
                        e.printStackTrace()
                        
                        if (!isAdded) {
                            Log.e(TAG, "[ERROR] Fragment destroyed, cannot update UI")
                            return@launch
                        }
                        
                        // Hide loader
                        progressBar.visibility = View.GONE
                        ViewUtils.enable(btnContinue)
                        
                        try {
                            Bakery.showToast(requireContext(), "Upload failed: ${e.message}")
                        } catch (ex: Exception) {
                            Log.e(TAG, "[ERROR] Cannot show error toast - context unavailable", ex)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "[ERROR] ✗✗✗ Exception starting upload process ✗✗✗", e)
                e.printStackTrace()
                
                if (isAdded) {
                    progressBar.visibility = View.GONE
                    ViewUtils.enable(btnContinue)
                }
            }
        }
    }
    
    private fun saveDataLocally(aadhaarUrl: String, panUrl: String, selfieUrl: String) {
        binding.apply {
            val name = etName.text.toString().trim()
            
            // Save to SharedPreferences with S3 URLs
            sharedPreference.saveOwnerName(name)
            sharedPreference.saveOwnerAadhaarUri(aadhaarUrl)
            sharedPreference.saveOwnerPANUri(panUrl)
            sharedPreference.saveOwnerSelfieUri(selfieUrl)
        }
    }
    
    // Navigation methods no longer needed - direct navigation in observer
    private fun navigateToVehicleDetails() {
        Log.d(TAG, "[DEPRECATED] This method is no longer used")
    }
    
    private fun navigateToDriverDetails() {
        Log.d(TAG, "[DEPRECATED] This method is no longer used")
    }
    
    private fun navigateToVerificationProgress() {
        Log.d(TAG, "[DEPRECATED] This method is no longer used")
    }
    
    private fun showTopNotification(message: String) {
        if (!isAdded) {
            Log.w(TAG, "[NOTIFICATION] Cannot show notification - Fragment not added")
            return
        }
        
        try {
            // Create a custom toast at the top right
            val toast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
            toast.setGravity(android.view.Gravity.TOP or android.view.Gravity.END, 32, 150)
            toast.show()
            Log.d(TAG, "[NOTIFICATION] Shown: $message")
        } catch (e: Exception) {
            Log.e(TAG, "[NOTIFICATION] Failed to show notification", e)
        }
    }
}
