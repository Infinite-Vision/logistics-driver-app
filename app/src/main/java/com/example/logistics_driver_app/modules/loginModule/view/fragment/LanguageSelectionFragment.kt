package com.example.logistics_driver_app.modules.loginModule.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.logistics_driver_app.Common.util.Bakery
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentLanguageSelectionBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.loginModule.view.adapter.LanguageAdapter
import com.example.logistics_driver_app.modules.loginModule.viewModel.LanguageViewModel

/**
 * LanguageSelectionFragment - Language selection screen.
 * Allows users to select preferred language (English/Hindi).
 */
class LanguageSelectionFragment : BaseFragment<FragmentLanguageSelectionBinding>() {
    
    private val viewModel: LanguageViewModel by viewModels()
    private lateinit var languageAdapter: LanguageAdapter
    
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLanguageSelectionBinding {
        return FragmentLanguageSelectionBinding.inflate(inflater, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeViewModel()
        viewModel.loadLanguages()
    }
    
    private fun setupRecyclerView() {
        languageAdapter = LanguageAdapter { language ->
            viewModel.selectLanguage(language)
        }
        
        binding.rvLanguages.apply {
            // GridLayoutManager is already set in XML, just set adapter
            adapter = languageAdapter
        }
    }
    
    private fun observeViewModel() {
        viewModel.languages.observe(viewLifecycleOwner, Observer { languages ->
            languageAdapter.submitList(languages)
        })
        
        viewModel.selectedLanguage.observe(viewLifecycleOwner, Observer { language ->
            language?.let {
                Bakery.showToast(requireContext(), getString(R.string.language_selected, it.name))
            }
        })
        
        binding.btnContinue.setOnClickListener {
            if (viewModel.isLanguageSelected()) {
                navigateToPhoneEntry()
            } else {
                Bakery.showToast(requireContext(), getString(R.string.please_select_language))
            }
        }
    }
    
    private fun navigateToPhoneEntry() {
        findNavController().navigate(
            R.id.action_languageSelectionFragment_to_phoneEntryFragment
        )
    }
}
