package com.rohitthebest.pagination_3_github_api.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.rohitthebest.pagination_3_github_api.databinding.ActivitySearchRepositoriesBinding
import com.rohitthebest.pagination_3_github_api.ui.adapters.ReposAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchRepositoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchRepositoriesBinding
    private val viewModel by viewModels<SearchRepositoryViewModel>()
    private var searchJob: Job? = null
    private val adapter = ReposAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchRepositoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // add dividers between RecyclerView's row items
        val decorations = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.list.addItemDecoration(decorations)

        initAdapter()
        val query = savedInstanceState?.getString(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY
        search(query)
        initSearch(query)
    }

    private fun search(query: String) {

        //Cancelling previous job before creating a new one
        searchJob?.cancel()

        searchJob = lifecycleScope.launch {

            viewModel.searchRepo(query).collectLatest {

                adapter.submitData(it)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)

        outState.putString(LAST_SEARCH_QUERY, binding.searchRepo.text.trim().toString())
    }

    private fun initAdapter() {

        binding.list.adapter = this.adapter
    }

    private fun initSearch(query: String) {

        binding.searchRepo.setText(query)

        binding.searchRepo.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_GO) {
                updateRepoListFromInput()
                true
            } else {
                false
            }
        }

        binding.searchRepo.setOnKeyListener { _, keyCode, event ->

            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateRepoListFromInput()
                true
            } else {
                false
            }
        }

        // scrolling the recycler view to the top position when the new list is requested

        lifecycleScope.launch {

            adapter.loadStateFlow
                // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy {
                    it.refresh
                }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .filter {
                    it.refresh is LoadState.NotLoading
                }
                .collect {
                    binding.list.scrollToPosition(0)
                }
        }
    }

    private fun updateRepoListFromInput() {

        binding.searchRepo.text.trim().let {
            if (it.isNotEmpty()) {
                search(it.toString())
            }
        }
    }

    private fun showEmptyList(show: Boolean) {
        if (show) {
            binding.emptyList.visibility = View.VISIBLE
            binding.list.visibility = View.GONE
        } else {
            binding.emptyList.visibility = View.GONE
            binding.list.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val LAST_SEARCH_QUERY: String = "last_search_query"
        private const val DEFAULT_QUERY = "Android"
    }

}