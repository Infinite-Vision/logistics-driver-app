package com.example.logistics_driver_app.ui.auth.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentLanguageSelectionBinding
import com.example.logistics_driver_app.utils.CommonUtils

/**
 * Fragment for language selection screen.
 * Allows users to select their preferred language during onboarding.
 */
class LanguageSelectionFragment : Fragment() {

    private var _binding: FragmentLanguageSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LanguageViewModel
    private lateinit var adapter: LanguageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[LanguageViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    /**
     * Setup RecyclerView with language options.
     */
    private fun setupRecyclerView() {
        adapter = LanguageAdapter { language ->
            viewModel.selectLanguage(language)
        }

        binding.rvLanguages.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvLanguages.adapter = adapter

        // Load languages
        adapter.submitList(viewModel.getLanguages())
    }

    /**
     * Setup LiveData observers.
     */
    private fun setupObservers() {
        viewModel.selectedLanguage.observe(viewLifecycleOwner) { language ->
            adapter.setSelectedLanguage(language)
        }
    }

    /**
     * Setup click listeners for UI elements.
     */
    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            val selectedLang = viewModel.selectedLanguage.value
            if (selectedLang != null) {
                // Save language and navigate to phone entry
                viewModel.saveLanguagePreference(requireContext())
                navigateToPhoneEntry()
            } else {
                CommonUtils.showToast(requireContext(), getString(R.string.please_select_language))
            }
        }
    }

    /**
     * Navigate to phone number entry screen.
     */
    private fun navigateToPhoneEntry() {
        findNavController().navigate(R.id.action_languageSelection_to_phoneEntry)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
