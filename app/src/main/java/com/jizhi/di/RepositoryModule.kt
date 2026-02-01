package com.jizhi.di

import android.content.Context
import com.jizhi.data.local.SentenceDao
import com.jizhi.data.remote.JinrishiciApiService
import com.jizhi.data.remote.JinrishiciClient
import com.jizhi.repository.SentenceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Repository 模块
 * 提供 Repository 实例
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * 提供 SentenceRepository
     */
    @Provides
    @Singleton
    fun provideSentenceRepository(
        @ApplicationContext context: Context,
        sentenceDao: SentenceDao,
        apiService: JinrishiciApiService
    ): SentenceRepository {
        return SentenceRepository(context, sentenceDao, apiService)
    }

    /**
     * 提供 JinrishiciClient
     */
    @Provides
    @Singleton
    fun provideJinrishiciClient(): JinrishiciClient {
        return JinrishiciClient
    }
}
