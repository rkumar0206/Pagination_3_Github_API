package com.rohitthebest.pagination_3_github_api.module

import android.content.Context
import androidx.room.Room
import com.rohitthebest.pagination_3_github_api.GITHUB_BASE_URL
import com.rohitthebest.pagination_3_github_api.api.GithubService
import com.rohitthebest.pagination_3_github_api.db.RemoteKeysDao
import com.rohitthebest.pagination_3_github_api.db.RepoDao
import com.rohitthebest.pagination_3_github_api.db.RepoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {


    @Provides
    @Singleton
    fun provideGithubHttpClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor()
        logger.setLevel(HttpLoggingInterceptor.Level.BASIC)

        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()
    }

    @Singleton
    @Provides
    fun provideGithubRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(GITHUB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Singleton
    @Provides
    fun provideGithubService(
        retrofit: Retrofit
    ): GithubService = retrofit.create(GithubService::class.java)


    //=============================== Repo Database ==============================

    @Singleton
    @Provides
    fun provideRepoDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        RepoDatabase::class.java,
        "Github.db"
    )
        .build()

    @Singleton
    @Provides
    fun provideRepoDao(
        repoDatabase: RepoDatabase
    ): RepoDao = repoDatabase.reposDao()


    @Singleton
    @Provides
    fun provideRemoteDao(
        repoDatabase: RepoDatabase
    ): RemoteKeysDao = repoDatabase.remoteKeysDao()
}