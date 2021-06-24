package com.rohitthebest.pagination_3_github_api.data

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.rohitthebest.pagination_3_github_api.NETWORK_PAGE_SIZE
import com.rohitthebest.pagination_3_github_api.api.GithubService
import com.rohitthebest.pagination_3_github_api.model.Repo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private const val TAG = "GithubRepository"

class GithubRepository @Inject constructor(
    private val githubService: GithubService
) {

    fun getSearchResultStream(query: String): Flow<PagingData<Repo>> {

        Log.d(TAG, "getSearchResultStream: new query : $query")

        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false,
                maxSize = 100
            ),
            pagingSourceFactory = { GithubPagingSource(githubService, query) }
        ).flow

    }

}