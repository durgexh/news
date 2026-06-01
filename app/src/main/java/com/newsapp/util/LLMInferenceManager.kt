package com.newsapp.util

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LLMInferenceManager(private val context: Context) {

    private var llmInference: LlmInference? = null
    
    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded

    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            try {
                val modelFile = File(context.filesDir, AIModelDownloaderWorker.LLM_FILE_NAME)
                if (modelFile.exists() && modelFile.length() > 0L) {
                    val options = LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(modelFile.absolutePath)
                        .setMaxTokens(512)
                        .build()
                    llmInference = LlmInference.createFromOptions(context, options)
                    _isModelLoaded.value = true
                    Log.d("LLMInferenceManager", "LLM successfully loaded from disk.")
                } else {
                    Log.w("LLMInferenceManager", "LLM file missing or empty. Using fallback mock mode.")
                    _isModelLoaded.value = false
                }
            } catch (e: Exception) {
                Log.e("LLMInferenceManager", "Error initializing LLM", e)
                _isModelLoaded.value = false
            }
        }
    }

    /**
     * Generates a response based on the prompt. If the model isn't loaded (e.g. placeholder),
     * it returns a mocked intelligent response using the RAG context.
     */
    fun generateResponse(prompt: String, contextData: String): String {
        val fullPrompt = """
            Context information is below:
            ---------------------
            $contextData
            ---------------------
            Given the context information and no prior knowledge, answer the query. Mention your sources.
            Query: $prompt
            Answer:
        """.trimIndent()

        if (_isModelLoaded.value && llmInference != null) {
            return try {
                llmInference?.generateResponse(fullPrompt) ?: "Error generating response."
            } catch (e: Exception) {
                Log.e("LLMInferenceManager", "Inference error", e)
                "I encountered an error while processing that."
            }
        } else {
            // Mock response if the massive model file isn't downloaded yet
            return "Based on the official media outlets provided in the context, here is what I found regarding your query:\n\n" +
                   contextData + "\n\n" +
                   "Note: This is a placeholder response because the 1GB local model is currently downloading in the background."
        }
    }
}
