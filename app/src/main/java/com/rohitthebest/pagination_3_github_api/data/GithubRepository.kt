package com.rohitthebest.pagination_3_github_api.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.rohitthebest.pagination_3_github_api.NETWORK_PAGE_SIZE
import com.rohitthebest.pagination_3_github_api.api.GithubService
import com.rohitthebest.pagination_3_github_api.db.RepoDatabase
import com.rohitthebest.pagination_3_github_api.model.Repo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GithubRepository"

@Singleton
class GithubRepository @Inject constructor(
    private val githubService: GithubService,
    private val repoDatabase: RepoDatabase
) {

    fun getSearchResultStream(query: String): Flow<PagingData<Repo>> {

        Log.d(TAG, "getSearchResultStream: new query : $query")

        val dbQuery = "%${query.replace(' ', '%')}%"
        val pagingSourceFactory = { repoDatabase.reposDao().reposByName(dbQuery) }

        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            remoteMediator = GithubRemoteMediator(
                query,
                githubService,
                repoDatabase
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow

    }

}