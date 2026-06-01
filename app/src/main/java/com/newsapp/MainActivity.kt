package com.newsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.newsapp.ui.NewsAppScreen
import com.newsapp.ui.theme.NewsAppTheme
import androidx.activity.viewModels
import com.newsapp.viewmodel.NewsViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val newsViewModel: NewsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme by newsViewModel.isDarkTheme.collectAsState()
            NewsAppTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NewsAppScreen(newsViewModel)
                }
            }
        }
    }
}
