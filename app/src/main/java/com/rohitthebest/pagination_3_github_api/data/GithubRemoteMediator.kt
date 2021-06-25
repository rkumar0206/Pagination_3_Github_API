package com.rohitthebest.pagination_3_github_api.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.rohitthebest.pagination_3_github_api.api.GithubService
import com.rohitthebest.pagination_3_github_api.api.IN_QUALIFIER
import com.rohitthebest.pagination_3_github_api.db.RemoteKeys
import com.rohitthebest.pagination_3_github_api.db.RepoDatabase
import com.rohitthebest.pagination_3_github_api.model.Repo
import retrofit2.HttpException
import java.io.IOException


private const val GITHUB_STARTING_PAGE_INDEX = 1


@OptIn(ExperimentalPagingApi::class)
class GithubRemoteMediator(
    private val query: String,
    private val service: GithubService,
    private val repoDatabase: RepoDatabase
) : RemoteMediator<Int, Repo>() {

    override suspend fun initialize(): InitializeAction {

        // Launch remote refresh as soon as paging starts and do not trigger remote prepend or
        // append until refresh has succeeded. In cases where we don't mind showing out-of-date,
        // cached offline data, we can return SKIP_INITIAL_REFRESH instead to prevent paging
        // triggering remote refresh.

        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Repo>): MediatorResult {

        val page = when (loadType) {

            LoadType.APPEND -> {

                val remoteKeys = getRemoteKeyForLastItem(state)

                // If remoteKeys is null, that means the refresh result is not in the database yet.
                // We can return Success with endOfPaginationReached = false because Paging
                // will call this method again if RemoteKeys becomes non-null.
                // If remoteKeys is NOT NULL but its nextKey is null, that means we've reached
                // the end of pagination for append.

                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)

                nextKey
            }

            LoadType.PREPEND -> {

                val remoteKeys = getRemoteKeyForFirstItem(state)

                // If remoteKeys is null, that means the refresh result is not in the database yet.
                // We can return Success with `endOfPaginationReached = false` because Paging
                // will call this method again if RemoteKeys becomes non-null.
                // If remoteKeys is NOT NULL but its prevKey is null, that means we've reached
                // the end of pagination for prepend.

                val prevKey = remoteKeys?.prevKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null
                )

                prevKey
            }

            LoadType.REFRESH -> {

                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)

                // If remoteKey is not null, then we can get the nextKey from it.
                // In the Github API the page keys are incremented sequentially.
                // So to get the page that contains the current item, we just subtract 1
                // from remoteKey.nextKey.
                // If RemoteKey is null (because the anchorPosition was null),
                // then the page we need to load is the initial one: GITHUB_STARTING_PAGE_INDEX

                remoteKeys?.nextKey?.minus(1) ?: GITHUB_STARTING_PAGE_INDEX
            }
        }

        val apiQuery = query + IN_QUALIFIER

        try {

            val response =
                service.searchRepos(apiQuery, GITHUB_STARTING_PAGE_INDEX, state.config.pageSize)

            val repos = response.items

            val endOfPaginationReached = repos.isEmpty()

            repoDatabase.withTransaction {

                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    repoDatabase.remoteKeysDao().clearRemoteKeys()
                    repoDatabase.reposDao().clearRepos()
                }

                val prevKey = if (page == GITHUB_STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                val keys = repos.map {

                    RemoteKeys(it.id, prevKey, nextKey)
                }

                repoDatabase.remoteKeysDao().insertAll(keys)
                repoDatabase.reposDao().insertAll(repos)
            }

            return MediatorResult.Success(endOfPaginationReached)

        } catch (e: IOException) {

            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }

    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Repo>): RemoteKeys? {

        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item

        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { repo ->

                repoDatabase.remoteKeysDao().remoteKeysRepoId(repoId = repo.id)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Repo>): RemoteKeys? {

        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item

        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { repo ->

                repoDatabase.remoteKeysDao().remoteKeysRepoId(repo.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, Repo>): RemoteKeys? {

        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position

        return state.anchorPosition?.let { position ->

            state.closestItemToPosition(position)?.id?.let { repoId ->

                repoDatabase.remoteKeysDao().remoteKeysRepoId(repoId)
            }
        }
    }
}
