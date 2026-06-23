package com.newsapp.ui

import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.newsapp.util.UpdateManager
import com.newsapp.viewmodel.NewsViewModel
import com.newsapp.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.newsapp.util.LocationHelper
import com.newsapp.util.LocalRAGManager
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Feed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
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

    // Bottom nav state: 0 = Feed, 1 = AI Chat
    var selectedTab by remember { mutableIntStateOf(0) }
    
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

    // ===== Main Layout: Drawer wraps content directly (no HorizontalPager) =====
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = selectedTab == 0, // Only enable drawer swipe on Feed tab
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                // Header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                ) {
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
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { newsViewModel.toggleTheme() }) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                                    contentDescription = "Toggle Theme",
                                    tint = MaterialTheme.colorScheme.primary
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
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    if (selectedCountry == "Local \uD83D\uDCCD" && localCity != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "$localCity",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
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
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = "Select Region",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                Divider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                
                Text(
                    text = "CATEGORIES",
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
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
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                
                Text(
                    text = "Version $appVersion",
                    modifier = Modifier.padding(24.dp).align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    ) {
        // ===== Content with Bottom Navigation =====
        Scaffold(
            topBar = {
                if (selectedTab == 0) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                selectedCategory,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 4.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Feed,
                                contentDescription = "News Feed"
                            )
                        },
                        label = { Text("Feed") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Chat"
                            )
                        },
                        label = { Text("AI Chat") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        ) { innerPadding ->
            // Content switching based on selected tab
            when (selectedTab) {
                0 -> {
                    // News Feed Tab
                    val uiState by newsViewModel.uiState.collectAsState()
                    
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
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { newsViewModel.refreshNews() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text("Retry")
                                    }
                                }
                                LazyColumn(modifier = Modifier.fillMaxSize()) { }
                            }
                            is NewsUiState.Success -> {
                                if (state.news.isEmpty()) {
                                    if (selectedCountry == "Local \uD83D\uDCCD" && localCity.isNullOrBlank()) {
                                        Card(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .padding(24.dp)
                                                .fillMaxWidth(),
                                            shape = RoundedCornerShape(24.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            ),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(28.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(72.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Public,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(36.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                
                                                Spacer(modifier = Modifier.height(20.dp))
                                                
                                                Text(
                                                    text = "Local News Onboarding",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                
                                                Spacer(modifier = Modifier.height(8.dp))
                                                
                                                Text(
                                                    text = "Enter your city or enable location services to get personalized local news updates around you.",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                                
                                                Spacer(modifier = Modifier.height(24.dp))
                                                
                                                Button(
                                                    onClick = {
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
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Public,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Detect Location")
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.height(8.dp))
                                                
                                                OutlinedButton(
                                                    onClick = { showManualCityDialog = true },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Enter City Manually")
                                                    }
                                                }
                                            }
                                        }
                                        LazyColumn(modifier = Modifier.fillMaxSize()) { }
                                    } else {
                                        Text(
                                            "No news available. Try pulling to refresh.",
                                            modifier = Modifier.align(Alignment.Center),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        LazyColumn(modifier = Modifier.fillMaxSize()) { }
                                    }
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
                1 -> {
                    // AI Chat Tab
                    val chatViewModel = hiltViewModel<ChatViewModel>()
                    
                    // Feed the RAG manager the current news state whenever it loads
                    val uiState by newsViewModel.uiState.collectAsState()
                    LaunchedEffect(uiState) {
                        if (uiState is NewsUiState.Success) {
                            val articles = (uiState as NewsUiState.Success).news.map { item ->
                                val sourceName = item.sources.firstOrNull()?.name ?: "Unknown"
                                LocalRAGManager.ArticleData(
                                    title = item.title,
                                    description = item.title, // Use title as description since NewsItem doesn't have a description field
                                    source = sourceName,
                                    pubDate = item.pubDate
                                )
                            }
                            chatViewModel.updateNewsContext(articles)
                        }
                    }
                    
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ChatScreen(chatViewModel = chatViewModel)
                    }
                }
            }
        }
    }
}