package com.example.logistics_driver_app.modules.loginModule.view.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.Common.util.ValidationUtil
import com.example.logistics_driver_app.Common.util.ViewUtils
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentDriverDetailsBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.loginModule.viewModel.DriverDetailsViewModel

/**
 * DriverDetailsFragment - Driver details entry screen.
 * Allows users to enter their name, vehicle number, and license number.
 */
class DriverDetailsFragment : BaseFragment<FragmentDriverDetailsBinding>() {

    private val viewModel: DriverDetailsViewModel by viewModels()
    private val args: DriverDetailsFragmentArgs by navArgs()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDriverDetailsBinding {
        return FragmentDriverDetailsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.apply {
            // Text watchers for validation
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    validateInputs()
                }

                override fun afterTextChanged(s: Editable?) {}
            }

            etName.addTextChangedListener(textWatcher)
            etVehicleNumber.addTextChangedListener(textWatcher)
            etLicenseNumber.addTextChangedListener(textWatcher)

            // Submit button
            btnSubmit.setOnClickListener {
                submitDriverDetails()
            }
        }
    }

    private fun validateInputs() {
        binding.apply {
            val name = etName.text.toString().trim()
            val vehicleNumber = etVehicleNumber.text.toString().trim()
            val licenseNumber = etLicenseNumber.text.toString().trim()

            val isValid = ValidationUtil.isValidName(name) &&
                    vehicleNumber.isNotEmpty() &&
                    licenseNumber.isNotEmpty()

            if (isValid) {
                ViewUtils.enable(btnSubmit)
            } else {
                ViewUtils.disable(btnSubmit)
            }
        }
    }

    private fun submitDriverDetails() {
        binding.apply {
            val name = etName.text.toString().trim()
            val vehicleNumber = etVehicleNumber.text.toString().trim().uppercase()
            val licenseNumber = etLicenseNumber.text.toString().trim().uppercase()

            // Validate name
            if (!ValidationUtil.isValidName(name)) {
                tilName.error = getString(R.string.error_invalid_name)
                return
            } else {
                tilName.error = null
            }

            // Validate vehicle number
            if (vehicleNumber.isEmpty()) {
                tilVehicleNumber.error = getString(R.string.vehicle_number_required)
                return
            } else {
                tilVehicleNumber.error = null
            }

            // Validate license number
            if (licenseNumber.isEmpty()) {
                tilLicenseNumber.error = getString(R.string.license_number_required)
                return
            } else {
                tilLicenseNumber.error = null
            }

            ViewUtils.hideKeyboard(requireContext(), btnSubmit)
            viewModel.saveDriverDetails(args.phoneNumber, name, vehicleNumber, licenseNumber)
        }
    }

    private fun observeViewModel() {
        viewModel.saveSuccess.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                Bakery.showToast(requireContext(), getString(R.string.registration_successful))
                navigateToMainScreen()
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.apply {
                btnSubmit.isEnabled = !isLoading
                if (isLoading) {
                    ViewUtils.disable(btnSubmit)
                } else {
                    validateInputs()
                }
            }
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                Bakery.showToast(requireContext(), it)
            }
        })
    }

    private fun navigateToMainScreen() {
        // Navigate to main screen (home/dashboard)
        // For now, just finish the activity
        requireActivity().finish()
    }
}
