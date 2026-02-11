package com.example.logistics_driver_app.modules.loginModule.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.Common.util.S3UploadUtil
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.Common.util.ViewUtils
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentVehicleDetailsBinding
import com.example.logistics_driver_app.databinding.IncludeProgressStepsBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.loginModule.viewModel.OnboardingViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * VehicleDetailsFragment - Vehicle details form (Step 2 of 3).
 * Collects vehicle information and dynamically shows body type selection.
 */
class VehicleDetailsFragment : BaseFragment<FragmentVehicleDetailsBinding>() {
    
    private lateinit var sharedPreference: SharedPreference
    private lateinit var viewModel: OnboardingViewModel
    private var rcUri: android.net.Uri? = null
    private var selectedVehicleType: String? = null
    private var selectedBodyType: String? = null
    
    private val rcPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            rcUri = it
            binding.apply {
                // Show image preview
                ivRCPreview.setImageURI(it)
                ivRCPreview.visibility = View.VISIBLE
            }
            Bakery.showToast(requireContext(), "RC uploaded")
            updateContinueButton()
        }
    }
    
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVehicleDetailsBinding {
        return FragmentVehicleDetailsBinding.inflate(inflater, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            super.onViewCreated(view, savedInstanceState)
            
            android.util.Log.d("VehicleDetailsFragment", "=== onViewCreated ===")
            
            sharedPreference = SharedPreference.getInstance(requireContext())
            viewModel = ViewModelProvider(this)[OnboardingViewModel::class.java]
            
            setupProgressSteps()
            setupViews()
            observeViewModel()
            
            android.util.Log.d("VehicleDetailsFragment", "=== onViewCreated completed successfully ===")
        } catch (e: Exception) {
            android.util.Log.e("VehicleDetailsFragment", "CRASH in onViewCreated!", e)
            e.printStackTrace()
            throw e
        }
    }
    
    private fun setupProgressSteps() {
        // Step 1 - Completed (show checkmark)
        binding.includeSteps.apply {
            vStep1Background.setBackgroundResource(R.drawable.bg_step_active)
            tvStep1Number.visibility = View.GONE
            ivStep1Checkmark.visibility = View.VISIBLE

            // Step 2 - Active
            vStep2Background.setBackgroundResource(R.drawable.bg_step_active)
            tvStep2Number.setTextColor(
                androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.white)
            )
            tvStep2Label.setTextColor(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_primary)
            )

            // Step 3 - Inactive (default state is fine)
        }
    }
    
    private fun setupViews() {
        binding.apply {
            // Back button
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }
            
            // RC Upload
            btnUploadRC.setOnClickListener {
                rcPickerLauncher.launch("image/*")
            }
            
            // City dropdown
            val cities = arrayOf(
                "Bangalore", "Chennai", "Mumbai", "Delhi", "Hyderabad",
                "Pune", "Kolkata", "Ahmedabad", "Coimbatore", "Kochi"
            )
            val cityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cities)
            actvCity.setAdapter(cityAdapter)
            actvCity.setOnItemClickListener { _, _, _, _ ->
                updateContinueButton()
            }
            
            // Vehicle Type Cards
            setupVehicleTypeCards()
            
            // Body Type Cards
            setupBodyTypeCards()
            
            // Body Details/Capacity dropdown - API enum values
            val capacities = arrayOf(
                "8 ft / 1.5 ton",
                "14 ft / 3.5 ton",
                "17 ft / 4.5 ton",
                "19 ft / 6 ton"
            )
            val capacityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, capacities)
            actvBodyDetails.setAdapter(capacityAdapter)
            actvBodyDetails.setOnItemClickListener { _, _, _, _ ->
                updateContinueButton()
            }
            
            // Continue button - initially disabled
            ViewUtils.disable(btnContinueFixed)
            btnContinueFixed.setOnClickListener {
                if (validateForm()) {
                    uploadAndSaveVehicle()
                }
            }
            
            // Vehicle number text change listener
            etVehicleNumber.setOnFocusChangeListener { _, _ ->
                updateContinueButton()
            }
        }
    }
    
    private fun setupVehicleTypeCards() {
        binding.apply {
            val vehicleCards = listOf(
                cardTruck to "Truck",
                cardMiniTruck to "Mini Truck",
                card3Wheeler to "3-Wheeler",
                cardPickup to "Pickup"
            )
            
            vehicleCards.forEach { (card, type) ->
                card.setOnClickListener {
                    selectVehicleType(card, type)
                    // Show body details section when vehicle type is selected
                    showBodyDetailsSection()
                }
            }
        }
    }
    
    private fun setupBodyTypeCards() {
        binding.apply {
            val bodyCards = listOf(
                cardClosed to "Closed",
                cardOpen to "Open",
                cardSemiOpen to "Semi-Open"
            )
            
            bodyCards.forEach { (card, type) ->
                card.setOnClickListener {
                    selectBodyType(card, type)
                }
            }
        }
    }
    
    private fun selectVehicleType(selectedCard: MaterialCardView, type: String) {
        binding.apply {
            // Reset all cards
            listOf(cardTruck, cardMiniTruck, card3Wheeler, cardPickup).forEach { card ->
                card.strokeColor = resources.getColor(R.color.border_light, null)
                card.strokeWidth = 2
            }
            
            // Highlight selected card
            selectedCard.strokeColor = resources.getColor(R.color.primary, null)
            selectedCard.strokeWidth = 4
            
            selectedVehicleType = type
            updateContinueButton()
        }
    }
    
    private fun selectBodyType(selectedCard: MaterialCardView, type: String) {
        binding.apply {
            // Reset all body type cards
            listOf(cardClosed, cardOpen, cardSemiOpen).forEach { card ->
                card.strokeColor = resources.getColor(R.color.border_light, null)
                card.strokeWidth = 2
            }
            
            // Highlight selected card
            selectedCard.strokeColor = resources.getColor(R.color.primary, null)
            selectedCard.strokeWidth = 4
            
            selectedBodyType = type
            updateContinueButton()
        }
    }
    
    private fun showBodyDetailsSection() {
        binding.llBodyDetailsContainer.visibility = View.VISIBLE
    }
    
    private fun validateForm(): Boolean {
        binding.apply {
            val vehicleNumber = etVehicleNumber.text.toString().trim()
            val city = actvCity.text.toString()
            val bodyDetails = actvBodyDetails.text.toString()
            
            if (vehicleNumber.isEmpty()) {
                Bakery.showToast(requireContext(), "Please enter vehicle number")
                return false
            }
            
            if (rcUri == null) {
                Bakery.showToast(requireContext(), "Please upload Vehicle RC")
                return false
            }
            
            if (city.isEmpty()) {
                Bakery.showToast(requireContext(), "Please select city")
                return false
            }
            
            if (selectedVehicleType == null) {
                Bakery.showToast(requireContext(), "Please select vehicle type")
                return false
            }
            
            if (selectedBodyType == null) {
                Bakery.showToast(requireContext(), "Please select body type")
                return false
            }
            
            if (bodyDetails.isEmpty()) {
                Bakery.showToast(requireContext(), "Please select capacity/size")
                return false
            }
            
            return true
        }
    }
    
    private fun updateContinueButton() {
        binding.apply {
            val vehicleNumber = etVehicleNumber.text.toString().trim()
            val city = actvCity.text.toString()
            val bodyDetails = actvBodyDetails.text.toString()
            
            val isComplete = vehicleNumber.isNotEmpty() && rcUri != null &&
                           city.isNotEmpty() && selectedVehicleType != null &&
                           selectedBodyType != null && bodyDetails.isNotEmpty()
            
            if (isComplete) {
                ViewUtils.enable(btnContinueFixed)
            } else {
                ViewUtils.disable(btnContinueFixed)
            }
        }
    }
    
    private fun saveDataLocally() {
        binding.apply {
            val vehicleNumber = etVehicleNumber.text.toString().trim()
            val city = actvCity.text.toString()
            val bodyDetailsDisplay = actvBodyDetails.text.toString()
            
            // Convert display value to API enum format
            val bodyDetails = when (bodyDetailsDisplay) {
                "8 ft / 1.5 ton" -> "EIGHT_FT_1_5_TON"
                "14 ft / 3.5 ton" -> "FOURTEEN_FT_3_5_TON"
                "17 ft / 4.5 ton" -> "SEVENTEEN_FT_4_5_TON"
                "19 ft / 6 ton" -> "NINETEEN_FT_6_TON"
                else -> bodyDetailsDisplay
            }
            
            // Save to SharedPreferences
            sharedPreference.saveVehicleNumber(vehicleNumber)
            sharedPreference.saveVehicleRCUri(rcUri.toString())
            sharedPreference.saveVehicleCity(city)
            sharedPreference.saveVehicleType(selectedVehicleType ?: "")
            sharedPreference.saveBodyType(selectedBodyType ?: "")
            sharedPreference.saveBodyCapacity(bodyDetails)
        }
    }
    
    private fun uploadAndSaveVehicle() {
        binding.apply {
            val vehicleNumber = etVehicleNumber.text.toString().trim()
            val city = actvCity.text.toString()
            val bodyDetailsDisplay = actvBodyDetails.text.toString()
            
            // Convert display values to API enum formats
            val bodyDetailsEnum = when (bodyDetailsDisplay) {
                "8 ft / 1.5 ton" -> "EIGHT_FT_1_5_TON"
                "14 ft / 3.5 ton" -> "FOURTEEN_FT_3_5_TON"
                "17 ft / 4.5 ton" -> "SEVENTEEN_FT_4_5_TON"
                "19 ft / 6 ton" -> "NINETEEN_FT_6_TON"
                else -> bodyDetailsDisplay
            }
            
            val vehicleTypeEnum = when (selectedVehicleType) {
                "Truck" -> "TRUCK"
                "Mini Truck" -> "MINI_TRUCK"
                "3-Wheeler" -> "THREE_WHEELER"
                "Pickup" -> "PICKUP"
                else -> selectedVehicleType ?: ""
            }
            
            val bodyTypeEnum = when (selectedBodyType) {
                "Open" -> "OPEN"
                "Closed" -> "CLOSED"
                "Semi-Open" -> "SEMI_OPEN"
                else -> selectedBodyType ?: ""
            }
            
            // Show loading
            Bakery.showToast(requireContext(), "Uploading RC document...")
            ViewUtils.disable(btnContinueFixed)
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Upload RC to S3
                    val rcUrl = rcUri?.let { uri ->
                        S3UploadUtil.uploadFile(
                            requireContext(),
                            uri,
                            "documents/vehicle/rc_${System.currentTimeMillis()}.jpg"
                        )
                    } ?: ""
                    
                    withContext(Dispatchers.Main) {
                        if (rcUrl.isNotEmpty()) {
                            // Call API with S3 URL
                            viewModel.saveVehicle(
                                registrationNumber = vehicleNumber,
                                vehicleType = vehicleTypeEnum,
                                bodyType = bodyTypeEnum,
                                bodySpec = bodyDetailsEnum,
                                rcUrl = rcUrl,
                                insuranceUrl = "", // Optional
                                pucUrl = "" // Optional
                            )
                            
                            // Save locally as well
                            saveDataLocally()
                        } else {
                            Bakery.showToast(requireContext(), "Failed to upload RC document")
                            ViewUtils.enable(btnContinueFixed)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("VehicleDetailsFragment", "Upload error", e)
                    withContext(Dispatchers.Main) {
                        Bakery.showToast(requireContext(), "Error: ${e.message}")
                        ViewUtils.enable(btnContinueFixed)
                    }
                }
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.vehicleSaveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Bakery.showToast(requireContext(), "Vehicle details saved successfully")
                navigateToDriverDetails()
            }
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Bakery.showToast(requireContext(), it)
                ViewUtils.enable(binding.btnContinueFixed)
            }
        }
        
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                ViewUtils.disable(binding.btnContinueFixed)
            }
        }
    }
    
    private fun navigateToDriverDetails() {
        val action = VehicleDetailsFragmentDirections
            .actionVehicleDetailsToDriverDetails()
        findNavController().navigate(action)
    }
}
