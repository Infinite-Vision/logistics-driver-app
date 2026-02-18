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
            // Initial state - show button, hide slider and status
            showButtonState()
            
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

            // Arrived at Pickup Button (t4.png)
            btnArrivedAtPickup.setOnClickListener {
                onArrivedAtPickup()
            }
            
            // Slider for starting trip (t5.png)
            setupSlider()

            // Call Button
            btnCall.setOnClickListener {
                viewModel.currentTrip.value?.let { trip ->
                    makePhoneCall(trip.pickupContactPhone)
                }
            }
        }
    }
    
    private fun showButtonState() {
        // t4.png state - Show "Arrived at Pickup" button
        binding.apply {
            headerCard.visibility = View.VISIBLE
            tvHeaderTitle.text = "Head to pickup location"
            btnArrivedAtPickup.visibility = View.VISIBLE
            sliderContainer.visibility = View.GONE
            statusCard.visibility = View.GONE
        }
    }
    
    private fun showSliderState() {
        // t5.png state - Show slider and waiting status
        binding.apply {
            headerCard.visibility = View.GONE
            btnArrivedAtPickup.visibility = View.GONE
            sliderContainer.visibility = View.VISIBLE
            statusCard.visibility = View.VISIBLE
            tvStatus.text = "Waiting for customer..."
            startWaitTimer()
        }
    }
    
    private fun onArrivedAtPickup() {
        // TODO: Call API to mark arrived at pickup
        Bakery.showToast(requireContext(), "Marked as arrived at pickup")
        showSliderState()
    }
    
    private fun startWaitTimer() {
        // TODO: Implement wait timer
        binding.tvWaitTimer.text = "Wait 00:00"
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
        // Slider implementation for starting trip
        binding.sliderButton.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    // Start dragging
                    true
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    // Update slider position
                    val newX = event.rawX - view.width / 2
                    val maxX = binding.sliderContainer.width - view.width
                    if (newX in 0f..maxX.toFloat()) {
                        view.x = newX
                    }
                    
                    // Check if slid to end
                    if (newX >= maxX * 0.9) {
                        onStartTrip()
                        return@setOnTouchListener true
                    }
                    true
                }
                android.view.MotionEvent.ACTION_UP -> {
                    // Reset slider position if not completed
                    android.animation.ObjectAnimator.ofFloat(view, "translationX", 0f).apply {
                        duration = 200
                        start()
                    }
                    true
                }
                else -> false
            }
        }
    }
    
    private fun onStartTrip() {
        // TODO: Verify OTP and start trip
        Bakery.showToast(requireContext(), "Starting trip...")
        // Navigate to next screen
    }

    private fun makePhoneCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }
}
