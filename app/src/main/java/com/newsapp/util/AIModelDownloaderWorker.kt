package com.newsapp.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Retained for potential future use (e.g., downloading curated keyword dictionaries
 * or category-specific response templates). Currently a no-op since the 
 * SmartResponseEngine works without any downloaded assets.
 */
class AIModelDownloaderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "AIModelDownloader"

        fun isModelDownloaded(context: Context): Boolean {
            // No model files needed — SmartResponseEngine runs without any
            return true
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("AIModelDownloader", "No model downloads needed — SmartResponseEngine is self-contained.")
        Result.success()
    }
}
