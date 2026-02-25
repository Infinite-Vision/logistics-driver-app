package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentDropArrivalBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.TripFlowViewModel

/**
 * DropArrivalFragment - Driver arrived at drop location ("Unloading").
 *
 * Shows unloading timer + contact info + address.
 * Slider → calls endTrip API → on success → navigate to PaymentCollectionFragment.
 */
class DropArrivalFragment : BaseFragment<FragmentDropArrivalBinding>() {

    private val tripFlowViewModel: TripFlowViewModel by activityViewModels()
    private val sharedPreference by lazy { SharedPreference.getInstance(requireContext()) }

    // Fallback if GPS is not yet available
    private val fallbackLat = 12.9200
    private val fallbackLng = 79.1400

    private fun currentLat() = com.example.logistics_driver_app.data.service.DriverLocationService.lastKnownLocation?.latitude ?: fallbackLat
    private fun currentLng() = com.example.logistics_driver_app.data.service.DriverLocationService.lastKnownLocation?.longitude ?: fallbackLng

    private var unloadSeconds = 0
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            unloadSeconds++
            val mm = unloadSeconds / 60
            val ss = unloadSeconds % 60
            binding.tvUnloadTimer.text = "Unloading %02d:%02d".format(mm, ss)
            timerHandler.postDelayed(this, 1000)
        }
    }

    private var sliderStartRawX = 0f
    private var sliderStartTranslation = 0f
    private var sliderTriggered = false

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDropArrivalBinding {
        return FragmentDropArrivalBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateInfo()
        setupViews()
        observeViewModel()
        timerHandler.post(timerRunnable)
    }

    private fun populateInfo() {
        binding.apply {
            tvContactName.text = sharedPreference.getCurrentCustomerName().ifEmpty { "Customer" }
            tvDropAddress.text = sharedPreference.getCurrentDropAddress().ifEmpty { "Drop Location" }
            tvDropLandmark.text = sharedPreference.getCurrentDropLandmark().ifEmpty { "" }
            tvUnloadStatus.text = "Unloading at drop location"
            tvUnloadTimer.text = "Unloading 00:00"
        }
    }

    private fun setupViews() {
        binding.apply {
            btnSos.setOnClickListener { /* SOS placeholder */ }

            btnMenu.setOnClickListener { /* Menu placeholder */ }

            btnCall.setOnClickListener {
                val phone = sharedPreference.getCurrentContactPhone().ifEmpty { "" }
                if (phone.isNotEmpty()) makePhoneCall(phone)
            }

            btnNavigate.setOnClickListener {
                val uri = Uri.parse("google.navigation:q=${currentLat()},${currentLng()}")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
                try { startActivity(intent) } catch (_: Exception) {
                    Bakery.showToast(requireContext(), "Google Maps not installed")
                }
            }

            setupSlider()
        }
    }

    private fun setupSlider() {
        binding.sliderButton.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    sliderStartRawX = event.rawX
                    sliderStartTranslation = v.translationX
                    sliderTriggered = false
                    true
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    if (!sliderTriggered) {
                        val maxX = (binding.sliderContainer.width - v.width).toFloat()
                        val newTranslation = (event.rawX - sliderStartRawX + sliderStartTranslation).coerceIn(0f, maxX)
                        v.translationX = newTranslation
                        if (newTranslation >= maxX * 0.85f) {
                            sliderTriggered = true
                            onCompleteTrip()
                        }
                    }
                    true
                }
                android.view.MotionEvent.ACTION_UP -> {
                    if (!sliderTriggered) {
                        android.animation.ObjectAnimator.ofFloat(v, "translationX", 0f)
                            .apply { duration = 200; start() }
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun onCompleteTrip() {
        timerHandler.removeCallbacks(timerRunnable)
        tripFlowViewModel.endTrip(currentLat(), currentLng())
    }

    private fun resetSlider() {
        sliderTriggered = false
        android.animation.ObjectAnimator.ofFloat(binding.sliderButton, "translationX", 0f)
            .apply { duration = 200; start() }
    }

    private fun observeViewModel() {
        tripFlowViewModel.endTripResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is TripFlowViewModel.StepResult.Loading -> { /* processing */ }
                is TripFlowViewModel.StepResult.Success -> {
                    if (isAdded && view != null) {
                        findNavController().navigate(R.id.action_dropArrival_to_payment)
                    }
                }
                is TripFlowViewModel.StepResult.Error -> {
                    resetSlider()
                    timerHandler.post(timerRunnable)
                    Bakery.showToast(requireContext(), result.message)
                }
                else -> {}
            }
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        startActivity(Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$phoneNumber") })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerHandler.removeCallbacks(timerRunnable)
    }
}
