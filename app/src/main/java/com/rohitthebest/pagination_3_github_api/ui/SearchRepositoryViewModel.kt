package com.rohitthebest.pagination_3_github_api.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.rohitthebest.pagination_3_github_api.data.GithubRepository
import com.rohitthebest.pagination_3_github_api.model.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class SearchRepositoryViewModel @Inject constructor(
    private val githubRepository: GithubRepository
) : ViewModel() {

    private var currentQueryValue: String? = null
    private var currentQueryResult: Flow<PagingData<UiModel>>? = null

    fun searchRepo(queryString: String): Flow<PagingData<UiModel>> {

        val lastResult = currentQueryResult

        if (queryString == currentQueryValue && lastResult != null) {

            return lastResult
        }

        currentQueryValue = queryString

        // Flow<PagingData> has a handy cachedIn() method
        // that allows us to cache the content of a Flow<PagingData> in a CoroutineScope
        // Since we're in a ViewModel, we will use the androidx.lifecycle.viewModelScope.
        val newResult = githubRepository.getSearchResultStream(queryString)
            .map { pagingData ->
                pagingData.map { UiModel.RepoItem(it) }
            }
            .map {

                it.insertSeparators<UiModel.RepoItem, UiModel> { before, after ->

                    if(after == null) {
                        //we are at the end of the list
                        return@insertSeparators null
                    }

                    if(before == null) {

                        //we are at the beginning of the list
                        return@insertSeparators UiModel.SeparatorItem("${after.roundedStarCount}0,000+ stars")
                    }

                    // check between 2 items
                    if (before.roundedStarCount > after.roundedStarCount) {
                        if (after.roundedStarCount >= 1) {
                            UiModel.SeparatorItem("${after.roundedStarCount}0,000+ stars")
                        } else {
                            UiModel.SeparatorItem("< 10,000+ stars")
                        }
                    } else {
                        // no separator
                        null
                    }

                }
            }
            .cachedIn(viewModelScope)

        currentQueryResult = newResult

        return newResult
    }

}

sealed class UiModel {
    data class RepoItem(val repo: Repo) : UiModel()
    data class SeparatorItem(val description: String) : UiModel()
}

private val UiModel.RepoItem.roundedStarCount: Int get() = this.repo.stars / 10_000