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
import com.example.logistics_driver_app.databinding.FragmentDropArrivalBinding
import com.example.logistics_driver_app.data.model.TripStatus
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.TripDropViewModel

/**
 * DropArrivalFragment - Screen shown when driver arrives at drop location
 * Allows driver to verify OTP, collect payment, and complete trip
 */
class DropArrivalFragment : BaseFragment<FragmentDropArrivalBinding>() {

    private val viewModel: TripDropViewModel by viewModels()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDropArrivalBinding {
        return FragmentDropArrivalBinding.inflate(inflater, container, false)
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

            // Call button
            btnCall.setOnClickListener {
                viewModel.currentTrip.value?.let { trip ->
                    makePhoneCall(trip.dropContactPhone)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.currentTrip.observe(viewLifecycleOwner, Observer { trip ->
            trip?.let {
                binding.apply {
                    tvDropAddress.text = it.dropAddress
                    tvContactName.text = it.dropContactName
                }
            }
        })

        viewModel.otpVerified.observe(viewLifecycleOwner, Observer { verified ->
            if (verified) {
                Bakery.showToast(requireContext(), getString(R.string.otp_verified))
            }
        })

        viewModel.paymentCollected.observe(viewLifecycleOwner, Observer { collected ->
            if (collected) {
                Bakery.showToast(requireContext(), getString(R.string.payment_collected_message))
            }
        })
    }

    private fun makePhoneCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }
}
