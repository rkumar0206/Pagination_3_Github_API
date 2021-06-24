package com.rohitthebest.pagination_3_github_api.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rohitthebest.pagination_3_github_api.data.GithubRepository
import com.rohitthebest.pagination_3_github_api.model.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SearchRepositoryViewModel @Inject constructor(
    private val githubRepository: GithubRepository
) : ViewModel() {

    private var currentQueryValue: String? = null
    private var currentQueryResult: Flow<PagingData<Repo>>? = null

    fun searchRepo(queryString: String): Flow<PagingData<Repo>> {

        val lastResult = currentQueryResult

        if (queryString == currentQueryValue && lastResult != null) {

            return lastResult
        }

        currentQueryValue = queryString

        // Flow<PagingData> has a handy cachedIn() method
        // that allows us to cache the content of a Flow<PagingData> in a CoroutineScope
        // Since we're in a ViewModel, we will use the androidx.lifecycle.viewModelScope.
        val newResult = githubRepository.getSearchResultStream(queryString).cachedIn(viewModelScope)

        currentQueryResult = newResult

        return newResult
    }

}