# NewsApp

An intelligent, modern Android News Application built with Kotlin and Jetpack Compose. This is an AI-assisted application, carefully guided and directed by the user throughout its development.

## 📱 Features

- **Modern UI/UX**: Fully built with Jetpack Compose, featuring Material Design 3, smooth animations, and a responsive layout.
- **Categorized News**: Browse news across various categories such as Top Stories, Business, Technology, Science, Health, Sports, and more.
- **Localized News & Location Services**: Get news tailored to your specific region or city using Google Play Services Location.
- **On-Device AI Chat (RAG)**: A built-in AI chatbot powered by **Google MediaPipe Tasks GenAI**. The AI runs entirely on-device and uses Retrieval-Augmented Generation (RAG) to answer questions based on the latest news articles you are reading.
- **Background Model Downloading**: Seamlessly downloads the required AI models in the background using WorkManager.
- **Offline Support**: Caches news articles using Room Database so you can read them even without an internet connection.
- **Dark/Light Theme**: Built-in toggle to switch between dark and light modes.
- **OTA Updates**: Includes an Over-The-Air (OTA) update manager to notify users of new versions and download them directly within the app.

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Dagger Hilt
- **Networking**: Retrofit2 & OkHttp
- **Local Database**: Room
- **Image Loading**: Coil
- **Asynchronous Programming**: Kotlin Coroutines & Flow
- **On-Device AI**: Google MediaPipe GenAI & Text Tasks
- **Background Processing**: WorkManager
- **Location**: Google Play Services Location

## 🤖 AI-Assisted Development
This project showcases the power of AI-assisted development. It was built collaboratively, where the AI generated code, structured the architecture, and implemented complex features (like the On-Device LLM integration) under the strict guidance and vision of the developer.

## 🚀 Getting Started

1. Clone the repository.
2. Open the project in Android Studio.
3. Build and run the app on an emulator or physical device.
4. *Note: The on-device AI feature may require downloading a model on the first run.*
