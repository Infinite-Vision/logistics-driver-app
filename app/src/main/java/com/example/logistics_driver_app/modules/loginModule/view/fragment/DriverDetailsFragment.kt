package com.example.logistics_driver_app.modules.loginModule.view.fragment

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.Common.util.ValidationUtil
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentDriverDetailsNewBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment

/**
 * DriverDetailsFragment - Driver details entry screen (Step 3/3).
 * Allows users to specify if they will drive, and if not, enter driver details.
 */
class DriverDetailsFragment : BaseFragment<FragmentDriverDetailsNewBinding>() {

    private var willDrive: Boolean = true
    private var licenseUri: Uri? = null

    // License picker
    private val licensePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            licenseUri = it
            Bakery.showToast(requireContext(), "License uploaded")
            validateForm()
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDriverDetailsNewBinding {
        return FragmentDriverDetailsNewBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupProgressSteps()
        setupViews()
    }

    private fun setupProgressSteps() {
        binding.includeSteps.apply {
            // Step 1 - Completed (show checkmark)
            vStep1Background.setBackgroundResource(R.drawable.bg_step_active)
            tvStep1Number.visibility = View.GONE
            ivStep1Checkmark.visibility = View.VISIBLE

            // Step 2 - Completed (show checkmark)
            vStep2Background.setBackgroundResource(R.drawable.bg_step_active)
            tvStep2Number.visibility = View.GONE
            ivStep2Checkmark.visibility = View.VISIBLE

            // Step 3 - Active
            vStep3Background.setBackgroundResource(R.drawable.bg_step_active)
            tvStep3Number.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        }
    }

    private fun setupViews() {
        binding.apply {
            // Yes/No radio buttons
            cardYes.setOnClickListener {
                selectOption(true)
            }

            cardNo.setOnClickListener {
                selectOption(false)
            }

            // Default: Yes selected
            selectOption(true)

            // Text watchers for validation
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    validateForm()
                }
                override fun afterTextChanged(s: Editable?) {}
            }

            etDriverName.addTextChangedListener(textWatcher)
            etDriverPhone.addTextChangedListener(textWatcher)

            // License upload
            btnUploadLicense.setOnClickListener {
                licensePickerLauncher.launch("image/*")
            }

            // Submit button
            btnSubmit.setOnClickListener {
                submitDriverDetails()
            }
        }
    }

    private fun selectOption(yesOption: Boolean) {
        willDrive = yesOption

        binding.apply {
            if (yesOption) {
                // Yes selected
                vYesIndicator.setBackgroundResource(R.drawable.bg_radio_selected)
                vNoIndicator.setBackgroundResource(R.drawable.bg_radio_unselected)

                // Hide driver details fields
                tvDriverNameLabel.visibility = View.GONE
                tvDriverNameRequired.visibility = View.GONE
                tilDriverName.visibility = View.GONE
                tvDriverPhoneLabel.visibility = View.GONE
                tvDriverPhoneRequired.visibility = View.GONE
                llPhoneNumber.visibility = View.GONE
            } else {
                // No selected
                vYesIndicator.setBackgroundResource(R.drawable.bg_radio_unselected)
                vNoIndicator.setBackgroundResource(R.drawable.bg_radio_selected)

                // Show driver details fields
                tvDriverNameLabel.visibility = View.VISIBLE
                tvDriverNameRequired.visibility = View.VISIBLE
                tilDriverName.visibility = View.VISIBLE
                tvDriverPhoneLabel.visibility = View.VISIBLE
                tvDriverPhoneRequired.visibility = View.VISIBLE
                llPhoneNumber.visibility = View.VISIBLE
            }

            validateForm()
        }
    }

    private fun validateForm(): Boolean {
        binding.apply {
            val isValid = if (willDrive) {
                // If owner will drive, just need license
                licenseUri != null
            } else {
                // If not driving, need driver name, phone, and license
                val driverName = etDriverName.text.toString().trim()
                val driverPhone = etDriverPhone.text.toString().trim()

                ValidationUtil.isValidName(driverName) &&
                        ValidationUtil.isValidPhoneNumber(driverPhone) &&
                        licenseUri != null
            }

            btnSubmit.isEnabled = isValid
            btnSubmit.alpha = if (isValid) 1.0f else 0.5f

            return isValid
        }
    }

    private fun submitDriverDetails() {
        if (!validateForm()) {
            Bakery.showToast(requireContext(), "Please fill all required fields")
            return
        }

        binding.apply {
            // Save data locally
            val sharedPref = SharedPreference.getInstance(requireContext())

            sharedPref.saveWillDrive(willDrive)

            if (willDrive) {
                // Owner will drive - clear old driver data
                sharedPref.setDriverName("")
                sharedPref.saveDriverPhone("")
            } else {
                // Save driver details
                val driverName = etDriverName.text.toString().trim()
                val driverPhone = etDriverPhone.text.toString().trim()

                sharedPref.setDriverName(driverName)
                sharedPref.saveDriverPhone(driverPhone)
            }

            // Save license URI
            licenseUri?.let {
                sharedPref.saveLicenseUri(it.toString())
            }

            Bakery.showToast(requireContext(), "Driver details saved")
            navigateToVerificationProgress()
        }
    }

    private fun navigateToVerificationProgress() {
        val action = DriverDetailsFragmentDirections
            .actionDriverDetailsToVerificationProgress()
        findNavController().navigate(action)
    }
}
