package com.example.logistics_driver_app.ui.driver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentDriverDetailsBinding
import com.example.logistics_driver_app.utils.CommonUtils
import com.google.android.material.textfield.TextInputLayout

/**
 * Fragment for driver details entry screen.
 * Allows drivers to enter and save their personal and vehicle information.
 */
class DriverDetailsFragment : Fragment() {

    private var _binding: FragmentDriverDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DriverDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriverDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val factory = DriverDetailsViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[DriverDetailsViewModel::class.java]

        setupObservers()
        setupClickListeners()
        loadExistingData()
    }

    /**
     * Setup LiveData observers.
     */
    private fun setupObservers() {
        viewModel.driverSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                CommonUtils.showToast(
                    requireContext(),
                    getString(R.string.profile_saved_success)
                )
                navigateToHome()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                CommonUtils.showToast(requireContext(), error)
            }
        }

        viewModel.validationErrors.observe(viewLifecycleOwner) { errors ->
            clearErrors()
            errors.forEach { (field, message) ->
                showFieldError(field, message)
            }
        }
    }

    /**
     * Setup click listeners for UI elements.
     */
    private fun setupClickListeners() {
        binding.btnSubmit.setOnClickListener {
            saveDriverDetails()
        }
    }

    /**
     * Load existing driver data if available.
     */
    private fun loadExistingData() {
        viewModel.existingDriver.observe(viewLifecycleOwner) { driver ->
            driver?.let {
                binding.etName.setText(it.name)
                binding.etEmail.setText(it.email)
                binding.etAddress.setText(it.address)
                binding.etCity.setText(it.city)
                binding.etState.setText(it.state)
                binding.etPincode.setText(it.pincode)
                binding.etVehicleNumber.setText(it.vehicleNumber)
                binding.etVehicleType.setText(it.vehicleType)
                binding.etLicenseNumber.setText(it.licenseNumber)
            }
        }
    }

    /**
     * Collect and validate driver details, then save.
     */
    private fun saveDriverDetails() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val city = binding.etCity.text.toString().trim()
        val state = binding.etState.text.toString().trim()
        val pincode = binding.etPincode.text.toString().trim()
        val vehicleNumber = binding.etVehicleNumber.text.toString().trim()
        val vehicleType = binding.etVehicleType.text.toString().trim()
        val licenseNumber = binding.etLicenseNumber.text.toString().trim()

        viewModel.saveDriverDetails(
            name = name,
            email = email,
            address = address,
            city = city,
            state = state,
            pincode = pincode,
            vehicleNumber = vehicleNumber,
            vehicleType = vehicleType,
            licenseNumber = licenseNumber
        )
    }

    /**
     * Show validation error for a specific field.
     * @param field Field name with error
     * @param message Error message
     */
    private fun showFieldError(field: String, message: String) {
        val textInputLayout: TextInputLayout? = when (field) {
            "name" -> binding.tilName
            "email" -> binding.tilEmail
            "pincode" -> binding.tilPincode
            "vehicleNumber" -> binding.tilVehicleNumber
            "licenseNumber" -> binding.tilLicenseNumber
            else -> null
        }
        textInputLayout?.error = message
    }

    /**
     * Clear all field errors.
     */
    private fun clearErrors() {
        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilPincode.error = null
        binding.tilVehicleNumber.error = null
        binding.tilLicenseNumber.error = null
    }

    /**
     * Navigate to home screen after successful save.
     */
    private fun navigateToHome() {
        // For now, just finish or show success
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
