package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.Common.util.Bakery
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
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            btnEarnings.setOnClickListener {
                Bakery.showToast(requireContext(), "Earnings clicked")
                viewModel.onMenuItemClicked(TripMenuViewModel.MenuAction.EARNINGS)
            }

            btnTrips.setOnClickListener {
                Bakery.showToast(requireContext(), "Trips clicked")
                viewModel.onMenuItemClicked(TripMenuViewModel.MenuAction.PROFILE)
            }

            btnSupport.setOnClickListener {
                Bakery.showToast(requireContext(), "Support clicked")
                viewModel.onMenuItemClicked(TripMenuViewModel.MenuAction.SUPPORT)
            }

            btnSettings.setOnClickListener {
                Bakery.showToast(requireContext(), "Settings clicked")
                viewModel.onMenuItemClicked(TripMenuViewModel.MenuAction.SETTINGS)
            }

            btnLogout.setOnClickListener {
                Bakery.showToast(requireContext(), "Logout clicked")
                viewModel.onMenuItemClicked(TripMenuViewModel.MenuAction.LOGOUT)
            }
        }
    }

    private fun loadData() {
        // Mock data for now
        binding.apply {
            tvDriverName.text = "Driver Name"
            tvDriverPhone.text = "+91 98765 43210"
            tvTodayEarnings.text = "₹1,250"
            tvTotalTrips.text = "45"
            tvCompletedTrips.text = "42"
        }
    }

    private fun observeViewModel() {
        viewModel.menuAction.observe(viewLifecycleOwner, Observer { action ->
            // Handle menu actions when needed
        })
    }
}
