package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentTripCompletedBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.TripActiveViewModel

/**
 * TripCompletedFragment - Shows trip completion summary
 */
class TripCompletedFragment : BaseFragment<FragmentTripCompletedBinding>() {

    private val viewModel: TripActiveViewModel by viewModels()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTripCompletedBinding {
        return FragmentTripCompletedBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.apply {
            btnViewEarnings.setOnClickListener {
                // Navigate to earnings screen
                findNavController().navigate(R.id.action_tripCompleted_to_menu)
            }

            btnDone.setOnClickListener {
                // Navigate back to driver home
                findNavController().navigate(R.id.action_tripCompleted_to_driverHome)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.currentTrip.observe(viewLifecycleOwner, Observer { trip ->
            trip?.let {
                binding.apply {
                    tvOrderId.text = getString(R.string.order_id_format, it.orderId)
                    tvDistance.text = "${it.distance} km"
                    tvAmount.text = getString(R.string.rupee_amount, it.amount.toString())
                    tvPaymentMode.text = if (it.paymentMode == "CASH")
                        getString(R.string.cash) else getString(R.string.online)
                }
            }
        })
    }
}
