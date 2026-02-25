package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentPickupArrivalBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.TripFlowViewModel

/**
 * PickupArrivalFragment - Driver arrives at pickup location.
 *
 * State 1 ("Head to pickup"): shows "Arrived at Pickup" button.
 * Tap → calls arrivedAtPickup API → on success → State 2.
 *
 * State 2 ("Waiting"): shows OTP boxes + slider.
 * Slide → reads OTP → calls startTrip API → on success → navigate to tripActive.
 */
class PickupArrivalFragment : BaseFragment<FragmentPickupArrivalBinding>() {

    private val tripFlowViewModel: TripFlowViewModel by activityViewModels()
    private val sharedPreference by lazy { SharedPreference.getInstance(requireContext()) }

    // Fallback if GPS is not yet available
    private val fallbackLat = 12.9165
    private val fallbackLng = 79.1325

    private fun currentLat() = com.example.logistics_driver_app.data.service.DriverLocationService.lastKnownLocation?.latitude ?: fallbackLat
    private fun currentLng() = com.example.logistics_driver_app.data.service.DriverLocationService.lastKnownLocation?.longitude ?: fallbackLng

    private var waitSeconds = 0
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            waitSeconds++
            val mm = waitSeconds / 60
            val ss = waitSeconds % 60
            binding.tvWaitTimer.text = "Wait %02d:%02d".format(mm, ss)
            timerHandler.postDelayed(this, 1000)
        }
    }

    private var sliderStartRawX = 0f
    private var sliderStartTranslation = 0f
    private var sliderTriggered = false

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPickupArrivalBinding {
        return FragmentPickupArrivalBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateTripInfo()
        setupViews()
        observeViewModel()
    }

    private fun populateTripInfo() {
        binding.apply {
            tvContactName.text = sharedPreference.getCurrentCustomerName().ifEmpty { "Customer" }
            tvPickupAddress.text = sharedPreference.getCurrentPickupAddress().ifEmpty { "Pickup Location" }
        }
        showButtonState()
    }

    private fun setupViews() {
        binding.apply {
            btnSos.setOnClickListener { /* SOS placeholder */ }

            btnMenu.setOnClickListener { /* Menu placeholder */ }

            btnNavigate.setOnClickListener {
                val uri = Uri.parse("google.navigation:q=${currentLat()},${currentLng()}")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
                try { startActivity(intent) } catch (_: Exception) {
                    Bakery.showToast(requireContext(), "Google Maps not installed")
                }
            }

            btnArrivedAtPickup.setOnClickListener { onArrivedAtPickup() }

            setupOtpFields()

            btnCall.setOnClickListener {
                val phone = sharedPreference.getCurrentContactPhone().ifEmpty { "" }
                if (phone.isNotEmpty()) makePhoneCall(phone)
            }

            setupSlider()
        }
    }

    // ---- UI states -------------------------------------------------------

    private fun showButtonState() {
        binding.apply {
            headerCard.visibility = View.VISIBLE
            tvHeaderTitle.text = "Head to pickup location"
            btnArrivedAtPickup.visibility = View.VISIBLE
            sliderContainer.visibility = View.GONE
            otpSection.visibility = View.GONE
            statusCard.visibility = View.GONE
        }
    }

    private fun showWaitingState() {
        binding.apply {
            headerCard.visibility = View.GONE
            btnArrivedAtPickup.visibility = View.GONE
            statusCard.visibility = View.VISIBLE
            tvStatus.text = "Waiting for customer..."
            otpSection.visibility = View.VISIBLE
            sliderContainer.visibility = View.VISIBLE
            tvSliderText.text = "Slide to Start Trip"
        }
        startWaitTimer()
    }

    // ---- actions ---------------------------------------------------------

    private fun onArrivedAtPickup() {
        binding.btnArrivedAtPickup.isEnabled = false
        tripFlowViewModel.arrivedAtPickup(currentLat(), currentLng())
    }

    private fun onStartTrip() {
        val otp = binding.run {
            listOf(etOtp1, etOtp2, etOtp3, etOtp4)
                .joinToString("") { it.text.toString().trim() }
        }
        if (otp.length < 4) {
            binding.tvOtpError.visibility = View.VISIBLE
            binding.tvOtpError.text = "Enter 4-digit OTP"
            shakeOtpFields()
            resetSlider()
            return
        }
        binding.tvOtpError.visibility = View.GONE
        tripFlowViewModel.startTrip(otp, currentLat(), currentLng())
    }

    private fun resetSlider() {
        sliderTriggered = false
        android.animation.ObjectAnimator.ofFloat(binding.sliderButton, "translationX", 0f)
            .apply { duration = 200; start() }
    }

    // ---- observer --------------------------------------------------------

    private fun observeViewModel() {
        tripFlowViewModel.arrivedPickupResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is TripFlowViewModel.StepResult.Loading -> {
                    binding.btnArrivedAtPickup.isEnabled = false
                }
                is TripFlowViewModel.StepResult.Success -> {
                    showWaitingState()
                }
                is TripFlowViewModel.StepResult.Error -> {
                    binding.btnArrivedAtPickup.isEnabled = true
                    Bakery.showToast(requireContext(), result.message)
                }
                else -> {}
            }
        }

        tripFlowViewModel.startTripResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is TripFlowViewModel.StepResult.Loading -> { /* spinner optional */ }
                is TripFlowViewModel.StepResult.Success -> {
                    timerHandler.removeCallbacks(timerRunnable)
                    // Clear any red OTP error state before navigating
                    listOf(binding.etOtp1, binding.etOtp2, binding.etOtp3, binding.etOtp4)
                        .forEach { it.setBackgroundResource(R.drawable.bg_otp_box) }
                    binding.tvOtpError.visibility = View.GONE
                    if (isAdded && view != null) {
                        findNavController().navigate(R.id.action_pickupArrival_to_tripActive)
                    }
                }
                is TripFlowViewModel.StepResult.Error -> {
                    binding.tvOtpError.visibility = View.VISIBLE
                    binding.tvOtpError.text = result.message
                    shakeOtpFields()
                    resetSlider()
                }
                else -> {}
            }
        }
    }

    // ---- OTP fields -------------------------------------------------------

    private fun setupOtpFields() {
        val fields = listOf(binding.etOtp1, binding.etOtp2, binding.etOtp3, binding.etOtp4)
        fields.forEachIndexed { index, editText ->
            // Forward: auto-advance on character input
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (index < fields.size - 1) fields[index + 1].requestFocus()
                        // Clear error highlight on typing
                        editText.setBackgroundResource(R.drawable.bg_otp_box)
                    }
                }
            })
            // Backward: go to previous field on backspace when empty
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (editText.text.isEmpty() && index > 0) {
                        fields[index - 1].apply {
                            requestFocus()
                            setText("")
                        }
                        true
                    } else false
                } else false
            }
        }
    }

    private fun shakeOtpFields() {
        val shakeAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        val fields = listOf(binding.etOtp1, binding.etOtp2, binding.etOtp3, binding.etOtp4)
        fields.forEach {
            it.setBackgroundResource(R.drawable.bg_otp_box_error)
            it.startAnimation(shakeAnim)
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded && view != null) {
                fields.forEach { it.setBackgroundResource(R.drawable.bg_otp_box) }
            }
        }, 2000)
    }

    // ---- slider ----------------------------------------------------------

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
                            onStartTrip()
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

    // ---- timer -----------------------------------------------------------

    private fun startWaitTimer() {
        waitSeconds = 0
        timerHandler.post(timerRunnable)
    }

    // ---- utils -----------------------------------------------------------

    private fun makePhoneCall(phoneNumber: String) {
        startActivity(Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$phoneNumber") })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerHandler.removeCallbacks(timerRunnable)
    }
}


