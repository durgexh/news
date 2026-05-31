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

class NewsRepository(
    private val parser: RssParser = RssParser(),
    private val apiService: NewsApiService = NewsApiService.create()
) {
    fun fetchNewsStream(sources: List<Pair<String, String>>): Flow<Result<List<NewsItem>>> = channelFlow {
        val allItems = mutableListOf<NewsItem>()
        val mutex = Mutex()
        
        val jobs = sources.map { source ->
            launch {
                try {
                    val fetchedItems = parser.fetchFeed(source.first, source.second)
                    if (fetchedItems.isNotEmpty()) {
                        val merged = mutex.withLock {
                            allItems.addAll(fetchedItems)
                            groupSimilarNews(allItems)
                        }
                        send(Result.success(merged.sortedByDescending { it.pubDate }))
                    }
                } catch (e: Exception) {
                    // Ignore single feed failures
                    e.printStackTrace()
                }
            }
        }
        
        jobs.forEach { it.join() }
        
        if (allItems.isEmpty()) {
            send(Result.failure(Exception("Could not fetch any news. Please check your connection.")))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun fetchLocalNews(city: String): Result<List<NewsItem>> = withContext(Dispatchers.IO) {
        try {
            val responseBody = apiService.getRssFeed("rss/search?q=${city.replace(" ", "+")}+news")
            val fetchedItems = parser.parseFeed(responseBody.byteStream(), "$city News", URL("https://news.google.com/"))
            if (fetchedItems.isNotEmpty()) {
                Result.success(fetchedItems.sortedByDescending { it.pubDate })
            } else {
                Result.failure(Exception("No local news found for $city."))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Failed to load local news: ${e.message}"))
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
