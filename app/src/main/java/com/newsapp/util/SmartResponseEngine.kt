package com.newsapp.util

/**
 * A lightweight, on-device response engine that generates natural-feeling
 * answers by combining RAG-retrieved article context with intelligent
 * template-based response generation.
 *
 * No model downloads. No API keys. No network calls.
 * Works entirely from cached news article data.
 */
class SmartResponseEngine {

    enum class QueryIntent {
        GREETING,
        SUMMARY_REQUEST,
        TOPIC_QUESTION,
        COMPARISON,
        OPINION_REQUEST,
        SOURCE_QUESTION,
        LATEST_NEWS,
        FAREWELL,
        HELP,
        UNKNOWN
    }

    data class ArticleContext(
        val title: String,
        val source: String,
        val snippet: String,
        val pubDate: String = ""
    )

    /**
     * Generates a contextual response based on the user's query and retrieved articles.
     */
    fun generateResponse(query: String, articles: List<ArticleContext>): ResponseResult {
        val intent = classifyIntent(query)
        val topicKeywords = extractKeywords(query)

        return when (intent) {
            QueryIntent.GREETING -> handleGreeting()
            QueryIntent.FAREWELL -> handleFarewell()
            QueryIntent.HELP -> handleHelp()
            QueryIntent.SUMMARY_REQUEST -> handleSummary(articles, topicKeywords)
            QueryIntent.TOPIC_QUESTION -> handleTopicQuestion(query, articles, topicKeywords)
            QueryIntent.COMPARISON -> handleComparison(articles, topicKeywords)
            QueryIntent.OPINION_REQUEST -> handleOpinionRequest(articles, topicKeywords)
            QueryIntent.SOURCE_QUESTION -> handleSourceQuestion(articles)
            QueryIntent.LATEST_NEWS -> handleLatestNews(articles)
            QueryIntent.UNKNOWN -> handleGenericQuery(query, articles, topicKeywords)
        }
    }

    data class ResponseResult(
        val text: String,
        val suggestedQuestions: List<String> = emptyList()
    )

    // --- Intent Classification ---

    fun classifyIntent(query: String): QueryIntent {
        val q = query.lowercase().trim()

        return when {
            // Greetings
            q.matches(Regex("^(hi|hello|hey|howdy|sup|yo|greetings|good\\s*(morning|afternoon|evening)).*")) -> QueryIntent.GREETING

            // Farewells
            q.matches(Regex("^(bye|goodbye|see\\s*you|thanks|thank\\s*you|that'?s\\s*all|done).*")) -> QueryIntent.FAREWELL

            // Help
            q.matches(Regex(".*(help|what can you do|how do you work|capabilities|features).*")) -> QueryIntent.HELP

            // Summary requests
            q.matches(Regex(".*(summarize|summary|brief|overview|recap|tldr|tl;?dr|catch me up|what'?s happening|what'?s going on|round\\s*up).*")) -> QueryIntent.SUMMARY_REQUEST

            // Latest/trending
            q.matches(Regex(".*(latest|trending|recent|new|breaking|top stories|headlines|today|current).*")) -> QueryIntent.LATEST_NEWS

            // Comparisons
            q.matches(Regex(".*(compare|versus|vs|difference|between|or|better).*")) -> QueryIntent.COMPARISON

            // Opinion/analysis
            q.matches(Regex(".*(think|opinion|analysis|impact|effect|consequence|mean|implication|significant).*")) -> QueryIntent.OPINION_REQUEST

            // Source questions
            q.matches(Regex(".*(source|who reported|which outlet|coverage|media|journalist|reporter).*")) -> QueryIntent.SOURCE_QUESTION

            // Topic questions (what, why, when, how, who, where, is, are, do, did, will, can)
            q.matches(Regex("^(what|why|when|how|who|where|is|are|do|did|will|can|could|should|tell me|explain).*")) -> QueryIntent.TOPIC_QUESTION

            else -> QueryIntent.UNKNOWN
        }
    }

    // --- Keyword Extraction ---

    private val stopWords = setOf(
        "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did", "will", "would", "could",
        "should", "may", "might", "shall", "can", "need", "dare", "ought",
        "used", "to", "of", "in", "for", "on", "with", "at", "by", "from",
        "as", "into", "through", "during", "before", "after", "above",
        "below", "between", "out", "off", "over", "under", "again",
        "further", "then", "once", "here", "there", "when", "where", "why",
        "how", "all", "both", "each", "few", "more", "most", "other",
        "some", "such", "no", "nor", "not", "only", "own", "same", "so",
        "than", "too", "very", "just", "because", "but", "and", "or", "if",
        "while", "about", "up", "it", "its", "this", "that", "these",
        "those", "i", "me", "my", "we", "our", "you", "your", "he", "him",
        "she", "her", "they", "them", "what", "which", "who", "whom",
        "tell", "explain", "know", "think", "going", "latest", "news",
        "today", "happening", "please", "thanks", "thank"
    )

    fun extractKeywords(query: String): List<String> {
        return query.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .split("\\s+".toRegex())
            .filter { it.length > 2 && it !in stopWords }
            .distinct()
    }

    // --- Response Handlers ---

    private fun handleGreeting(): ResponseResult {
        val greetings = listOf(
            "Hey! 👋 I'm your news assistant. I can help you understand the latest stories loaded on your device. What would you like to know about?",
            "Hello there! I've got access to all the news articles on your feed. Ask me anything — summaries, specific topics, or what's trending!",
            "Hi! Ready to dive into the news. I can summarize stories, answer questions about specific topics, or highlight what's important. What interests you?",
            "Hey! I'm here to help you navigate today's news. Try asking me for a summary, or ask about any topic you're curious about!"
        )
        return ResponseResult(
            text = greetings.random(),
            suggestedQuestions = listOf(
                "What's happening today?",
                "Summarize the top stories",
                "Any tech news?"
            )
        )
    }

    private fun handleFarewell(): ResponseResult {
        val farewells = listOf(
            "Happy reading! Come back anytime you want to chat about the news. 📰",
            "See you! I'll be here whenever you need a news update. ✌️",
            "Take care! Don't forget to check back for the latest stories."
        )
        return ResponseResult(text = farewells.random())
    }

    private fun handleHelp(): ResponseResult {
        return ResponseResult(
            text = "Here's what I can do:\n\n" +
                    "📋 **Summarize** — Ask for an overview of current stories\n" +
                    "🔍 **Answer questions** — Ask about any topic in the news\n" +
                    "📊 **Compare** — Ask me to compare coverage across sources\n" +
                    "📰 **Latest news** — Ask what's trending or breaking\n" +
                    "ℹ️ **Sources** — Ask who's reporting on a story\n\n" +
                    "I work entirely from your loaded news articles — no internet needed for my responses!",
            suggestedQuestions = listOf(
                "Summarize today's news",
                "What's trending?",
                "Any breaking news?"
            )
        )
    }

    private fun handleSummary(articles: List<ArticleContext>, keywords: List<String>): ResponseResult {
        if (articles.isEmpty()) return noContextResponse()

        val filtered = if (keywords.isNotEmpty()) {
            articles.filter { a ->
                keywords.any { kw -> a.title.contains(kw, true) || a.snippet.contains(kw, true) }
            }.ifEmpty { articles }
        } else articles

        val topArticles = filtered.take(5)
        val intros = listOf(
            "Here's a roundup of what's making headlines:",
            "Here's what's going on based on your current feed:",
            "Let me catch you up on the key stories:"
        )

        val summary = buildString {
            appendLine(intros.random())
            appendLine()
            topArticles.forEachIndexed { index, article ->
                appendLine("${index + 1}. **${article.title}**")
                if (article.source.isNotBlank()) {
                    appendLine("   _Reported by ${article.source}_")
                }
                appendLine()
            }
            if (filtered.size > 5) {
                appendLine("_...and ${filtered.size - 5} more stories in your feed._")
            }
        }

        return ResponseResult(
            text = summary.trim(),
            suggestedQuestions = topArticles.take(2).map { "Tell me more about: ${truncate(it.title, 40)}" }
        )
    }

    private fun handleTopicQuestion(query: String, articles: List<ArticleContext>, keywords: List<String>): ResponseResult {
        if (articles.isEmpty()) return noContextResponse(keywords)

        val relevant = findRelevantArticles(articles, keywords)
        if (relevant.isEmpty()) return noContextResponse(keywords)

        val primary = relevant.first()
        val others = relevant.drop(1).take(2)

        val openers = listOf(
            "Based on what I found in your news feed:",
            "Here's what the current coverage says:",
            "From the articles loaded on your device:"
        )

        val response = buildString {
            appendLine(openers.random())
            appendLine()
            appendLine("**${primary.title}**")
            if (primary.snippet.isNotBlank()) {
                appendLine(primary.snippet)
            }
            if (primary.source.isNotBlank()) {
                appendLine("_— ${primary.source}_")
            }

            if (others.isNotEmpty()) {
                appendLine()
                appendLine("Related coverage:")
                others.forEach { article ->
                    appendLine("• **${article.title}** _(${article.source})_")
                }
            }
        }

        val suggestions = mutableListOf<String>()
        if (others.isNotEmpty()) suggestions.add("Compare the sources on this")
        suggestions.add("Summarize all ${keywords.firstOrNull() ?: "related"} news")
        if (relevant.size > 1) suggestions.add("Who else is reporting on this?")

        return ResponseResult(text = response.trim(), suggestedQuestions = suggestions.take(3))
    }

    private fun handleComparison(articles: List<ArticleContext>, keywords: List<String>): ResponseResult {
        if (articles.isEmpty()) return noContextResponse(keywords)

        val relevant = findRelevantArticles(articles, keywords)
        if (relevant.size < 2) {
            return ResponseResult(
                text = "I only found one source covering this topic, so I can't do a full comparison. Here's what **${relevant.firstOrNull()?.source ?: "the source"}** reports:\n\n**${relevant.firstOrNull()?.title ?: "No title"}**",
                suggestedQuestions = listOf("Tell me more about this", "What else is in the news?")
            )
        }

        val sources = relevant.map { it.source }.distinct()
        val response = buildString {
            appendLine("Here's how different outlets are covering this topic:")
            appendLine()
            relevant.take(4).forEach { article ->
                appendLine("📰 **${article.source}**: ${article.title}")
            }
            appendLine()
            appendLine("_${sources.size} sources are covering this story across your feed._")
        }

        return ResponseResult(
            text = response.trim(),
            suggestedQuestions = listOf(
                "Which source has the most coverage?",
                "Summarize all related stories",
                "Any different perspectives on this?"
            )
        )
    }

    private fun handleOpinionRequest(articles: List<ArticleContext>, keywords: List<String>): ResponseResult {
        if (articles.isEmpty()) return noContextResponse(keywords)

        val relevant = findRelevantArticles(articles, keywords)

        val response = buildString {
            appendLine("I can share what the coverage suggests, though I don't form personal opinions:")
            appendLine()
            if (relevant.isNotEmpty()) {
                appendLine("The current reporting indicates this is a significant topic — **${relevant.size}** article(s) in your feed touch on it:")
                appendLine()
                relevant.take(3).forEach { article ->
                    appendLine("• **${article.title}** _(${article.source})_")
                }
                appendLine()
                appendLine("_Reading multiple sources can help you form a well-rounded view._")
            } else {
                appendLine("I couldn't find strong coverage on this specific angle in your current feed. Try refreshing your news or checking a different category!")
            }
        }

        return ResponseResult(
            text = response.trim(),
            suggestedQuestions = listOf("Summarize these stories", "Any other perspectives?", "What's trending instead?")
        )
    }

    private fun handleSourceQuestion(articles: List<ArticleContext>): ResponseResult {
        if (articles.isEmpty()) return noContextResponse()

        val sourceCounts = articles.groupBy { it.source }
            .mapValues { it.value.size }
            .entries
            .sortedByDescending { it.value }

        val response = buildString {
            appendLine("Here are the sources in your current feed:")
            appendLine()
            sourceCounts.take(8).forEach { (source, count) ->
                appendLine("• **$source** — $count article${if (count > 1) "s" else ""}")
            }
            if (sourceCounts.size > 8) {
                appendLine("• _...and ${sourceCounts.size - 8} more sources_")
            }
        }

        return ResponseResult(
            text = response.trim(),
            suggestedQuestions = sourceCounts.take(2).map { "What is ${it.key} reporting?" }
        )
    }

    private fun handleLatestNews(articles: List<ArticleContext>): ResponseResult {
        if (articles.isEmpty()) return noContextResponse()

        val topStories = articles.take(5)
        val intros = listOf(
            "Here's what's fresh on your feed right now:",
            "The latest headlines from your loaded articles:",
            "Here are the most recent stories:"
        )

        val response = buildString {
            appendLine(intros.random())
            appendLine()
            topStories.forEachIndexed { index, article ->
                val sourceTag = if (article.source.isNotBlank()) " _(${article.source})_" else ""
                appendLine("${index + 1}. **${article.title}**$sourceTag")
            }
            if (articles.size > 5) {
                appendLine()
                appendLine("_${articles.size - 5} more stories available — ask about any topic!_")
            }
        }

        return ResponseResult(
            text = response.trim(),
            suggestedQuestions = topStories.take(2).map { "Tell me about: ${truncate(it.title, 35)}" } +
                    listOf("Summarize everything")
        )
    }

    private fun handleGenericQuery(query: String, articles: List<ArticleContext>, keywords: List<String>): ResponseResult {
        // Fallback: treat as a topic question
        if (articles.isNotEmpty() && keywords.isNotEmpty()) {
            return handleTopicQuestion(query, articles, keywords)
        }

        if (articles.isNotEmpty()) {
            return handleLatestNews(articles)
        }

        return noContextResponse(keywords)
    }

    // --- Helpers ---

    private fun findRelevantArticles(articles: List<ArticleContext>, keywords: List<String>): List<ArticleContext> {
        if (keywords.isEmpty()) return articles.take(3)

        val scored = articles.map { article ->
            val titleLower = article.title.lowercase()
            val snippetLower = article.snippet.lowercase()
            val score = keywords.sumOf { kw ->
                var s = 0
                if (titleLower.contains(kw)) s += 3  // title match is stronger
                if (snippetLower.contains(kw)) s += 1
                s
            }
            article to score
        }

        val relevant = scored.filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }

        return relevant.ifEmpty { articles.take(3) }
    }

    private fun noContextResponse(keywords: List<String> = emptyList()): ResponseResult {
        val topic = if (keywords.isNotEmpty()) "\"${keywords.joinToString(" ")}\"" else "that topic"
        val responses = listOf(
            "I don't have any articles about $topic in your current feed. Try switching categories or pulling to refresh for new stories!",
            "Hmm, I couldn't find coverage on $topic in your loaded articles. The news feed might have fresh content if you refresh it.",
            "No results for $topic in your feed right now. Try asking about a different topic, or refresh your news for the latest articles."
        )
        return ResponseResult(
            text = responses.random(),
            suggestedQuestions = listOf("What's in my feed?", "Show me the latest", "Summarize today's news")
        )
    }

    private fun truncate(text: String, maxLen: Int): String {
        return if (text.length > maxLen) text.take(maxLen - 1) + "…" else text
    }
}
