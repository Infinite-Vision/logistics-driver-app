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
                requireActivity().onBackPressed()
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
                val otp = getOtpFromFields()
                if (ValidationUtil.isValidOTP(otp)) {
                    ViewUtils.hideKeyboard(requireContext(), requireView())
                    // Get language code from SharedPreference and convert to uppercase for API
                    val languageCode = (sharedPreference.getLanguage() ?: "en").uppercase()
                    // TODO: Get country code from UI (for now using +91)
                    viewModel.verifyOTP("+91", args.phoneNumber, otp, languageCode)
                } else {
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
        viewModel.verificationSuccess.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                // Show success overlay for 3 seconds
                showSuccessScreen()
                
                // Wait 3 seconds then proceed
                binding.root.postDelayed({
                    // Navigation handled by onboardingStatus observer
                    viewModel.onboardingStatus.value?.let { status ->
                        if (status == "IN_PROGRESS") {
                            viewModel.onboardingStep.value?.let { step ->
                                navigateBasedOnStep(step)
                            }
                        } else if (status == "COMPLETED") {
                            navigateToDriverDetails()
                        }
                    }
                }, 3000) // 3 seconds delay
            }
        })
        
        viewModel.onboardingStatus.observe(viewLifecycleOwner, Observer { status ->
            // Route based on onboarding status
            if (status == "IN_PROGRESS") {
                // Check onboarding step
                viewModel.onboardingStep.value?.let { step ->
                    navigateBasedOnStep(step)
                }
            } else if (status == "COMPLETED") {
                // If onboarding is complete, navigate to main app (future)
                navigateToDriverDetails() // For now, still go to driver details
            }
        })
        
        viewModel.onboardingStep.observe(viewLifecycleOwner, Observer { step ->
            // This will be triggered after status is set
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
        // TODO: Navigate once VehicleDetails action is created
        navigateToOwnerDetails() // Fallback for now
    }
    
    private fun navigateToDriverDetails() {
        val action = OTPVerificationFragmentDirections
            .actionOtpVerificationToOwnerDetails()  // Updated to go to Owner Details first
        findNavController().navigate(action)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}
