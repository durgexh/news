package com.newsapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM news_articles WHERE category = :category AND country = :country ORDER BY timestamp DESC")
    fun getNewsByCategoryAndCountry(category: String, country: String): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news_articles WHERE category = :category AND country = :city ORDER BY timestamp DESC")
    fun getLocalNews(city: String, category: String = "Local \uD83D\uDCCD"): Flow<List<NewsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: List<NewsEntity>)

    @Query("DELETE FROM news_articles WHERE category = :category AND country = :country")
    suspend fun clearNewsByCategoryAndCountry(category: String, country: String)
    
    @Query("DELETE FROM news_articles WHERE category = :category AND country = :city")
    suspend fun clearLocalNews(city: String, category: String = "Local \uD83D\uDCCD")
}
