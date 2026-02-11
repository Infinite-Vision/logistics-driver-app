package com.example.logistics_driver_app.modules.loginModule.view.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.logistics_driver_app.Common.CommonFunctions
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.Common.util.ValidationUtil
import com.example.logistics_driver_app.Common.util.ViewUtils
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentOtpVerificationBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.loginModule.viewModel.OTPViewModel

/**
 * OTPVerificationFragment - OTP verification screen.
 * Allows users to verify OTP sent to their phone number.
 */
class OTPVerificationFragment : BaseFragment<FragmentOtpVerificationBinding>() {
    
    private val viewModel: OTPViewModel by viewModels()
    private val args: OTPVerificationFragmentArgs by navArgs()
    private var countDownTimer: CountDownTimer? = null
    private var resendEnabled = false
    private lateinit var sharedPreference: SharedPreference
    
    companion object {
        private const val OTP_TIMEOUT = 30 // seconds (as per requirements)
    }
    
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOtpVerificationBinding {
        return FragmentOtpVerificationBinding.inflate(inflater, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sharedPreference = SharedPreference.getInstance(requireContext())
        setupViews()
        observeViewModel()
        startOTPTimer()
    }
    
    private fun setupViews() {
        binding.apply {
            // Back button
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }
            
            // Display phone number
            val maskedNumber = CommonFunctions.maskPhoneNumber(args.phoneNumber)
            tvPhoneNumber.text = maskedNumber

            val otpEditTexts = listOf(etOTP1, etOTP2, etOTP3, etOTP4)
            otpEditTexts.forEachIndexed { index, editText ->
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (s?.length == 1 && index < otpEditTexts.size - 1) {
                            otpEditTexts[index + 1].requestFocus()
                        }
                        updateVerifyButton(getOtpFromFields())
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })
            }
            
            // Verify button
            btnVerify.setOnClickListener {
                android.util.Log.d("OTPVerificationFragment", "[NAVIGATION_FLOW] ===== VERIFY BUTTON CLICKED =====")
                val otp = getOtpFromFields()
                android.util.Log.d("OTPVerificationFragment", "[NAVIGATION_FLOW] OTP entered: ****")
                if (ValidationUtil.isValidOTP(otp)) {
                    ViewUtils.hideKeyboard(requireContext(), requireView())
                    // Get language code from SharedPreference and convert to uppercase for API
                    val languageCode = (sharedPreference.getLanguage() ?: "en").uppercase()
                    android.util.Log.d("OTPVerificationFragment", "[NAVIGATION_FLOW] Language: $languageCode")
                    android.util.Log.d("OTPVerificationFragment", "[NAVIGATION_FLOW] Calling viewModel.verifyOTP()...")
                    // TODO: Get country code from UI (for now using +91)
                    viewModel.verifyOTP("+91", args.phoneNumber, otp, languageCode)
                } else {
                    android.util.Log.w("OTPVerificationFragment", "[NAVIGATION_FLOW] Invalid OTP format")
                    Bakery.showToast(requireContext(), getString(R.string.error_invalid_otp))
                }
            }
            
            // Resend OTP
            tvResendTimer.setOnClickListener {
                if (resendEnabled) {
                    // TODO: Get country code from UI (for now using +91)
                    viewModel.resendOTP("+91", args.phoneNumber)
                    resendEnabled = false
                    startOTPTimer()
                }
            }
        }
    }
    
    private fun getOtpFromFields(): String {
        return binding.run { 
            etOTP1.text.toString() + etOTP2.text.toString() + etOTP3.text.toString() + etOTP4.text.toString()
        } 
    }
    
    private fun getOTPFromFields(): String {
        binding.apply {
            return "${etOTP1.text}${etOTP2.text}${etOTP3.text}${etOTP4.text}"
        }
    }
    
    private fun updateVerifyButton(otp: String) {
        binding.apply {
            if (ValidationUtil.isValidOTP(otp)) {
                ViewUtils.enable(btnVerify)
            } else {
                ViewUtils.disable(btnVerify)
            }
        }
    }
    
    private fun startOTPTimer() {
        binding.apply {
            tvResendTimer.isEnabled = false
            tvResendTimer.alpha = 0.5f
        }
        
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer((OTP_TIMEOUT * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                binding.tvResendTimer.text = getString(
                    R.string.resend_code,
                    CommonFunctions.formatTime(secondsRemaining)
                )
            }
            
            override fun onFinish() {
                binding.apply {
                    tvResendTimer.text = getString(R.string.resend_otp)
                    tvResendTimer.isEnabled = true
                    tvResendTimer.alpha = 1.0f
                    resendEnabled = true
                }
            }
        }.start()
    }
    
    private fun showSuccessScreen() {
        binding.apply {
            // Hide main content
            clMainContent.visibility = View.GONE
            
            // Show success overlay
            clSuccessOverlay.visibility = View.VISIBLE
            
            // Set phone number in success screen
            val maskedNumber = CommonFunctions.maskPhoneNumber(args.phoneNumber)
            tvSuccessPhoneNumber.text = maskedNumber
        }
    }
    
    private fun observeViewModel() {
        android.util.Log.d("OTPVerificationFragment", "[OBSERVER] ===== SETTING UP OBSERVERS =====")
        android.util.Log.d("OTPVerificationFragment", "[OBSERVER] Current lifecycle state: ${viewLifecycleOwner.lifecycle.currentState}")
        
        // Observe app state next screen for proper routing
        viewModel.nextScreen.observe(viewLifecycleOwner, Observer { nextScreen ->
            android.util.Log.d("OTPVerificationFragment", "[OBSERVER] nextScreen observer triggered: '$nextScreen'")
            
            if (!nextScreen.isNullOrEmpty()) {
                android.util.Log.d("OTPVerificationFragment", "[NAVIGATION] NextScreen received: $nextScreen")
                
                // Show success overlay and navigate after 3 seconds
                showSuccessScreen()
                
                binding.root.postDelayed({
                    android.util.Log.d("OTPVerificationFragment", "[NAVIGATION] Executing navigation to: $nextScreen")
                    navigateToScreen(nextScreen)
                }, 3000) // 3 seconds delay
            } else {
                android.util.Log.w("OTPVerificationFragment", "[OBSERVER] nextScreen is null or empty, checking verificationSuccess")
                // If nextScreen is empty but verification succeeded, use fallback
                if (viewModel.verificationSuccess.value == true) {
                    android.util.Log.w("OTPVerificationFragment", "[FALLBACK] Using fallback navigation to Owner Details")
                    showSuccessScreen()
                    binding.root.postDelayed({
                        navigateToOwnerDetails()
                    }, 3000)
                }
            }
        })
        
        viewModel.verificationSuccess.observe(viewLifecycleOwner, Observer { success ->
            android.util.Log.d("OTPVerificationFragment", "[OBSERVER] ===== verificationSuccess TRIGGERED =====")
            android.util.Log.d("OTPVerificationFragment", "[OBSERVER] verificationSuccess value: $success")
            android.util.Log.d("OTPVerificationFragment", "[OBSERVER] Current nextScreen value: ${viewModel.nextScreen.value}")
            if (success) {
                android.util.Log.d("OTPVerificationFragment", "[SUCCESS] ===== OTP VERIFIED SUCCESSFULLY =====")
                android.util.Log.d("OTPVerificationFragment", "[SUCCESS] Waiting for nextScreen value from app state API...")
                android.util.Log.d("OTPVerificationFragment", "[SUCCESS] Token saved: ${sharedPreference.getSessionToken()?.take(15)}...")
                // Navigation handled by nextScreen observer above
            } else {
                android.util.Log.e("OTPVerificationFragment", "[ERROR] OTP verification FAILED")
            }
        })
        
        viewModel.onboardingStatus.observe(viewLifecycleOwner, Observer { status ->
            // Keep for backward compatibility if app state call fails
            android.util.Log.d("OTPVerificationFragment", "[STATUS] Onboarding status: $status")
        })
        
        viewModel.onboardingStep.observe(viewLifecycleOwner, Observer { step ->
            // Keep for backward compatibility if app state call fails
            android.util.Log.d("OTPVerificationFragment", "[STATUS] Onboarding step: $step")
        })
        
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMsg ->
            errorMsg?.let {
                Bakery.showToast(requireContext(), it)
                // TODO: Add shake animation for OTP input fields on error
            }
        })
        
        viewModel.resendSuccess.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                Bakery.showToast(requireContext(), getString(R.string.otp_sent_success))
            }
        })
        
        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.apply {
                btnVerify.isEnabled = !isLoading
                if (isLoading) {
                    ViewUtils.disable(btnVerify)
                } else {
                    val otp = getOTPFromFields()
                    updateVerifyButton(otp)
                }
            }
        })
        
        viewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                Bakery.showToast(requireContext(), it)
            }
        })
    }
    
    /**
     * Navigate to screen based on app state nextScreen value.
     * Possible values: OWNER_DETAILS, VEHICLE_DETAILS, DRIVER_DETAILS, HOME, verification_in_progress
     */
    private fun navigateToScreen(nextScreen: String) {
        android.util.Log.d("OTPVerificationFragment", "[NAVIGATION] Navigating to: $nextScreen")
        
        val screenKey = nextScreen.uppercase().replace("_", "")
        
        when (screenKey) {
            "OWNERDETAILS", "OWNER" -> {
                android.util.Log.d("OTPVerificationFragment", "[NAVIGATION] -> Owner Details")
                navigateToOwnerDetails()
            }
            "VEHICLEDETAILS", "VEHICLE" -> {
                android.util.Log.d("OTPVerificationFragment", "[NAVIGATION] -> Vehicle Details")
                navigateToVehicleDetails()
            }
            "DRIVERDETAILS", "DRIVER" -> {
                android.util.Log.d("OTPVerificationFragment", "[NAVIGATION] -> Driver Details")
                navigateToDriverDetails()
            }
            "VERIFICATIONINPROGRESS", "VERIFICATION", "PENDING" -> {
                android.util.Log.d("OTPVerificationFragment", "[NAVIGATION] -> Verification Progress (24-48 hrs pending)")
                navigateToVerificationProgress()
            }
            "HOME", "DASHBOARD" -> {
                android.util.Log.d("OTPVerificationFragment", "[NAVIGATION] -> Home (not implemented)")
                // TODO: Navigate to main app home/dashboard
                navigateToOwnerDetails() // Fallback for now
            }
            else -> {
                android.util.Log.w("OTPVerificationFragment", "[NAVIGATION] Unknown screen: $nextScreen, defaulting to Owner Details")
                navigateToOwnerDetails()
            }
        }
    }
    
    private fun navigateBasedOnStep(step: String) {
        when (step) {
            "OWNER" -> navigateToOwnerDetails()
            "VEHICLE" -> navigateToVehicleDetails()
            "DRIVER" -> navigateToDriverDetails()
            else -> navigateToOwnerDetails()
        }
    }
    
    private fun navigateToOwnerDetails() {
        val action = OTPVerificationFragmentDirections
            .actionOtpVerificationToOwnerDetails()
        findNavController().navigate(action)
    }
    
    private fun navigateToVehicleDetails() {
        try {
            val action = OTPVerificationFragmentDirections
                .actionOtpVerificationToVehicleDetails()
            findNavController().navigate(action)
        } catch (e: Exception) {
            android.util.Log.e("OTPVerificationFragment", "Navigation to Vehicle Details failed", e)
            // Fallback to Owner Details if navigation fails
            navigateToOwnerDetails()
        }
    }
    
    private fun navigateToDriverDetails() {
        val action = OTPVerificationFragmentDirections
            .actionOtpVerificationToOwnerDetails()  // Updated to go to Owner Details first
        findNavController().navigate(action)
    }
    
    private fun navigateToVerificationProgress() {
        android.util.Log.d("OTPVerificationFragment", "[NAVIGATION] Navigating to Verification Progress screen")
        try {
            val action = OTPVerificationFragmentDirections
                .actionOtpVerificationToVerificationProgress()
            findNavController().navigate(action)
        } catch (e: Exception) {
            android.util.Log.e("OTPVerificationFragment", "Navigation to Verification Progress failed", e)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}
