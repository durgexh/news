# NewsApp

An intelligent, modern Android News Application built with Kotlin and Jetpack Compose. This is an AI-assisted application, carefully guided and directed by the user throughout its development.

## 📱 Features

- **Modern UI/UX**: Fully built with Jetpack Compose, featuring Material Design 3, smooth animations, teal accent theme, and a responsive layout.
- **Categorized News**: Browse news across various categories such as Top Stories, Business, Technology, Science, Health, Sports, and more.
- **Localized News & Location Services**: Get news tailored to your specific region or city using Google Play Services Location.
- **Smart AI Chat**: A built-in AI chatbot powered by an on-device **SmartResponseEngine** with RAG (Retrieval-Augmented Generation). The AI intelligently answers questions about your loaded news articles — no model downloads, no API keys, no network calls needed.
- **Offline Support**: Caches news articles using Room Database so you can read them even without an internet connection.
- **Dark/Light Theme**: Built-in toggle to switch between a premium dark (GitHub-dark inspired) and clean light mode with teal accent colors.
- **OTA Updates**: Includes an Over-The-Air (OTA) update manager to notify users of new versions and download them directly within the app.
- **Bottom Navigation**: Clean Feed/Chat tab switching with a polished bottom navigation bar.

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Dagger Hilt
- **Networking**: Retrofit2 & OkHttp
- **Local Database**: Room
- **Image Loading**: Coil
- **Asynchronous Programming**: Kotlin Coroutines & Flow
- **On-Device AI**: SmartResponseEngine (lightweight, template-based with RAG — zero model downloads)
- **Background Processing**: WorkManager
- **Location**: Google Play Services Location

## 🤖 AI Chat System

The AI chat system uses a **SmartResponseEngine** that:
- Classifies user intent (greeting, summary, topic question, comparison, source inquiry, etc.)
- Uses **cosine similarity RAG search** with bigram-enhanced embeddings to find relevant articles
- Generates natural, contextual responses using multiple response templates
- Suggests follow-up questions to keep conversation flowing
- Tracks recent conversation topics for better context
- Runs **100% on-device** with zero external dependencies — no model files, no API keys, no network calls

## 🚀 Getting Started

1. Clone the repository.
2. Open the project in Android Studio.
3. Build and run the app on an emulator or physical device.
4. The AI chat works immediately — no model downloads needed!
