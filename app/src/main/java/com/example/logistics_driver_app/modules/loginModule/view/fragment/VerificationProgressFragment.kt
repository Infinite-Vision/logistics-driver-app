package com.example.logistics_driver_app.modules.loginModule.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentVerificationProgressBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.loginModule.viewModel.AppStateViewModel

/**
 * VerificationProgressFragment - Acts as a routing checkpoint on app restart,
 * and also shows the "verification pending" screen for drivers awaiting admin approval.
 */
class VerificationProgressFragment : BaseFragment<FragmentVerificationProgressBinding>() {

    companion object {
        private const val TAG = "VerificationProgress"
    }

    private val appStateViewModel: AppStateViewModel by viewModels()
    private var hasNavigated = false

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVerificationProgressBinding {
        return FragmentVerificationProgressBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeAppState()

        // Always call getAppState to determine correct destination.
        // If the API says owner_details / vehicle_details / home etc. we navigate away.
        // Only remain here when onboardingStatus == PENDING (awaiting admin approval).
        Log.d(TAG, "[ROUTING] Calling getAppState to determine correct screen...")
        appStateViewModel.getAppState()
    }

    private fun observeAppState() {
        appStateViewModel.nextScreen.observe(viewLifecycleOwner) { nextScreen ->
            Log.d(TAG, "[ROUTING] nextScreen received: '$nextScreen', hasNavigated: $hasNavigated")
            if (hasNavigated || nextScreen.isNullOrEmpty()) return@observe
            routeToScreen(nextScreen)
        }

        appStateViewModel.onboardingStatus.observe(viewLifecycleOwner) { status ->
            Log.d(TAG, "[ROUTING] onboardingStatus: '$status'")
        }

        appStateViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            // If app state call fails, stay on this screen — may be a temp network issue
            if (!error.isNullOrEmpty()) {
                Log.e(TAG, "[ROUTING] getAppState error: $error — staying on screen")
                binding.root.visibility = View.VISIBLE
            }
        }

        appStateViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Dim the UI while routing is in progress
            binding.root.alpha = if (isLoading) 0.3f else 1.0f
        }
    }

    /**
     * Routes to the correct screen based on nextScreen value from /api/v1/app/state.
     * Only stays on VerificationProgressFragment when status is truly PENDING (admin review).
     */
    private fun routeToScreen(nextScreen: String) {
        if (!isAdded || isDetached || isRemoving || activity?.isFinishing == true) return

        val key = nextScreen.uppercase().replace("-", "_")
        Log.d(TAG, "[ROUTING] Routing to: $key")

        hasNavigated = true
        binding.root.alpha = 1.0f

        try {
            when {
                key.contains("OWNER") -> {
                    Log.d(TAG, "[ROUTING] -> Owner Details")
                    findNavController().navigate(R.id.action_verificationProgress_to_ownerDetails)
                }
                key.contains("VEHICLE") -> {
                    Log.d(TAG, "[ROUTING] -> Vehicle Details")
                    findNavController().navigate(R.id.action_verificationProgress_to_vehicleDetails)
                }
                key.contains("DRIVER") && !key.contains("HOME") -> {
                    Log.d(TAG, "[ROUTING] -> Driver Details")
                    findNavController().navigate(R.id.action_verificationProgress_to_driverDetails)
                }
                key == "HOME" || key == "DASHBOARD" -> {
                    Log.d(TAG, "[ROUTING] -> Driver Home")
                    findNavController().navigate(R.id.action_verificationProgress_to_tripHome)
                }
                key.contains("VERIFICATION") || key.contains("PENDING") -> {
                    // Genuinely pending admin approval — stay on this screen
                    Log.d(TAG, "[ROUTING] Status is PENDING — showing verification progress screen")
                    hasNavigated = false
                    binding.root.alpha = 1.0f
                }
                else -> {
                    Log.w(TAG, "[ROUTING] Unknown nextScreen '$nextScreen' — defaulting to Owner Details")
                    findNavController().navigate(R.id.action_verificationProgress_to_ownerDetails)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "[ROUTING] Navigation failed", e)
            hasNavigated = false
        }
    }

    private fun setupViews() {
        binding.apply {
            btnContactSupport.setOnClickListener {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Contact Support: +91-1234567890",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }

            tvSkipDemo.setOnClickListener {
                findNavController().navigate(R.id.action_verificationProgress_to_tripHome)
            }
        }
    }
}
