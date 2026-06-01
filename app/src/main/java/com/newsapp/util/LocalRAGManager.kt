package com.newsapp.util

import android.content.Context
import android.util.Log
import java.io.File
import kotlin.math.sqrt

/**
 * Handles localized Retrieval-Augmented Generation (RAG) by generating
 * embeddings and performing cosine similarity searches.
 */
class LocalRAGManager(private val context: Context) {

    // Represents a chunk of text and its semantic vector
    data class DocumentChunk(
        val text: String,
        val source: String,
        val embedding: FloatArray
    )

    private val vectorStore = mutableListOf<DocumentChunk>()

    /**
     * Ingests a list of articles, embedding their content into the vector store.
     */
    fun ingestArticles(articles: List<Pair<String, String>>) { // Pair of (Text, Source)
        vectorStore.clear()
        for (article in articles) {
            val embedding = generateEmbedding(article.first)
            vectorStore.add(DocumentChunk(article.first, article.second, embedding))
        }
        Log.d("LocalRAGManager", "Ingested ${vectorStore.size} articles into local vector store.")
    }

    /**
     * Searches for the top K most relevant articles to the query.
     */
    fun search(query: String, topK: Int = 3): List<DocumentChunk> {
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
            .map { it.first }
    }

    /**
     * Generates a semantic embedding vector for a given text.
     * In a production environment, this would initialize MediaPipe TextEmbedder:
     * 
     * val baseOptions = BaseOptions.builder().setModelAssetPath("embedder.tflite").build()
     * val options = TextEmbedderOptions.builder().setBaseOptions(baseOptions).build()
     * val textEmbedder = TextEmbedder.createFromOptions(context, options)
     * return textEmbedder.embed(text).embeddingResult().embeddings().first().floatEmbedding()
     */
    private fun generateEmbedding(text: String): FloatArray {
        val modelFile = File(context.filesDir, AIModelDownloaderWorker.EMBEDDER_FILE_NAME)
        
        // If we don't have a real model, fallback to a pseudo-embedding (TF-IDF mock)
        // based on character frequency to simulate semantic distance for testing.
        if (!modelFile.exists() || modelFile.length() == 0L) {
            return mockEmbedding(text)
        }
        
        // TODO: Plug in actual MediaPipe TextEmbedder when model file is valid.
        return mockEmbedding(text)
    }

    private fun mockEmbedding(text: String): FloatArray {
        val vector = FloatArray(256)
        val normalized = text.lowercase()
        for (i in normalized.indices) {
            val charCode = normalized[i].code
            if (charCode in 0..255) {
                vector[charCode] += 1f
            }
        }
        // Normalize the vector
        var sum = 0f
        for (v in vector) sum += v * v
        val magnitude = sqrt(sum)
        if (magnitude > 0) {
            for (i in vector.indices) vector[i] /= magnitude
        }
        return vector
    }

    private fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        for (i in v1.indices) {
            dotProduct += v1[i] * v2[i]
            normA += v1[i] * v1[i]
            normB += v2[i] * v2[i]
        }
        if (normA == 0f || normB == 0f) return 0f
        return dotProduct / (sqrt(normA) * sqrt(normB))
    }
}
