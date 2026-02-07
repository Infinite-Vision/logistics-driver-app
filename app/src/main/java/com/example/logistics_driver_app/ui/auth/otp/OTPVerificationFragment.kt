package com.example.logistics_driver_app.ui.auth.otp

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentOtpVerificationBinding
import com.example.logistics_driver_app.utils.CommonUtils
import com.example.logistics_driver_app.utils.Constants
import com.example.logistics_driver_app.utils.ValidationUtils

/**
 * Fragment for OTP verification screen.
 * Allows users to enter the 4-digit OTP sent to their phone number.
 */
class OTPVerificationFragment : Fragment() {

    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: OTPViewModel
    private var phoneNumber: String = ""
    private var sentOTP: String = ""
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[OTPViewModel::class.java]

        // Get arguments
        phoneNumber = arguments?.getString("phoneNumber") ?: ""
        sentOTP = arguments?.getString("otp") ?: ""

        setupUI()
        setupOTPInputs()
        setupObservers()
        setupClickListeners()
        startResendTimer()
    }

    /**
     * Setup UI with phone number display.
     */
    private fun setupUI() {
        binding.tvPhoneNumber.text = CommonUtils.capitalizeWords(
            ValidationUtils.formatPhoneNumber(phoneNumber)
        )
        
        // Set OTP in ViewModel for verification
        viewModel.setExpectedOTP(sentOTP)
    }

    /**
     * Setup OTP input fields with auto-focus behavior.
     */
    private fun setupOTPInputs() {
        val otpFields = listOf(
            binding.etOTP1,
            binding.etOTP2,
            binding.etOTP3,
            binding.etOTP4
        )

        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s != null && s.length == 1 && index < otpFields.size - 1) {
                        // Move to next field
                        otpFields[index + 1].requestFocus()
                    }
                    
                    // Update ViewModel with current OTP
                    val otp = getEnteredOTP()
                    viewModel.updateEnteredOTP(otp)
                }

                override fun afterTextChanged(s: Editable?) {
                    // No action needed here
                }
            })

            // Handle backspace
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (editText.text.isNullOrEmpty() && index > 0) {
                        // Move to previous field
                        otpFields[index - 1].requestFocus()
                        otpFields[index - 1].setText("")
                    }
                }
                false
            }
        }

        // Focus first field
        otpFields[0].requestFocus()
        CommonUtils.showKeyboard(requireContext(), otpFields[0])
    }

    /**
     * Get the entered OTP from all fields.
     * @return Concatenated OTP string
     */
    private fun getEnteredOTP(): String {
        return binding.etOTP1.text.toString() +
                binding.etOTP2.text.toString() +
                binding.etOTP3.text.toString() +
                binding.etOTP4.text.toString()
    }

    /**
     * Setup LiveData observers.
     */
    private fun setupObservers() {
        viewModel.otpVerified.observe(viewLifecycleOwner) { verified ->
            if (verified) {
                CommonUtils.showToast(
                    requireContext(),
                    getString(R.string.otp_verified_success)
                )
                // Save auth session
                viewModel.saveAuthSession(requireContext(), phoneNumber)
                navigateToDriverDetails()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                CommonUtils.showToast(requireContext(), error)
                clearOTPFields()
            }
        }
    }

    /**
     * Setup click listeners for UI elements.
     */
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvChange.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnVerify.setOnClickListener {
            val enteredOTP = getEnteredOTP()
            viewModel.verifyOTP(enteredOTP)
        }

        binding.tvResendTimer.setOnClickListener {
            if (viewModel.canResendOTP.value == true) {
                resendOTP()
            }
        }
    }

    /**
     * Start the countdown timer for OTP resend.
     */
    private fun startResendTimer() {
        countDownTimer?.cancel()
        viewModel.setCanResendOTP(false)

        countDownTimer = object : CountDownTimer(
            Constants.OTP_RESEND_TIME * 1000L,
            1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                binding.tvResendTimer.text = getString(
                    R.string.resend_code,
                    CommonUtils.formatTime(secondsRemaining)
                )
            }

            override fun onFinish() {
                binding.tvResendTimer.text = getString(R.string.resend_otp)
                viewModel.setCanResendOTP(true)
            }
        }.start()
    }

    /**
     * Resend OTP to the user.
     */
    private fun resendOTP() {
        // Generate new OTP
        val newOTP = CommonUtils.generateOTP()
        sentOTP = newOTP
        viewModel.setExpectedOTP(newOTP)
        
        android.util.Log.d("OTPVerification", "Resent OTP: $newOTP")
        
        CommonUtils.showToast(
            requireContext(),
            getString(R.string.otp_sent_success)
        )
        
        clearOTPFields()
        startResendTimer()
    }

    /**
     * Clear all OTP input fields.
     */
    private fun clearOTPFields() {
        binding.etOTP1.setText("")
        binding.etOTP2.setText("")
        binding.etOTP3.setText("")
        binding.etOTP4.setText("")
        binding.etOTP1.requestFocus()
    }

    /**
     * Navigate to driver details screen.
     */
    private fun navigateToDriverDetails() {
        findNavController().navigate(R.id.action_otpVerification_to_driverDetails)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }
}
