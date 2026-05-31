package com.newsapp.ui

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.newsapp.util.UpdateManager
import com.newsapp.viewmodel.NewsViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NewsAppScreen(newsViewModel: NewsViewModel = viewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val categories by newsViewModel.categories.collectAsState()
    val selectedCategory by newsViewModel.selectedCategory.collectAsState()
    val selectedCountry by newsViewModel.selectedCountry.collectAsState()
    val supportedCountries = newsViewModel.supportedCountries
    val context = LocalContext.current
    
    // Dynamically fetch the version name from PackageManager
    val appVersion = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "Unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

    // OTA Updater State
    var updateInfo by remember { mutableStateOf<UpdateManager.UpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val info = UpdateManager.checkForUpdate(context)
        if (info?.isUpdateAvailable == true) {
            updateInfo = info
            showUpdateDialog = true
        }
    }
    
    if (showUpdateDialog && updateInfo != null) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = {
                Text(text = "Update Available", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Version ${updateInfo!!.latestVersionCode} is now available. Would you like to download and install this update?")
            },
            confirmButton = {
                Button(onClick = {
                    UpdateManager.downloadAndInstallUpdate(context, updateInfo!!.apkUrl)
                    showUpdateDialog = false
                }) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    var showCountryDialog by remember { mutableStateOf(false) }

    if (showCountryDialog) {
        AlertDialog(
            onDismissRequest = { showCountryDialog = false },
            title = {
                Text(text = "Select Region", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    supportedCountries.forEach { country ->
                        TextButton(
                            onClick = {
                                newsViewModel.selectCountry(country)
                                showCountryDialog = false
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = country,
                                fontSize = 18.sp,
                                modifier = Modifier.fillMaxWidth(),
                                fontWeight = if (country == selectedCountry) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCountryDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "News Categories",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = selectedCountry,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(onClick = { showCountryDialog = true }) {
                        Icon(imageVector = Icons.Default.Public, contentDescription = "Select Region")
                    }
                }
                Divider()
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(categories) { category ->
                        NavigationDrawerItem(
                            label = { Text(text = category) },
                            selected = category == selectedCategory,
                            onClick = {
                                newsViewModel.selectCategory(category)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
                Divider()
                Text(
                    text = "Version $appVersion",
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(selectedCategory, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            val newsItems by newsViewModel.newsItems.collectAsState()
            val isLoading by newsViewModel.isLoading.collectAsState()
            
            // Pull-to-refresh state from androidx.compose.material
            val pullRefreshState = rememberPullRefreshState(
                refreshing = isLoading,
                onRefresh = { newsViewModel.refreshNews() }
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .pullRefresh(pullRefreshState)
            ) {
                if (newsItems.isEmpty() && !isLoading) {
                    Text(
                        "No news available. Try pulling to refresh.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                    // We still need a scrollable area for pullRefresh to work properly
                    LazyColumn(modifier = Modifier.fillMaxSize()) { }
                } else {
                    NewsFeed(newsItems = newsItems)
                }

                PullRefreshIndicator(
                    refreshing = isLoading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
