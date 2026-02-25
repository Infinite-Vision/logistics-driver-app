package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentSearchingTripsBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.TripFlowViewModel

/**
 * SearchingTripsFragment - Shows searching animation when driver is online.
 * When a NEW_ORDER arrives (or simulated), saves trip data and navigates
 * to the trip request screen.
 */
class SearchingTripsFragment : BaseFragment<FragmentSearchingTripsBinding>() {

    private val tripFlowViewModel: TripFlowViewModel by activityViewModels()
    private val handler = Handler(Looper.getMainLooper())
    private var pulseAnimator: ObjectAnimator? = null

    private val sharedPreference by lazy { SharedPreference.getInstance(requireContext()) }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchingTripsBinding {
        return FragmentSearchingTripsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        startPulseAnimation()
        simulateTripRequest()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            btnMenu.setOnClickListener {
                findNavController().navigate(R.id.action_searchingTrips_to_menu)
            }

            btnGoOffline.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun startPulseAnimation() {
        pulseAnimator = ObjectAnimator.ofFloat(binding.ivSearching, "scaleX", 1f, 1.2f, 1f).apply {
            duration = 1500
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        ObjectAnimator.ofFloat(binding.ivSearching, "scaleY", 1f, 1.2f, 1f).apply {
            duration = 1500
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    /**
     * Simulates receiving a NEW_ORDER WebSocket message after 3 seconds.
     * Saves demo trip data to SharedPreference and TripFlowViewModel, then navigates.
     */
    private fun simulateTripRequest() {
        handler.postDelayed({
            if (isAdded && view != null) {
                // Save demo trip data (replace with real payload.orderId etc. when WebSocket integrated)
                val demoOrderId = 1001L
                sharedPreference.saveOrderId(demoOrderId)
                sharedPreference.saveCurrentCustomerName("Amit Verma")
                sharedPreference.saveCurrentPickupAddress("Block 4, Sector 12, Noida")
                sharedPreference.saveCurrentDropAddress("Mall Road, Sector 18, Noida")
                sharedPreference.saveCurrentContactPhone("9876543210")
                sharedPreference.saveCurrentDropLandmark("Near City Mall")
                sharedPreference.saveOrderFare(450.0)
                tripFlowViewModel.setOrderId(demoOrderId)

                findNavController().navigate(R.id.action_searchingTrips_to_tripRequest)
            }
        }, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        pulseAnimator?.cancel()
    }
}
