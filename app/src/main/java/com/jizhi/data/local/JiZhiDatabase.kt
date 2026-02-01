package com.jizhi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * 今日诗词数据库类
 * 管理句子数据的本地存储
 */
@Database(
    entities = [SentenceEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class JiZhiDatabase : RoomDatabase() {

    /**
     * 获取句子数据访问对象
     */
    abstract fun sentenceDao(): SentenceDao

    companion object {
        private const val DATABASE_NAME = "jizhi_database.db"

        @Volatile
        private var INSTANCE: JiZhiDatabase? = null

        /**
         * 获取数据库单例实例
         */
        fun getInstance(context: Context): JiZhiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JiZhiDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
