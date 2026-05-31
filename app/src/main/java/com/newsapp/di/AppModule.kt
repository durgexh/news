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
        parser: RssParser
    ): NewsRepository {
        return NewsRepository(parser, apiService)
    }
}
