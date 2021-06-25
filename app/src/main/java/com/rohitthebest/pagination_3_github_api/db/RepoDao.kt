package com.rohitthebest.pagination_3_github_api.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rohitthebest.pagination_3_github_api.model.Repo

@Dao
interface RepoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(repos: List<Repo>)

    @Query("DELETE FROM repos")
    suspend fun clearRepos()

    @Query(
        "SELECT * FROM repos WHERE " +
                "name LIKE :queryString OR description LIKE :queryString " +
                "ORDER BY stars DESC, name ASC"
    )
    fun reposByName(queryString: String): PagingSource<Int, Repo>

}