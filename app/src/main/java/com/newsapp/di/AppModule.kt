package com.newsapp.di

import com.newsapp.data.NewsApiService
import com.newsapp.data.NewsRepository
import com.newsapp.data.RssParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNewsApiService(): NewsApiService {
        return NewsApiService.create()
    }

    @Provides
    @Singleton
    fun provideRssParser(): RssParser {
        return RssParser()
    }

    @Provides
    @Singleton
    fun provideNewsRepository(
        apiService: NewsApiService,
        parser: RssParser,
        newsDao: com.newsapp.data.local.NewsDao
    ): NewsRepository {
        return NewsRepository(parser, apiService, newsDao)
    }

    @Provides
    @Singleton
    fun provideNewsDatabase(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): com.newsapp.data.local.NewsDatabase {
        return androidx.room.Room.databaseBuilder(
            context,
            com.newsapp.data.local.NewsDatabase::class.java,
            "news_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideNewsDao(database: com.newsapp.data.local.NewsDatabase): com.newsapp.data.local.NewsDao {
        return database.newsDao()
    }
}
