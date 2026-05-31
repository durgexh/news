package com.newsapp.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class RssParser {

    suspend fun fetchFeed(urlString: String, sourceName: String): List<NewsItem> = withContext(Dispatchers.IO) {
        val newsItems = mutableListOf<NewsItem>()
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.readTimeout = 8000
            connection.connectTimeout = 8000
            connection.requestMethod = "GET"
            // Some feeds require a user agent
            connection.setRequestProperty("User-Agent", "Mozilla/5.0") 
            connection.connect()
            
            val inputStream: InputStream = connection.inputStream
            newsItems.addAll(parseFeed(inputStream, sourceName, url))
        } catch (e: Exception) {
            Log.e("RssParser", "Error fetching feed: $urlString", e)
        }
        newsItems
    }

    suspend fun parseFeed(inputStream: InputStream, sourceName: String, url: URL?): List<NewsItem> = withContext(Dispatchers.IO) {
        val newsItems = mutableListOf<NewsItem>()
        try {
            // Extract domain for favicon
            val domain = url?.host ?: "google.com"
            val logoUrl = "https://www.google.com/s2/favicons?domain=$domain&sz=128"
            val sourceInfo = SourceInfo(name = sourceName, logoUrl = logoUrl)
            
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)

            var eventType = parser.eventType
            var currentTitle: String? = null
            var currentLink: String? = null
            var currentPubDate: String? = null
            var currentImageUrl: String? = null
            var insideItem = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val nodeName = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (nodeName.equals("item", ignoreCase = true) || nodeName.equals("entry", ignoreCase = true)) {
                            insideItem = true
                        } else if (insideItem) {
                            if (nodeName.equals("title", ignoreCase = true)) {
                                currentTitle = parser.nextText()
                            } else if (nodeName.equals("link", ignoreCase = true)) {
                                val linkText = parser.nextText()
                                if (linkText.isNotBlank()) {
                                    currentLink = linkText
                                } else {
                                    // Handle Atom feeds <link href="...">
                                    val href = parser.getAttributeValue(null, "href")
                                    if (href != null) currentLink = href
                                }
                            } else if (nodeName.equals("pubDate", ignoreCase = true) || nodeName.equals("published", ignoreCase = true) || nodeName.equals("updated", ignoreCase = true)) {
                                currentPubDate = parser.nextText()
                            } else if (nodeName.equals("content", ignoreCase = true) && parser.prefix == "media") {
                                // <media:content url="...">
                                currentImageUrl = parser.getAttributeValue(null, "url")
                            } else if (nodeName.equals("enclosure", ignoreCase = true)) {
                                // <enclosure url="..." type="image/jpeg">
                                val type = parser.getAttributeValue(null, "type")
                                if (type != null && type.startsWith("image")) {
                                    currentImageUrl = parser.getAttributeValue(null, "url")
                                }
                            } else if (nodeName.equals("thumbnail", ignoreCase = true) && parser.prefix == "media") {
                                // <media:thumbnail url="...">
                                if (currentImageUrl == null) {
                                    currentImageUrl = parser.getAttributeValue(null, "url")
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (nodeName.equals("item", ignoreCase = true) || nodeName.equals("entry", ignoreCase = true)) {
                            if (currentTitle != null && currentLink != null) {
                                val formattedDate = formatDate(currentPubDate)
                                newsItems.add(
                                    NewsItem(
                                        title = currentTitle.trim(),
                                        link = currentLink.trim(),
                                        sources = listOf(sourceInfo),
                                        imageUrl = currentImageUrl,
                                        pubDate = formattedDate
                                    )
                                )
                            }
                            insideItem = false
                            currentTitle = null
                            currentLink = null
                            currentPubDate = null
                            currentImageUrl = null
                        }
                    }
                }
                eventType = parser.next()
            }
            inputStream.close()
        } catch (e: Exception) {
            Log.e("RssParser", "Error parsing feed", e)
        }
        newsItems
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "Unknown Date"
        
        // Try multiple common RSS formts
        val formats = listOf(
            "EEE, dd MMM yyyy HH:mm:ss zzz", // RFC 822
            "EEE, dd MMM yyyy HH:mm:ss Z",   // RFC 822 with numbers
            "yyyy-MM-dd'T'HH:mm:ss'Z'",      // ISO 8601 UTC
            "yyyy-MM-dd'T'HH:mm:ssZ",        // ISO 8601 Offset
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",  // ISO 8601 UTC with millis
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ"     // ISO 8601 Offset with millis
        )

        var parsedDate: Date? = null
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.ENGLISH)
                if (format.endsWith("'Z'")) {
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                }
                parsedDate = sdf.parse(dateString.trim())
                if (parsedDate != null) break
            } catch (e: Exception) {
                // Ignore and try next format
            }
        }

        if (parsedDate == null) return dateString // Fallback

        // Format to user's local timezone
        val localSdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
        localSdf.timeZone = TimeZone.getDefault()
        return localSdf.format(parsedDate)
    }
}
