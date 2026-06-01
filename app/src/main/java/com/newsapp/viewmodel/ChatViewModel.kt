package com.newsapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.newsapp.util.AIModelDownloaderWorker
import com.newsapp.util.LLMInferenceManager
import com.newsapp.util.LocalRAGManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val role: String, val text: String)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val llmManager = LLMInferenceManager(application)
    private val ragManager = LocalRAGManager(application)
    private val workManager = WorkManager.getInstance(application)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    init {
        // Automatically start the background download when the viewmodel is created
        startBackgroundModelDownload()
        
        viewModelScope.launch {
            llmManager.initialize()
            addMessage("AI", "Hello! I am your completely localized, secure AI assistant. Ask me anything about the news currently loaded on your device!")
        }
    }

    private fun startBackgroundModelDownload() {
        if (!AIModelDownloaderWorker.isModelDownloaded(getApplication())) {
            val request = OneTimeWorkRequestBuilder<AIModelDownloaderWorker>().build()
            workManager.enqueue(request)
        }
    }

    /**
     * Ingests current news into the local RAG manager.
     */
    fun updateNewsContext(newsList: List<Pair<String, String>>) {
        ragManager.ingestArticles(newsList)
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        addMessage("User", userText)
        _isProcessing.value = true

        viewModelScope.launch {
            // 1. Retrieve context via RAG
            val topArticles = ragManager.search(userText, topK = 3)
            val contextData = topArticles.joinToString("\n\n") { "Source: ${it.source}\nContent: ${it.text}" }

            // 2. Generate Response using on-device LLM
            val response = llmManager.generateResponse(userText, contextData)

            addMessage("AI", response)
            _isProcessing.value = false
        }
    }

    private fun addMessage(role: String, text: String) {
        val current = _messages.value.toMutableList()
        current.add(ChatMessage(role, text))
        _messages.value = current
    }
}
