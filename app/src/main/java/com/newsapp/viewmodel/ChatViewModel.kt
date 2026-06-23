package com.newsapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.newsapp.util.LocalRAGManager
import com.newsapp.util.SmartResponseEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val role: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedQuestions: List<String> = emptyList()
)

@HiltViewModel
class ChatViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val ragManager = LocalRAGManager(application)
    private val responseEngine = SmartResponseEngine()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _suggestedQuestions = MutableStateFlow<List<String>>(emptyList())
    val suggestedQuestions: StateFlow<List<String>> = _suggestedQuestions

    // Track recent topics for conversational context
    private val recentTopics = mutableListOf<String>()

    init {
        addMessage(
            "AI",
            "Hey! 👋 I'm your news assistant. Ask me anything about the articles on your feed — summaries, specific topics, or what's trending!",
            suggestedQuestions = listOf(
                "What's happening today?",
                "Summarize the top stories",
                "What can you do?"
            )
        )
    }

    /**
     * Ingests current news into the local RAG manager with richer data.
     */
    fun updateNewsContext(newsList: List<LocalRAGManager.ArticleData>) {
        ragManager.ingestArticles(newsList)
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        addMessage("User", userText)
        _isProcessing.value = true
        _suggestedQuestions.value = emptyList()

        // Track keywords for context
        val keywords = responseEngine.extractKeywords(userText)
        recentTopics.addAll(keywords.take(3))
        if (recentTopics.size > 10) {
            recentTopics.subList(0, recentTopics.size - 10).clear()
        }

        viewModelScope.launch {
            // Simulate natural "thinking" delay (300-1200ms)
            val thinkingDelay = (300L..1200L).random()
            delay(thinkingDelay)

            // 1. Retrieve context via RAG
            val topArticles = ragManager.search(userText, topK = 5)

            // 2. If RAG returned few results, also check with recent topics
            val additionalArticles = if (topArticles.size < 3 && recentTopics.isNotEmpty()) {
                val contextQuery = recentTopics.takeLast(3).joinToString(" ")
                ragManager.search(contextQuery, topK = 3)
            } else emptyList()

            val allArticles = (topArticles + additionalArticles).distinctBy { it.title }

            // 3. Generate response using the smart engine
            val result = responseEngine.generateResponse(userText, allArticles)

            addMessage("AI", result.text, suggestedQuestions = result.suggestedQuestions)
            _suggestedQuestions.value = result.suggestedQuestions
            _isProcessing.value = false
        }
    }

    private fun addMessage(role: String, text: String, suggestedQuestions: List<String> = emptyList()) {
        val current = _messages.value.toMutableList()
        current.add(ChatMessage(role, text, suggestedQuestions = suggestedQuestions))
        _messages.value = current
    }
}
