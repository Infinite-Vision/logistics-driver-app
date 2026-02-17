package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentSearchingTripsBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment

/**
 * SearchingTripsFragment - Shows searching animation when driver is online
 */
class SearchingTripsFragment : BaseFragment<FragmentSearchingTripsBinding>() {

    private val handler = Handler(Looper.getMainLooper())
    private var pulseAnimator: ObjectAnimator? = null

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

    private fun simulateTripRequest() {
        // Simulate receiving a trip request after 3 seconds (for demo)
        handler.postDelayed({
            if (isAdded && view != null) {
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
