package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentTripInfoSheetBinding
import com.example.logistics_driver_app.data.model.TripStatus
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.TripActiveViewModel

/**
 * TripInfoSheetFragment - Shows detailed trip information
 * Appears when user clicks the top left red icon (t15.png)
 */
class TripInfoSheetFragment : BaseFragment<FragmentTripInfoSheetBinding>() {

    private val viewModel: TripActiveViewModel by viewModels()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTripInfoSheetBinding {
        return FragmentTripInfoSheetBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            btnCancelTrip.setOnClickListener {
                findNavController().navigate(R.id.action_tripInfoSheet_to_cancelTrip)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.currentTrip.observe(viewLifecycleOwner, Observer { trip ->
            trip?.let {
                binding.apply {
                    tvOrderId.text = getString(R.string.order_id_format, it.orderId)
                    
                    tvTripStatus.text = when (it.status) {
                        TripStatus.TRIP_ASSIGNED -> getString(R.string.trip_assigned)
                        TripStatus.HEADING_TO_PICKUP -> getString(R.string.heading_to_pickup)
                        TripStatus.ARRIVED_AT_PICKUP -> getString(R.string.arrived_at_pickup)
                        TripStatus.STARTED_TRIP -> getString(R.string.trip_started)
                        TripStatus.HEADING_TO_DROP -> getString(R.string.heading_to_drop)
                        TripStatus.ARRIVED_AT_DROP -> getString(R.string.arrived_at_drop)
                        TripStatus.TRIP_COMPLETED -> getString(R.string.trip_completed)
                        TripStatus.TRIP_CANCELLED -> getString(R.string.trip_cancelled)
                    }
                    
                    tvPickupAddress.text = it.pickupAddress
                    tvPickupContactName.text = it.pickupContactName
                    tvPickupContactPhone.text = it.pickupContactPhone
                    
                    tvDropAddress.text = it.dropAddress
                    tvDropContactName.text = it.dropContactName
                    tvDropContactPhone.text = it.dropContactPhone
                    
                    tvItemDescription.text = it.itemDescription ?: "No description"
                    tvDistance.text = "${it.distance} km"
                    tvAmount.text = getString(R.string.rupee_amount, it.amount.toString())
                    
                    tvPaymentMode.text = if (it.paymentMode == "CASH")
                        getString(R.string.cash) else getString(R.string.online)
                }
            }
        })
    }
}
