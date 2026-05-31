package com.newsapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.newsapp.data.NewsItem
import com.newsapp.data.SourceInfo

@Entity(tableName = "news_articles")
data class NewsEntity(
    @PrimaryKey
    val link: String,
    val title: String,
    val sources: List<SourceInfo>,
    val imageUrl: String?,
    val pubDate: String,
    val category: String,
    val country: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toNewsItem(): NewsItem {
        return NewsItem(
            title = title,
            link = link,
            sources = sources,
            imageUrl = imageUrl,
            pubDate = pubDate
        )
    }
}
