package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentTripRequestBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.DriverHomeViewModel
import com.example.logistics_driver_app.modules.tripModule.viewModel.TripFlowViewModel

/**
 * TripRequestFragment - Shows incoming trip request with timer.
 * On Accept: calls acceptOrder API then navigates to PickupArrivalFragment.
 * On Decline: calls rejectOrder API and goes back to searching.
 */
class TripRequestFragment : BaseFragment<FragmentTripRequestBinding>() {

    private val homeViewModel: DriverHomeViewModel by activityViewModels()
    private val tripFlowViewModel: TripFlowViewModel by activityViewModels()
    private var countDownTimer: CountDownTimer? = null

    private val sharedPreference by lazy { SharedPreference.getInstance(requireContext()) }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTripRequestBinding {
        return FragmentTripRequestBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        loadTripDetails()
        startTimer()
    }

    private fun loadTripDetails() {
        binding.apply {
            tvPickupAddress.text = sharedPreference.getCurrentPickupAddress().ifEmpty { "Pickup Location" }
            tvDropAddress.text = sharedPreference.getCurrentDropAddress().ifEmpty { "Drop Location" }
            val fare = sharedPreference.getOrderFare()
            tvEstimatedFare.text = if (fare > 0) "₹${"%.0f".format(fare)}" else "₹450"
            try { tvTripDistance.text = "~5.2 km trip" } catch (_: Exception) {}
        }
    }

    private fun setupViews() {
        binding.apply {
            btnClose.setOnClickListener { declineTrip() }

            btnMenu.setOnClickListener {
                findNavController().navigate(R.id.action_tripRequest_to_menu)
            }

            btnAccept.setOnClickListener { acceptTrip() }

            btnDecline.setOnClickListener { declineTrip() }
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvTimer.text = String.format("00:%02d", seconds)
            }

            override fun onFinish() {
                declineTrip()
            }
        }.start()
    }

    private fun acceptTrip() {
        countDownTimer?.cancel()
        val orderId = sharedPreference.getOrderId()
        if (orderId == -1L) {
            // Demo mode — no real order, just navigate
            findNavController().navigate(R.id.action_tripRequest_to_pickupArrival)
            return
        }
        binding.btnAccept.isEnabled = false
        binding.btnDecline.isEnabled = false
        homeViewModel.acceptOrder(
            orderId = orderId,
            onSuccess = {
                tripFlowViewModel.setOrderId(orderId)
                if (isAdded && view != null) {
                    findNavController().navigate(R.id.action_tripRequest_to_pickupArrival)
                }
            },
            onError = { err ->
                if (isAdded) {
                    binding.btnAccept.isEnabled = true
                    binding.btnDecline.isEnabled = true
                    Bakery.showToast(requireContext(), err)
                }
            }
        )
    }

    private fun declineTrip() {
        countDownTimer?.cancel()
        val orderId = sharedPreference.getOrderId()
        if (orderId != -1L) {
            homeViewModel.rejectOrder(orderId, onSuccess = {}, onError = {})
        }
        if (isAdded && view != null) findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}
