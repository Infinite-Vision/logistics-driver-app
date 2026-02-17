package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.Common.util.ViewUtils
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentPickupArrivalBinding
import com.example.logistics_driver_app.data.model.TripStatus
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.TripPickupViewModel

/**
 * PickupArrivalFragment - Screen shown when driver arrives at pickup location
 * Allows driver to verify OTP and start the trip
 */
class PickupArrivalFragment : BaseFragment<FragmentPickupArrivalBinding>() {

    private val viewModel: TripPickupViewModel by viewModels()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPickupArrivalBinding {
        return FragmentPickupArrivalBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.apply {
            // SOS button
            btnSos.setOnClickListener {
                // Handle SOS action
            }

            // Menu button
            btnMenu.setOnClickListener {
                // Handle menu action
            }

            // Navigate button
            btnNavigate.setOnClickListener {
                // Navigate to pickup location
                val uri = Uri.parse("google.navigation:q=13.0827,80.2707")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Bakery.showToast(requireContext(), "Google Maps not installed")
                }
            }

            // Slider for starting trip
            setupSlider()

            // Call Button
            btnCall.setOnClickListener {
                viewModel.currentTrip.value?.let { trip ->
                    makePhoneCall(trip.pickupContactPhone)
                }
            }
        }
    }

    private fun observeViewModel() {
        // Observe current trip
        viewModel.currentTrip.observe(viewLifecycleOwner, Observer { trip ->
            trip?.let {
                binding.apply {
                    tvPickupAddress.text = it.pickupAddress
                    tvContactName.text = it.pickupContactName
                }
            }
        })

        // Observe OTP verification
        viewModel.otpVerified.observe(viewLifecycleOwner, Observer { verified ->
            if (verified) {
                Bakery.showToast(requireContext(), getString(R.string.otp_verified))
            }
        })
    }

    private fun setupSlider() {
        // Slider implementation for starting trip - TODO
    }

    private fun makePhoneCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }
}
