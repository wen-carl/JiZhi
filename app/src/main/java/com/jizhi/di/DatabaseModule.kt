package com.jizhi.di

import android.content.Context
import com.jizhi.data.local.JiZhiDatabase
import com.jizhi.data.local.SentenceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 数据库模块
 * 提供数据库和 DAO 实例
 *
 * 注意：Widget 组件无法使用 Hilt 注入，使用 JiZhiDatabase.getInstance() 获取单例
 * 此处直接返回单例，确保 Hilt 注入和手动获取使用同一实例
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * 提供数据库实例
     * 直接使用单例模式，确保与 Widget 组件使用的实例一致
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): JiZhiDatabase {
        return JiZhiDatabase.getInstance(context)
    }

    /**
     * 提供 SentenceDao
     */
    @Provides
    @Singleton
    fun provideSentenceDao(database: JiZhiDatabase): SentenceDao {
        return database.sentenceDao()
    }
}
