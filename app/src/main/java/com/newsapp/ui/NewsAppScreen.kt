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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.newsapp.util.UpdateManager
import com.newsapp.viewmodel.NewsViewModel
import com.newsapp.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.newsapp.util.LocationHelper
import android.Manifest
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun NewsAppScreen(newsViewModel: NewsViewModel = hiltViewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val categories by newsViewModel.categories.collectAsState()
    val selectedCategory by newsViewModel.selectedCategory.collectAsState()
    val selectedCountry by newsViewModel.selectedCountry.collectAsState()
    val localCity by newsViewModel.localCity.collectAsState()
    val isDarkTheme by newsViewModel.isDarkTheme.collectAsState()
    val supportedCountries = newsViewModel.supportedCountries
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    
    var showManualCityDialog by remember { mutableStateOf(false) }
    var manualCityInput by remember { mutableStateOf("") }
    
    fun getIconForCategory(category: String): ImageVector {
        return when {
            category.contains("Top Stories", true) || category.contains("Trending", true) -> Icons.Default.LocalFireDepartment
            category.contains("Business", true) || category.contains("Finance", true) -> Icons.Default.Business
            category.contains("Technology", true) -> Icons.Default.Computer
            category.contains("Science", true) -> Icons.Default.Science
            category.contains("Health", true) -> Icons.Default.LocalHospital
            category.contains("Sports", true) || category.contains("eSports", true) -> Icons.Default.SportsBaseball
            category.contains("Education", true) -> Icons.Default.School
            category.contains("Weather", true) -> Icons.Default.Cloud
            category.contains("Automobile", true) -> Icons.Default.DirectionsCar
            else -> Icons.Default.Article
        }
    }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                val city = locationHelper.getCurrentCity()
                if (city != null) {
                    newsViewModel.setLocalCity(city)
                } else {
                    showManualCityDialog = true
                }
            }
        } else {
            showManualCityDialog = true
        }
    }
    
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
                                if (country == "Local \uD83D\uDCCD") {
                                    if (locationHelper.hasLocationPermission()) {
                                        scope.launch {
                                            val city = locationHelper.getCurrentCity()
                                            if (city != null) {
                                                newsViewModel.setLocalCity(city)
                                            } else {
                                                showManualCityDialog = true
                                            }
                                        }
                                    } else {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                                    }
                                }
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

    if (showManualCityDialog) {
        AlertDialog(
            onDismissRequest = { showManualCityDialog = false },
            title = { Text("Enter Your City") },
            text = {
                OutlinedTextField(
                    value = manualCityInput,
                    onValueChange = { manualCityInput = it },
                    label = { Text("City Name (e.g., Chicago)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (manualCityInput.isNotBlank()) {
                        newsViewModel.setLocalCity(manualCityInput.trim())
                    }
                    showManualCityDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualCityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val pagerState = rememberPagerState(pageCount = { 2 })

    HorizontalPager(state = pagerState) { page ->
        if (page == 0) {
            ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NewsApp",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { newsViewModel.toggleTheme() }) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                                contentDescription = "Toggle Theme"
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    
                    Surface(
                        onClick = { showCountryDialog = true },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Region",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                if (selectedCountry == "Local \uD83D\uDCCD" && localCity != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$localCity",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        IconButton(
                                            onClick = { showManualCityDialog = true },
                                            modifier = Modifier.size(20.dp).padding(start = 4.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit City", modifier = Modifier.size(14.dp))
                                        }
                                    }
                                } else {
                                    Text(
                                        text = selectedCountry,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            Icon(imageVector = Icons.Default.Public, contentDescription = "Select Region")
                        }
                    }
                }
                
                Divider(modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                
                Text(
                    text = "CATEGORIES",
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp
                )
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(categories) { category ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = getIconForCategory(category),
                                    contentDescription = null,
                                    tint = if (category == selectedCategory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = { 
                                Text(
                                    text = category,
                                    fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Medium
                                ) 
                            },
                            selected = category == selectedCategory,
                            onClick = {
                                newsViewModel.selectCategory(category)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                
                Text(
                    text = "Version $appVersion",
                    modifier = Modifier.padding(24.dp).align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.labelSmall,
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
            val uiState by newsViewModel.uiState.collectAsState()
            
            // Pull-to-refresh state from androidx.compose.material
            val isRefreshing = uiState is NewsUiState.Loading
            val pullRefreshState = rememberPullRefreshState(
                refreshing = isRefreshing,
                onRefresh = { newsViewModel.refreshNews() }
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .pullRefresh(pullRefreshState)
            ) {
                when (val state = uiState) {
                    is NewsUiState.Loading -> {
                        // Let PullRefreshIndicator handle loading state visually.
                        // We still need a scrollable area for pullRefresh to work properly
                        LazyColumn(modifier = Modifier.fillMaxSize()) { }
                    }
                    is NewsUiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error: ${state.message}", 
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { newsViewModel.refreshNews() }) {
                                Text("Retry")
                            }
                        }
                        LazyColumn(modifier = Modifier.fillMaxSize()) { }
                    }
                    is NewsUiState.Success -> {
                        if (state.news.isEmpty()) {
                            Text(
                                "No news available. Try pulling to refresh.",
                                modifier = Modifier.align(Alignment.Center)
                            )
                            LazyColumn(modifier = Modifier.fillMaxSize()) { }
                        } else {
                            NewsFeed(newsItems = state.news)
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
        } else if (page == 1) {
            val chatViewModel = hiltViewModel<ChatViewModel>()
            
            // Feed the RAG manager the current news state whenever it loads
            val uiState by newsViewModel.uiState.collectAsState()
            LaunchedEffect(uiState) {
                if (uiState is NewsUiState.Success) {
                    val articles = (uiState as NewsUiState.Success).news.map { 
                        Pair(it.title + " " + (it.description ?: ""), it.sourceName)
                    }
                    chatViewModel.updateNewsContext(articles)
                }
            }
            
            ChatScreen(chatViewModel = chatViewModel)
        }
    }
}
