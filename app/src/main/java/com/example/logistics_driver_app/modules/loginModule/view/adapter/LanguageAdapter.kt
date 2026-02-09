package com.example.logistics_driver_app.modules.loginModule.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.data.model.Language
import com.example.logistics_driver_app.databinding.ItemLanguageBinding

/**
 * LanguageAdapter - RecyclerView adapter for language selection.
 * Displays list of available languages with selection state.
 */
class LanguageAdapter(
    private val onLanguageSelected: (Language) -> Unit
) : ListAdapter<Language, LanguageAdapter.LanguageViewHolder>(LanguageDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LanguageViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class LanguageViewHolder(
        private val binding: ItemLanguageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(language: Language) {
            binding.apply {
                tvLanguageName.text = language.name
                tvNativeName.text = language.nativeName
                tvFlag.text = language.flagEmoji
                
                // Set selection state
                cardLanguage.strokeWidth = if (language.isSelected) 4 else 2
                cardLanguage.strokeColor = if (language.isSelected) {
                    binding.root.context.getColor(R.color.primary)
                } else {
                    binding.root.context.getColor(R.color.border_light)
                }
                
                // Show/hide checkmark
                ivCheckmark.visibility = if (language.isSelected) View.VISIBLE else View.GONE
                
                // Set click listeners
                root.setOnClickListener {
                    onLanguageSelected(language)
                }
            }
        }
    }
    
    class LanguageDiffCallback : DiffUtil.ItemCallback<Language>() {
        override fun areItemsTheSame(oldItem: Language, newItem: Language): Boolean {
            return oldItem.code == newItem.code
        }
        
        override fun areContentsTheSame(oldItem: Language, newItem: Language): Boolean {
            return oldItem == newItem
        }
    }
}
