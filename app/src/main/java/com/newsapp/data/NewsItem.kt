package com.newsapp.data

data class SourceInfo(
    val name: String,
    val logoUrl: String
)

data class NewsItem(
    val title: String,
    val link: String,
    val sources: List<SourceInfo>,
    val imageUrl: String?,
    val pubDate: String
)
