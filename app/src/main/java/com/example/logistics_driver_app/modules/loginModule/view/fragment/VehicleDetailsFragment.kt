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
import com.example.logistics_driver_app.data.model.FormOption
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
    private var selectedBodySpec: String? = null
    
    // API Data
    private var vehicleTypeOptions: List<FormOption> = emptyList()
    private var bodyTypeOptions: List<FormOption> = emptyList()
    private var bodySpecOptions: List<FormOption> = emptyList()
    
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
            
            // Fetch vehicle form options from API
            viewModel.getVehicleFormOptions()
            
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
            
            // Body Details/Capacity dropdown - Will be populated from API
            actvBodyDetails.setOnItemClickListener { parent, _, position, _ ->
                // Get the selected FormOption
                if (bodySpecOptions.isNotEmpty() && position < bodySpecOptions.size) {
                    selectedBodySpec = bodySpecOptions[position].code
                }
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
            // Vehicle type cards are static in the layout
            // We'll map them to API codes when they're clicked
            val vehicleCards = listOf(
                cardTruck,
                cardMiniTruck,
                card3Wheeler,
                cardPickup
            )
            
            vehicleCards.forEachIndexed { index, card ->
                card.setOnClickListener {
                    // Get the corresponding code from API options
                    if (vehicleTypeOptions.isNotEmpty() && index < vehicleTypeOptions.size) {
                        val option = vehicleTypeOptions[index]
                        selectVehicleType(card, option.code)
                    }
                    // Show body details section when vehicle type is selected
                    showBodyDetailsSection()
                }
            }
        }
    }
    
    private fun setupBodyTypeCards() {
        binding.apply {
            // Body type cards are static in the layout
            // We'll map them to API codes when they're clicked
            val bodyCards = listOf(
                cardClosed,
                cardOpen,
                cardSemiOpen
            )
            
            bodyCards.forEachIndexed { index, card ->
                card.setOnClickListener {
                    // Map card to API code based on order
                    // API returns: OPEN, CLOSED, SEMI_OPEN (alphabetically)
                    // Layout order: CLOSED, OPEN, SEMI_OPEN
                    val mappedIndex = when (index) {
                        0 -> 1 // cardClosed -> CLOSED
                        1 -> 0 // cardOpen -> OPEN
                        2 -> 2 // cardSemiOpen -> SEMI_OPEN
                        else -> index
                    }
                    if (bodyTypeOptions.isNotEmpty() && mappedIndex < bodyTypeOptions.size) {
                        val option = bodyTypeOptions[mappedIndex]
                        selectBodyType(card, option.code)
                    }
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
    
    private fun populateBodySpecsDropdown() {
        if (bodySpecOptions.isEmpty()) {
            return
        }
        
        binding.apply {
            val displayNames = bodySpecOptions.map { it.displayName }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                displayNames
            )
            actvBodyDetails.setAdapter(adapter)
        }
    }
    
    private fun validateForm(): Boolean {
        binding.apply {
            val vehicleNumber = etVehicleNumber.text.toString().trim()
            val city = actvCity.text.toString()
            
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
            
            if (selectedBodySpec == null || selectedBodySpec!!.isEmpty()) {
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
            
            val isComplete = vehicleNumber.isNotEmpty() && rcUri != null &&
                           city.isNotEmpty() && selectedVehicleType != null &&
                           selectedBodyType != null && selectedBodySpec != null
            
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
            
            // Save to SharedPreferences using codes from API
            sharedPreference.saveVehicleNumber(vehicleNumber)
            sharedPreference.saveVehicleRCUri(rcUri.toString())
            sharedPreference.saveVehicleCity(city)
            sharedPreference.saveVehicleType(selectedVehicleType ?: "")
            sharedPreference.saveBodyType(selectedBodyType ?: "")
            sharedPreference.saveBodyCapacity(selectedBodySpec ?: "")
        }
    }
    
    private fun uploadAndSaveVehicle() {
        binding.apply {
            val vehicleNumber = etVehicleNumber.text.toString().trim()
            val city = actvCity.text.toString()
            
            // Use codes from API directly (no conversion needed)
            val vehicleTypeCode = selectedVehicleType ?: ""
            val bodyTypeCode = selectedBodyType ?: ""
            val bodySpecCode = selectedBodySpec ?: ""
            
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
                                vehicleType = vehicleTypeCode,
                                bodyType = bodyTypeCode,
                                bodySpec = bodySpecCode,
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
        // Observe vehicle form options
        viewModel.vehicleFormOptions.observe(viewLifecycleOwner) { options ->
            options?.let {
                vehicleTypeOptions = it.vehicleTypes
                bodyTypeOptions = it.bodyTypes
                bodySpecOptions = it.bodySpecs
                
                // Populate body specs dropdown
                populateBodySpecsDropdown()
                
                android.util.Log.d("VehicleDetailsFragment", "Vehicle form options loaded: ${vehicleTypeOptions.size} types, ${bodyTypeOptions.size} body types, ${bodySpecOptions.size} specs")
            }
        }
        
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
