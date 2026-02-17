package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentCancelTripBinding
import com.example.logistics_driver_app.data.model.CancellationReasons
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.CancellationViewModel

/**
 * CancelTripFragment - Screen for cancelling a trip with reason selection
 * (tc1.png and tc2.png) - Note: Top right cancel button is visible
 */
class CancelTripFragment : BaseFragment<FragmentCancelTripBinding>() {

    private val viewModel: CancellationViewModel by viewModels()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCancelTripBinding {
        return FragmentCancelTripBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.apply {
            // Setup cancellation reasons dropdown
            val reasons = CancellationReasons.reasons.map { it.reason }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                reasons
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCancelReason.adapter = adapter

            // Back button
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            // Top right cancel button (visible as per requirement)
            btnCancel.setOnClickListener {
                findNavController().navigateUp()
            }

            // Submit cancellation
            btnSubmit.setOnClickListener {
                val selectedPosition = spinnerCancelReason.selectedItemPosition
                val selectedReason = CancellationReasons.reasons[selectedPosition].reason
                val additionalNotes = etAdditionalNotes.text.toString().trim()

                val fullReason = if (additionalNotes.isNotEmpty()) {
                    "$selectedReason - $additionalNotes"
                } else {
                    selectedReason
                }

                viewModel.submitCancellation(fullReason, additionalNotes)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.currentTrip.observe(viewLifecycleOwner, Observer { trip ->
            trip?.let {
                binding.apply {
                    tvOrderId.text = getString(R.string.order_id_format, it.orderId)
                    tvAmount.text = getString(R.string.rupee_amount, it.amount.toString())
                }
            }
        })

        viewModel.cancellationSuccess.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                Bakery.showToast(requireContext(), getString(R.string.trip_cancelled_message))
                // Navigate back to home or trip list
                findNavController().navigate(R.id.action_cancelTrip_to_tripActive)
            }
        })
    }
}
