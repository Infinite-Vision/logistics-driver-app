package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.data.service.DriverLocationService
import com.example.logistics_driver_app.databinding.FragmentTripCompletedBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment

/**
 * TripCompletedFragment - Shows rating screen then completion summary
 */
class TripCompletedFragment : BaseFragment<FragmentTripCompletedBinding>() {

    private val sharedPreference by lazy { SharedPreference.getInstance(requireContext()) }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTripCompletedBinding {
        return FragmentTripCompletedBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showRatingState()
    }

    private fun showRatingState() {
        binding.ratingSection.visibility = View.VISIBLE
        binding.completedSection.visibility = View.GONE

        binding.btnSubmitRating.setOnClickListener {
            showCompletedState()
        }
    }

    private fun showCompletedState() {
        binding.ratingSection.visibility = View.GONE
        binding.completedSection.visibility = View.VISIBLE

        val fare = sharedPreference.getOrderFare()
        binding.tvEarnedAmount.text = "You earned ₹${"%.0f".format(fare)}"

        binding.btnDone.setOnClickListener {
            // Clear trip data
            sharedPreference.clearOrderId()
            // Restart location service so driver is back ONLINE and ready for next order
            DriverLocationService.startService(requireContext())
            findNavController().navigate(R.id.action_tripCompleted_to_driverHome)
        }
    }
}
