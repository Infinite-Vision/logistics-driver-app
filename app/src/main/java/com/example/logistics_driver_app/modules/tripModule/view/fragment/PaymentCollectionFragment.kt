package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentPaymentCollectionBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment

/**
 * PaymentCollectionFragment - Dummy 3-state payment flow.
 *
 * State 0: Loading / Calculating fare (2s auto-advance)
 * State 1: Method select (UPI or Cash)
 * State 2: Cash confirm → navigate to TripCompletedFragment
 */
class PaymentCollectionFragment : BaseFragment<FragmentPaymentCollectionBinding>() {

    private val sharedPreference by lazy { SharedPreference.getInstance(requireContext()) }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPaymentCollectionBinding {
        return FragmentPaymentCollectionBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLoadingState()
        // Auto-advance to method select after 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded && view != null) showMethodState()
        }, 2000)
    }

    // ---- states --------------------------------------------------------

    private fun showLoadingState() {
        binding.loadingSection.visibility = View.VISIBLE
        binding.methodSection.visibility = View.GONE
        binding.cashSection.visibility = View.GONE
    }

    private fun showMethodState() {
        binding.loadingSection.visibility = View.GONE
        binding.methodSection.visibility = View.VISIBLE
        binding.cashSection.visibility = View.GONE

        val fare = sharedPreference.getOrderFare()
        val fareText = if (fare > 0) "₹${"%.0f".format(fare)}" else "₹450"
        binding.tvAmount.text = fareText
        binding.tvOrderId.text = "#ORD${sharedPreference.getOrderId().toString().takeLast(4)}"

        binding.btnPayCash.setOnClickListener { showCashState(fareText) }
        binding.btnPayUpi.setOnClickListener { showCashState(fareText) }  // UPI → go to tripCompleted directly
    }

    private fun showCashState(fareText: String) {
        binding.loadingSection.visibility = View.GONE
        binding.methodSection.visibility = View.GONE
        binding.cashSection.visibility = View.VISIBLE

        binding.tvCashAmount.text = "Collect $fareText in Cash"

        binding.btnConfirmCash.setOnClickListener {
            navigateToCompleted()
        }

        binding.btnChangeMethod.setOnClickListener {
            showMethodState()
        }
    }

    // ---- navigation ----------------------------------------------------

    private fun navigateToCompleted() {
        if (isAdded && view != null) {
            findNavController().navigate(R.id.action_payment_to_tripCompleted)
        }
    }
}
