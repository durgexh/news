package com.newsapp.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AIModelDownloaderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "AIModelDownloader"
        
        // Placeholder URLs for the quantized models
        private const val LLM_URL = "https://example.com/models/gemma_1b_int4.bin"
        private const val EMBEDDER_URL = "https://example.com/models/gecko_110m.tflite"

        const val LLM_FILE_NAME = "llm.bin"
        const val EMBEDDER_FILE_NAME = "embedder.tflite"

        fun isModelDownloaded(context: Context): Boolean {
            val llmFile = File(context.filesDir, LLM_FILE_NAME)
            val embedderFile = File(context.filesDir, EMBEDDER_FILE_NAME)
            // In a real scenario, also check file size or checksum
            return llmFile.exists() && embedderFile.exists()
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("AIModelDownloader", "Starting background model download...")

            val llmFile = File(applicationContext.filesDir, LLM_FILE_NAME)
            if (!llmFile.exists()) {
                downloadFile(LLM_URL, llmFile)
            }

            val embedderFile = File(applicationContext.filesDir, EMBEDDER_FILE_NAME)
            if (!embedderFile.exists()) {
                downloadFile(EMBEDDER_URL, embedderFile)
            }

            Log.d("AIModelDownloader", "Models downloaded successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e("AIModelDownloader", "Failed to download models", e)
            Result.retry()
        }
    }

    private fun downloadFile(urlString: String, destination: File) {
        // Skip actual download if placeholder URL is used, just create an empty file for testing
        if (urlString.contains("example.com")) {
            destination.createNewFile()
            Log.d("AIModelDownloader", "Mock downloaded: ${destination.name}")
            return
        }

        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        connection.connect()

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw Exception("Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
        }

        connection.inputStream.use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        }
    }
}
