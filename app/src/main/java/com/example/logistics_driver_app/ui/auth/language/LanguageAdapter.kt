package com.example.logistics_driver_app.ui.auth.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.ItemLanguageBinding

/**
 * RecyclerView adapter for language selection items.
 * Displays language options in a grid layout.
 */
class LanguageAdapter(
    private val onLanguageClick: (String) -> Unit
) : ListAdapter<Map<String, String>, LanguageAdapter.LanguageViewHolder>(LanguageDiffCallback()) {

    private var selectedLanguageCode: String? = null

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

    /**
     * Set the currently selected language and refresh the view.
     * @param languageCode Selected language code
     */
    fun setSelectedLanguage(languageCode: String?) {
        selectedLanguageCode = languageCode
        notifyDataSetChanged()
    }

    inner class LanguageViewHolder(
        private val binding: ItemLanguageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Bind language data to the view.
         * @param language Language data map
         */
        fun bind(language: Map<String, String>) {
            val code = language["code"] ?: ""
            val name = language["name"] ?: ""
            val nativeName = language["nativeName"] ?: ""
            val flag = language["flag"] ?: "🇮🇳"

            binding.tvFlag.text = flag
            binding.tvLanguageName.text = name
            binding.tvNativeName.text = nativeName

            // Highlight selected language
            val isSelected = code == selectedLanguageCode
            if (isSelected) {
                binding.cardLanguage.strokeWidth = 6
                binding.cardLanguage.strokeColor = ContextCompat.getColor(
                    binding.root.context,
                    R.color.primary
                )
            } else {
                binding.cardLanguage.strokeWidth = 2
                binding.cardLanguage.strokeColor = ContextCompat.getColor(
                    binding.root.context,
                    R.color.border_light
                )
            }

            // Click listener
            binding.cardLanguage.setOnClickListener {
                onLanguageClick(code)
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     */
    private class LanguageDiffCallback : DiffUtil.ItemCallback<Map<String, String>>() {
        override fun areItemsTheSame(
            oldItem: Map<String, String>,
            newItem: Map<String, String>
        ): Boolean {
            return oldItem["code"] == newItem["code"]
        }

        override fun areContentsTheSame(
            oldItem: Map<String, String>,
            newItem: Map<String, String>
        ): Boolean {
            return oldItem == newItem
        }
    }
}
