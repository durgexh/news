package com.newsapp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.net.URL

import javax.inject.Inject

import com.newsapp.data.local.NewsDao
import com.newsapp.data.local.NewsEntity
import kotlinx.coroutines.flow.map

import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val parser: RssParser,
    private val apiService: NewsApiService,
    private val newsDao: NewsDao
) {
    fun observeNews(category: String, country: String): Flow<List<NewsItem>> {
        return newsDao.getNewsByCategoryAndCountry(category, country).map { entities ->
            entities.map { it.toNewsItem() }
        }
    }

    fun observeLocalNews(city: String): Flow<List<NewsItem>> {
        return newsDao.getLocalNews(city, "Local \uD83D\uDCCD").map { entities ->
            entities.map { it.toNewsItem() }
        }
    }

    suspend fun refreshNews(sources: List<Pair<String, String>>, category: String, country: String) = withContext(Dispatchers.IO) {
        val allItems = mutableListOf<NewsItem>()
        val mutex = Mutex()
        
        val jobs = sources.map { source ->
            launch {
                try {
                    val fetchedItems = parser.fetchFeed(source.first, source.second)
                    if (fetchedItems.isNotEmpty()) {
                        mutex.withLock {
                            allItems.addAll(fetchedItems)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        jobs.forEach { it.join() }
        
        if (allItems.isNotEmpty()) {
            val merged = groupSimilarNews(allItems).sortedByDescending { it.pubDate }
            val entities = merged.map { item ->
                NewsEntity(
                    link = item.link,
                    title = item.title,
                    sources = item.sources,
                    imageUrl = item.imageUrl,
                    pubDate = item.pubDate,
                    category = category,
                    country = country
                )
            }
            // Clear old cache and insert new data
            newsDao.clearNewsByCategoryAndCountry(category, country)
            newsDao.insertNews(entities)
        } else {
            throw Exception("Could not fetch any news. Please check your connection.")
        }
    }

    suspend fun refreshLocalNews(city: String) = withContext(Dispatchers.IO) {
        try {
            val responseBody = apiService.getRssFeed("rss/search?q=${city.replace(" ", "+")}+news")
            val fetchedItems = parser.parseFeed(responseBody.byteStream(), "$city News", URL("https://news.google.com/"))
            if (fetchedItems.isNotEmpty()) {
                val sorted = fetchedItems.sortedByDescending { it.pubDate }
                val entities = sorted.map { item ->
                    NewsEntity(
                        link = item.link,
                        title = item.title,
                        sources = item.sources,
                        imageUrl = item.imageUrl,
                        pubDate = item.pubDate,
                        category = "Local \uD83D\uDCCD",
                        country = city
                    )
                }
                newsDao.clearLocalNews(city, "Local \uD83D\uDCCD")
                newsDao.insertNews(entities)
            } else {
                throw Exception("No local news found for $city.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to load local news: ${e.message}")
        }
    }

    private fun groupSimilarNews(items: List<NewsItem>): List<NewsItem> {
        val groupedList = mutableListOf<NewsItem>()
        for (item in items) {
            var matched = false
            for (i in groupedList.indices) {
                val existingItem = groupedList[i]
                if (isSimilar(item.title, existingItem.title)) {
                    val updatedSources = (existingItem.sources + item.sources).distinctBy { it.name }
                    groupedList[i] = existingItem.copy(sources = updatedSources)
                    matched = true
                    break
                }
            }
            if (!matched) {
                groupedList.add(item)
            }
        }
        return groupedList
    }

    private fun isSimilar(title1: String, title2: String): Boolean {
        val words1 = title1.lowercase().split("\\W+".toRegex()).filter { it.length > 3 }.toSet()
        val words2 = title2.lowercase().split("\\W+".toRegex()).filter { it.length > 3 }.toSet()
        
        val intersection = words1.intersect(words2).size
        val minSize = minOf(words1.size, words2.size)
        
        if (minSize == 0) return false
        return intersection.toDouble() / minSize > 0.6
    }
}
