package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentPaymentCollectionBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.PaymentViewModel

/**
 * PaymentCollectionFragment - Screen for collecting payment from customer
 * (tp1.png and tp2.png)
 */
class PaymentCollectionFragment : BaseFragment<FragmentPaymentCollectionBinding>() {

    private val viewModel: PaymentViewModel by viewModels()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPaymentCollectionBinding {
        return FragmentPaymentCollectionBinding.inflate(inflater, container, false)
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

            btnConfirmPayment.setOnClickListener {
                viewModel.currentTrip.value?.let { trip ->
                    if (trip.paymentMode == "CASH") {
                        viewModel.confirmCashPayment(trip.amount)
                    } else {
                        viewModel.confirmOnlinePayment("ONLINE_TXN_${System.currentTimeMillis()}")
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.currentTrip.observe(viewLifecycleOwner, Observer { trip ->
            trip?.let {
                binding.apply {
                    tvAmount.text = getString(R.string.rupee_amount, it.amount.toString())
                    tvOrderId.text = getString(R.string.order_id_format, it.orderId)
                    tvCustomerName.text = it.dropContactName
                    tvDistance.text = "${it.distance} km"
                    
                    tvPaymentMode.text = if (it.paymentMode == "CASH")
                        getString(R.string.cash_payment).uppercase()
                    else
                        getString(R.string.online_payment).uppercase()
                }
            }
        })

        viewModel.paymentConfirmed.observe(viewLifecycleOwner, Observer { confirmed ->
            if (confirmed) {
                Bakery.showToast(requireContext(), getString(R.string.payment_collected_message))
                findNavController().navigateUp()
            }
        })
    }
}
