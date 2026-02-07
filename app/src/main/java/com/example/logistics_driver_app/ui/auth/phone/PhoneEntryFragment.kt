package com.example.logistics_driver_app.ui.auth.phone

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentPhoneEntryBinding
import com.example.logistics_driver_app.utils.CommonUtils

/**
 * Fragment for phone number entry screen.
 * Allows users to enter their mobile number for OTP verification.
 */
class PhoneEntryFragment : Fragment() {

    private var _binding: FragmentPhoneEntryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PhoneViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[PhoneViewModel::class.java]

        setupUI()
        setupObservers()
        setupClickListeners()
    }

    /**
     * Setup UI components and input validation.
     */
    private fun setupUI() {
        // Add text watcher for real-time validation
        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updatePhoneNumber(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /**
     * Setup LiveData observers.
     */
    private fun setupObservers() {
        viewModel.phoneNumber.observe(viewLifecycleOwner) { phone ->
            // Enable/disable button based on validation
            viewModel.isPhoneValid.observe(viewLifecycleOwner) { isValid ->
                binding.btnSendOTP.isEnabled = isValid
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                CommonUtils.showToast(requireContext(), error)
            }
        }

        viewModel.otpSent.observe(viewLifecycleOwner) { sent ->
            if (sent) {
                CommonUtils.showToast(
                    requireContext(),
                    getString(R.string.otp_sent_success)
                )
                navigateToOTPVerification()
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

        binding.btnSendOTP.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString()
            if (viewModel.validateAndSendOTP(phoneNumber)) {
                // OTP sent, observe otpSent LiveData
            } else {
                CommonUtils.showToast(
                    requireContext(),
                    viewModel.errorMessage.value ?: getString(R.string.error_invalid_phone)
                )
            }
        }
    }

    /**
     * Navigate to OTP verification screen.
     */
    private fun navigateToOTPVerification() {
        val bundle = Bundle().apply {
            putString("phoneNumber", viewModel.phoneNumber.value)
            putString("otp", viewModel.generatedOTP.value)
        }
        findNavController().navigate(R.id.action_phoneEntry_to_otpVerification, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
