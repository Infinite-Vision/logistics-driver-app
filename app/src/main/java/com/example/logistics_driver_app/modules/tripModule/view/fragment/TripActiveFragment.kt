package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentTripActiveBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.TripFlowViewModel

/**
 * TripActiveFragment - Trip is in progress, driver heading to drop location.
 * Shows drop contact + address.
 * "Arrived at Drop Location" button → calls arrivedAtDrop API → navigate to DropArrivalFragment.
 */
class TripActiveFragment : BaseFragment<FragmentTripActiveBinding>() {

    private val tripFlowViewModel: TripFlowViewModel by activityViewModels()
    private val sharedPreference by lazy { SharedPreference.getInstance(requireContext()) }

    // Fallback if GPS is not yet available
    private val fallbackLat = 12.9200
    private val fallbackLng = 79.1400

    private fun currentLat() = com.example.logistics_driver_app.data.service.DriverLocationService.lastKnownLocation?.latitude ?: fallbackLat
    private fun currentLng() = com.example.logistics_driver_app.data.service.DriverLocationService.lastKnownLocation?.longitude ?: fallbackLng

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTripActiveBinding {
        return FragmentTripActiveBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateTripInfo()
        setupViews()
        observeViewModel()
        // Expand bottom sheet so "Arrived at Drop" button is visible by default
        BottomSheetBehavior.from(binding.bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun populateTripInfo() {
        binding.apply {
            tvTripStatus.text = "Heading to Drop Location"
            tvOrderId.text = "#ORD${sharedPreference.getOrderId().toString().takeLast(4)}"
            tvEstimatedTime.text = "Arriving in ~15 mins"
            tvDistance.text = "5.5 km away"
            tvDropAddress.text = sharedPreference.getCurrentDropAddress().ifEmpty { "Drop Location" }
            tvDropContact.text = sharedPreference.getCurrentCustomerName().ifEmpty { "Customer" }
            tvPickupAddress.text = sharedPreference.getCurrentPickupAddress().ifEmpty { "Pickup Location" }
            tvPickupContact.text = sharedPreference.getCurrentCustomerName().ifEmpty { "Customer" }
            val fare = sharedPreference.getOrderFare()
            tvAmount.text = if (fare > 0) "₹${"%.0f".format(fare)}" else "₹450"
            tvPaymentMode.text = "CASH"
            // Re-label navigation button as "Arrived at Drop Location"
            btnStartNavigation.text = "Arrived at Drop Location"
        }
    }

    private fun setupViews() {
        binding.apply {
            btnMenu.setOnClickListener {
                findNavController().navigate(R.id.action_tripActive_to_tripInfoSheet)
            }

            btnCallDrop.setOnClickListener {
                val phone = sharedPreference.getCurrentContactPhone().ifEmpty { "" }
                if (phone.isNotEmpty()) makePhoneCall(phone)
            }

            btnCallPickup.setOnClickListener {
                val phone = sharedPreference.getCurrentContactPhone().ifEmpty { "" }
                if (phone.isNotEmpty()) makePhoneCall(phone)
            }

            // "Arrived at Drop" — calls arrivedAtDrop API
            btnStartNavigation.setOnClickListener {
                it.isEnabled = false
                tripFlowViewModel.arrivedAtDrop(currentLat(), currentLng())
            }
        }
    }

    private fun observeViewModel() {
        tripFlowViewModel.arrivedDropResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is TripFlowViewModel.StepResult.Loading -> {
                    binding.btnStartNavigation.isEnabled = false
                }
                is TripFlowViewModel.StepResult.Success -> {
                    if (isAdded && view != null) {
                        findNavController().navigate(R.id.action_tripActive_to_dropArrival)
                    }
                }
                is TripFlowViewModel.StepResult.Error -> {
                    binding.btnStartNavigation.isEnabled = true
                    Bakery.showToast(requireContext(), result.message)
                }
                else -> {}
            }
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        startActivity(Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$phoneNumber") })
    }
}


