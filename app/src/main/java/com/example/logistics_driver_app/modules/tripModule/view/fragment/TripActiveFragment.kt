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
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentTripActiveBinding
import com.example.logistics_driver_app.data.model.TripStatus
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.TripActiveViewModel

/**
 * TripActiveFragment - Main screen showing active trip details
 * Displays pickup/drop locations, estimated time, and navigation options
 */
class TripActiveFragment : BaseFragment<FragmentTripActiveBinding>() {

    private val viewModel: TripActiveViewModel by viewModels()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTripActiveBinding {
        return FragmentTripActiveBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.apply {
            // Menu button - shows trip info sheet
            btnMenu.setOnClickListener {
                findNavController().navigate(R.id.action_tripActive_to_tripInfoSheet)
            }

            // Start Navigation
            btnStartNavigation.setOnClickListener {
                viewModel.startNavigation()
                startGoogleMapsNavigation()
            }

            // Call Pickup Contact
            btnCallPickup.setOnClickListener {
                viewModel.currentTrip.value?.let { trip ->
                    makePhoneCall(trip.pickupContactPhone)
                }
            }

            // Call Drop Contact
            btnCallDrop.setOnClickListener {
                viewModel.currentTrip.value?.let { trip ->
                    makePhoneCall(trip.dropContactPhone)
                }
            }
        }
    }

    private fun observeViewModel() {
        // Observe current trip
        viewModel.currentTrip.observe(viewLifecycleOwner, Observer { trip ->
            trip?.let {
                updateUI(it)
            }
        })

        // Observe trip status changes
        viewModel.tripStatus.observe(viewLifecycleOwner, Observer { status ->
            updateTripStatus(status)
        })

        // Observe navigation started
        viewModel.navigationStarted.observe(viewLifecycleOwner, Observer { started ->
            if (started) {
                Bakery.showToast(requireContext(), getString(R.string.navigation_started))
            }
        })
    }

    private fun updateUI(trip: com.example.logistics_driver_app.data.model.Trip) {
        binding.apply {
            // Order ID
            tvOrderId.text = getString(R.string.order_id_format, trip.orderId)

            // Estimated time
            tvEstimatedTime.text = getString(R.string.arriving_in, "${trip.estimatedTime} mins")

            // Distance
            tvDistance.text = getString(R.string.km_away, trip.distance.toString())

            // Pickup details
            tvPickupAddress.text = trip.pickupAddress
            tvPickupContact.text = trip.pickupContactName

            // Drop details
            tvDropAddress.text = trip.dropAddress
            tvDropContact.text = trip.dropContactName

            // Amount
            tvAmount.text = getString(R.string.rupee_amount, trip.amount.toString())

            // Payment mode
            tvPaymentMode.text = if (trip.paymentMode == "CASH") 
                getString(R.string.cash) else getString(R.string.online)
        }
    }

    private fun updateTripStatus(status: TripStatus) {
        binding.tvTripStatus.text = when (status) {
            TripStatus.TRIP_ASSIGNED -> getString(R.string.trip_assigned)
            TripStatus.HEADING_TO_PICKUP -> getString(R.string.heading_to_pickup)
            TripStatus.ARRIVED_AT_PICKUP -> getString(R.string.arrived_at_pickup)
            TripStatus.STARTED_TRIP -> getString(R.string.trip_started)
            TripStatus.HEADING_TO_DROP -> getString(R.string.heading_to_drop)
            TripStatus.ARRIVED_AT_DROP -> {
                // Navigate to drop screen
                findNavController().navigate(R.id.action_tripActive_to_dropArrival)
                getString(R.string.arrived_at_drop)
            }
            TripStatus.TRIP_COMPLETED -> getString(R.string.trip_completed)
            TripStatus.TRIP_CANCELLED -> getString(R.string.trip_cancelled)
        }

        // Update button text based on status
        when (status) {
            TripStatus.HEADING_TO_DROP -> {
                binding.btnStartNavigation.text = getString(R.string.start_navigation)
            }
            TripStatus.ARRIVED_AT_DROP -> {
                binding.btnStartNavigation.text = getString(R.string.complete_trip)
            }
            else -> {}
        }
    }

    private fun startGoogleMapsNavigation() {
        viewModel.currentTrip.value?.let { trip ->
            val latitude = trip.pickupLat
            val longitude = trip.pickupLng
            
            // Create navigation intent for Google Maps
            val uri = Uri.parse("google.navigation:q=$latitude,$longitude")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            
            // Check if Google Maps is installed
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback to browser/map selector
                val fallbackUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
                startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
            }
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }
}
