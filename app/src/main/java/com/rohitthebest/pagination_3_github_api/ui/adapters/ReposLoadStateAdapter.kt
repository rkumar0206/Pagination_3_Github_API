package com.rohitthebest.pagination_3_github_api.ui.adapters

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import com.rohitthebest.pagination_3_github_api.ui.viewHolders.ReposLoadStateViewHolder

class ReposLoadStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<ReposLoadStateViewHolder>() {

    override fun onBindViewHolder(holder: ReposLoadStateViewHolder, loadState: LoadState) {

        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): ReposLoadStateViewHolder {

        return ReposLoadStateViewHolder.create(parent, retry)
    }


}