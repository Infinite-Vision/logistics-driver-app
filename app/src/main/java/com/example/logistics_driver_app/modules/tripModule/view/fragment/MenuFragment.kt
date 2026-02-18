package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.MainActivity
import com.example.logistics_driver_app.databinding.FragmentMenuBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.TripMenuViewModel

/**
 * MenuFragment - Main menu screen showing options and statistics
 * (t19.png to t22.png)
 */
class MenuFragment : BaseFragment<FragmentMenuBinding>() {

    private val viewModel: TripMenuViewModel by viewModels()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMenuBinding {
        return FragmentMenuBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        loadData()
        observeViewModel()
    }

    private fun setupViews() {
        binding.apply {
            btnClose.setOnClickListener {
                findNavController().navigateUp()
            }

            btnEarnings.setOnClickListener {
                Bakery.showToast(requireContext(), "My Earnings clicked")
                viewModel.onMenuItemClicked(TripMenuViewModel.MenuAction.EARNINGS)
            }

            btnTrips.setOnClickListener {
                Bakery.showToast(requireContext(), "Trip History clicked")
                viewModel.onMenuItemClicked(TripMenuViewModel.MenuAction.PROFILE)
            }

            btnBankDetails.setOnClickListener {
                Bakery.showToast(requireContext(), "Bank Details clicked")
            }

            btnLanguage.setOnClickListener {
                Bakery.showToast(requireContext(), "Language clicked")
            }

            btnSettings.setOnClickListener {
                Bakery.showToast(requireContext(), "Settings clicked")
                viewModel.onMenuItemClicked(TripMenuViewModel.MenuAction.SETTINGS)
            }

            btnLogout.setOnClickListener {
                Bakery.showToast(requireContext(), "Logging out...")
                viewModel.logout()
            }
        }
    }

    private fun loadData() {
        // Load driver data from SharedPreference
        val sharedPref = SharedPreference.getInstance(requireContext())
        binding.apply {
            tvDriverName.text = sharedPref.getDriverName().ifEmpty { "Driver Name" }
            tvDriverPhone.text = sharedPref.getPhoneNumber().ifEmpty { "+91 98765 43210" }
            tvDriverRating.text = "4.8" // TODO: Load from API
            tvTotalTrips.text = "1247 trips" // TODO: Load from API
        }
    }

    private fun observeViewModel() {
        viewModel.menuAction.observe(viewLifecycleOwner, Observer { action ->
            // Handle menu actions when needed
        })

        viewModel.logoutSuccess.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                // Restart MainActivity and navigate to language selection
                // MainActivity will detect no session and go to language screen
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
        })
    }
}
