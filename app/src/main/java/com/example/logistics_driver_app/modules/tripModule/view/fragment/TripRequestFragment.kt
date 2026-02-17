package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentTripRequestBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.TripActiveViewModel

/**
 * TripRequestFragment - Shows incoming trip request with timer
 * Driver can Accept or Decline the trip
 */
class TripRequestFragment : BaseFragment<FragmentTripRequestBinding>() {

    private val viewModel: TripActiveViewModel by viewModels()
    private var countDownTimer: CountDownTimer? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTripRequestBinding {
        return FragmentTripRequestBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
        startTimer()
    }

    private fun setupViews() {
        binding.apply {
            btnClose.setOnClickListener {
                findNavController().navigateUp()
            }

            btnMenu.setOnClickListener {
                findNavController().navigate(R.id.action_tripRequest_to_menu)
            }

            btnAccept.setOnClickListener {
                acceptTrip()
            }

            btnDecline.setOnClickListener {
                declineTrip()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.currentTrip.observe(viewLifecycleOwner, Observer { trip ->
            trip?.let {
                binding.apply {
                    tvPickupAddress.text = it.pickupAddress
                    tvDropAddress.text = it.dropAddress
                    try {
                        tvTripDistance.text = "~${it.distance} km trip"
                    } catch (e: Exception) {
                        // Field doesn't exist
                    }
                    tvEstimatedFare.text = getString(R.string.rupee_amount, it.amount.toString())
                    
                    // Show helper banner if needed (optional)
                    try {
                        tvHelperBanner.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        // Banner doesn't exist
                    }
                    
                    // Package details (optional)
                    try {
                        tvPackageType.text = "Documents"
                        tvPackageWeight.text = "2 kg"
                    } catch (e: Exception) {
                        // Fields don't exist
                    }
                }
            }
        })
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvTimer.text = String.format("00:%02d", seconds)
            }

            override fun onFinish() {
                // Auto decline if timer runs out
                findNavController().navigateUp()
            }
        }.start()
    }

    private fun acceptTrip() {
        countDownTimer?.cancel()
        // Navigate to pickup arrival screen (t1.png - Waiting for customer)
        findNavController().navigate(R.id.action_tripRequest_to_pickupArrival)
    }

    private fun declineTrip() {
        countDownTimer?.cancel()
        // Go back to searching trips
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}
