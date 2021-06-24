package com.rohitthebest.pagination_3_github_api.ui.viewHolders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.pagination_3_github_api.databinding.SeparatorViewItemBinding

class SeparatorViewHolder(private val binding: SeparatorViewItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(separatorText: String) {
        binding.separatorDescription.text = separatorText
    }

    companion object {
        fun create(parent: ViewGroup): SeparatorViewHolder {

            val binding =
                SeparatorViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

            return SeparatorViewHolder(binding)
        }
    }
}