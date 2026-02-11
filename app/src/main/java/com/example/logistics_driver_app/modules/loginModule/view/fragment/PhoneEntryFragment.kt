package com.example.logistics_driver_app.modules.loginModule.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.Common.util.ValidationUtil
import com.example.logistics_driver_app.Common.util.ViewUtils
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentPhoneEntryBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.loginModule.viewModel.PhoneViewModel

/**
 * PhoneEntryFragment - Phone number entry screen.
 * Allows users to enter their phone number for OTP verification.
 */
class PhoneEntryFragment : BaseFragment<FragmentPhoneEntryBinding>() {
    
    private val viewModel: PhoneViewModel by viewModels()
    
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPhoneEntryBinding {
        return FragmentPhoneEntryBinding.inflate(inflater, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        observeViewModel()
    }
    
    private fun setupViews() {
        binding.apply {
            // Disable Login button by default
            ViewUtils.disable(btnSendOTP)
            
            // Back button
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }
            
            // Setup legal text with clickable links
            setupLegalText()
            
            // Setup help text
            setupHelpText()
            
            etPhoneNumber.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updateContinueButton(s.toString())
                }
                
                override fun afterTextChanged(s: Editable?) {}
            })
            
            btnSendOTP.setOnClickListener {
                val phoneNumber = etPhoneNumber.text.toString().trim()
                if (ValidationUtil.isValidPhoneNumber(phoneNumber)) {
                    ViewUtils.hideKeyboard(requireContext(), etPhoneNumber)
                    // TODO: Get country code from UI selector (for now using +91)
                    viewModel.sendOTP("+91", phoneNumber)
                } else {
                    Bakery.showToast(requireContext(), getString(R.string.error_invalid_phone))
                }
            }
        }
    }
    
    private fun updateContinueButton(phoneNumber: String) {
        binding.apply {
            if (ValidationUtil.isValidPhoneNumber(phoneNumber)) {
                ViewUtils.enable(btnSendOTP)
            } else {
                ViewUtils.disable(btnSendOTP)
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.otpSent.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                val phoneNumber = binding.etPhoneNumber.text.toString().trim()
                navigateToOTPVerification(phoneNumber)
            }
        })
        
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMsg ->
            errorMsg?.let {
                Bakery.showToast(requireContext(), it)
            }
        })
        
        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.apply {
                btnSendOTP.isEnabled = !isLoading
                if (isLoading) {
                    ViewUtils.disable(btnSendOTP)
                } else {
                    val phoneNumber = etPhoneNumber.text.toString().trim()
                    updateContinueButton(phoneNumber)
                }
            }
        })
        
        viewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                Bakery.showToast(requireContext(), it)
            }
        })
    }
    
    private fun setupLegalText() {
        val fullText = "By clicking Login, you agree to our Terms and Conditions, Privacy Policy and TDS Declaration"
        val spannableString = SpannableString(fullText)
        
        val primaryColor = requireContext().getColor(R.color.primary)
        
        // Terms and Conditions
        val termsStart = fullText.indexOf("Terms and Conditions")
        val termsEnd = termsStart + "Terms and Conditions".length
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // TODO: Open Terms and Conditions
                    Bakery.showToast(requireContext(), "Terms and Conditions")
                }
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = primaryColor
                    ds.isUnderlineText = false
                }
            },
            termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // Privacy Policy
        val privacyStart = fullText.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // TODO: Open Privacy Policy
                    Bakery.showToast(requireContext(), "Privacy Policy")
                }
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = primaryColor
                    ds.isUnderlineText = false
                }
            },
            privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // TDS Declaration
        val tdsStart = fullText.indexOf("TDS Declaration")
        val tdsEnd = tdsStart + "TDS Declaration".length
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // TODO: Open TDS Declaration
                    Bakery.showToast(requireContext(), "TDS Declaration")
                }
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = primaryColor
                    ds.isUnderlineText = false
                }
            },
            tdsStart, tdsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        binding.tvLegalText.text = spannableString
        binding.tvLegalText.movementMethod = LinkMovementMethod.getInstance()
        binding.tvLegalText.highlightColor = Color.TRANSPARENT
    }
    
    private fun setupHelpText() {
        val fullText = "Need help? Contact Support"
        val spannableString = SpannableString(fullText)
        
        val primaryColor = requireContext().getColor(R.color.primary)
        
        // Contact Support
        val contactStart = fullText.indexOf("Contact Support")
        val contactEnd = contactStart + "Contact Support".length
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // TODO: Open Contact Support
                    Bakery.showToast(requireContext(), "Contact Support")
                }
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = primaryColor
                    ds.isUnderlineText = false
                }
            },
            contactStart, contactEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        binding.tvHelp.text = spannableString
        binding.tvHelp.movementMethod = LinkMovementMethod.getInstance()
        binding.tvHelp.highlightColor = Color.TRANSPARENT
    }
    
    private fun navigateToOTPVerification(phoneNumber: String) {
        val action = PhoneEntryFragmentDirections
            .actionPhoneEntryToOtpVerification(phoneNumber)
        findNavController().navigate(action)
    }
}
