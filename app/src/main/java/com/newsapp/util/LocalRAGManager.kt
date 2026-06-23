package com.newsapp.util

import android.content.Context
import android.util.Log
import kotlin.math.sqrt

/**
 * Handles localized Retrieval-Augmented Generation (RAG) by generating
 * embeddings and performing cosine similarity searches.
 * 
 * Enhanced with bigram features and richer document metadata for better
 * search relevance and response generation.
 */
class LocalRAGManager(private val context: Context) {

    // Represents a chunk of text and its semantic vector
    data class DocumentChunk(
        val text: String,
        val title: String,
        val source: String,
        val pubDate: String,
        val embedding: FloatArray
    )

    private val vectorStore = mutableListOf<DocumentChunk>()

    /**
     * Returns all ingested articles as SmartResponseEngine.ArticleContext objects.
     * Useful for feeding the full context to the response engine.
     */
    fun getAllArticles(): List<SmartResponseEngine.ArticleContext> {
        return vectorStore.map { chunk ->
            SmartResponseEngine.ArticleContext(
                title = chunk.title,
                source = chunk.source,
                snippet = chunk.text.take(200),
                pubDate = chunk.pubDate
            )
        }
    }

    /**
     * Ingests a list of articles, embedding their content into the vector store.
     * Now accepts richer article data: (title, description/snippet, sourceName, pubDate)
     */
    fun ingestArticles(articles: List<ArticleData>) {
        vectorStore.clear()
        for (article in articles) {
            val fullText = "${article.title} ${article.description}"
            val embedding = generateEmbedding(fullText)
            vectorStore.add(
                DocumentChunk(
                    text = article.description.ifBlank { article.title },
                    title = article.title,
                    source = article.source,
                    pubDate = article.pubDate,
                    embedding = embedding
                )
            )
        }
        Log.d("LocalRAGManager", "Ingested ${vectorStore.size} articles into local vector store.")
    }

    data class ArticleData(
        val title: String,
        val description: String,
        val source: String,
        val pubDate: String
    )

    /**
     * Searches for the top K most relevant articles to the query.
     * Returns results as SmartResponseEngine.ArticleContext for direct use.
     */
    fun search(query: String, topK: Int = 3): List<SmartResponseEngine.ArticleContext> {
        if (vectorStore.isEmpty()) return emptyList()

        val queryEmbedding = generateEmbedding(query)
        
        // Calculate cosine similarity for all chunks
        val scoredChunks = vectorStore.map { chunk ->
            chunk to cosineSimilarity(queryEmbedding, chunk.embedding)
        }

        // Sort descending by score and take top K
        return scoredChunks
            .sortedByDescending { it.second }
            .take(topK)
            .map { (chunk, _) ->
                SmartResponseEngine.ArticleContext(
                    title = chunk.title,
                    source = chunk.source,
                    snippet = chunk.text.take(200),
                    pubDate = chunk.pubDate
                )
            }
    }

    /**
     * Enhanced embedding using character unigrams + bigram frequency features
     * for better similarity matching than simple character frequency.
     */
    private fun generateEmbedding(text: String): FloatArray {
        // 256 for character unigrams + 256 for bigram hash features = 512 dimensions
        val vectorSize = 512
        val vector = FloatArray(vectorSize)
        val normalized = text.lowercase()
        
        // Unigram features (character frequency) — first 256 dimensions
        for (i in normalized.indices) {
            val charCode = normalized[i].code
            if (charCode in 0..255) {
                vector[charCode] += 1f
            }
        }
        
        // Bigram features (character pair frequency) — next 256 dimensions via hash
        for (i in 0 until normalized.length - 1) {
            val bigramHash = ((normalized[i].code * 31) + normalized[i + 1].code) % 256
            vector[256 + bigramHash] += 1f
        }

        // Word-level boosting: boost dimensions for important words
        val words = normalized.split(Regex("\\s+"))
        for (word in words) {
            if (word.length > 3) {
                // Hash the word to a dimension and boost it
                val wordHash = word.hashCode().and(0xFF)
                vector[wordHash] += 2f // extra weight for word-level signal
            }
        }
        
        // L2 normalize the vector
        var sum = 0f
        for (v in vector) sum += v * v
        val magnitude = sqrt(sum)
        if (magnitude > 0) {
            for (i in vector.indices) vector[i] /= magnitude
        }
        return vector
    }

    private fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        val minLen = minOf(v1.size, v2.size)
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        for (i in 0 until minLen) {
            dotProduct += v1[i] * v2[i]
            normA += v1[i] * v1[i]
            normB += v2[i] * v2[i]
        }
        if (normA == 0f || normB == 0f) return 0f
        return dotProduct / (sqrt(normA) * sqrt(normB))
    }
}
