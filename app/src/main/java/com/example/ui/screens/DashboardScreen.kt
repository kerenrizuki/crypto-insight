package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CryptoToken
import com.example.ui.FormatUtils
import com.example.ui.components.SparklineChart
import com.example.ui.theme.*
import com.example.viewmodel.CryptoViewModel
import java.util.Date

@Composable
fun DashboardScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val navTab by viewModel.currentNavigationTab.collectAsStateWithLifecycle()
    val tokens by viewModel.displayedTokens.collectAsStateWithLifecycle()
    val watchlistIds by viewModel.watchlistIds.collectAsStateWithLifecycle()
    val activityLogs by viewModel.activityLogs.collectAsStateWithLifecycle()
    val isFirebaseEnabled by viewModel.firebaseSyncManager.isFirebaseEnabled.collectAsStateWithLifecycle()
    val syncStatus by viewModel.firebaseSyncManager.syncStatus.collectAsStateWithLifecycle()

    var selectedDetailToken by remember { mutableStateOf<CryptoToken?>(null) }
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(
                currentTab = navTab,
                onTabSelected = {
                    viewModel.currentNavigationTab.value = it
                    focusManager.clearFocus()
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (navTab) {
                "Home" -> {
                    HomeScreenContent(
                        viewModel = viewModel,
                        tokens = tokens,
                        watchlistIds = watchlistIds,
                        onTokenClick = { selectedDetailToken = it }
                    )
                }
                "Watchlist" -> {
                    WatchlistScreenContent(
                        viewModel = viewModel,
                        tokens = tokens,
                        watchlistIds = watchlistIds,
                        onTokenClick = { selectedDetailToken = it }
                    )
                }
                "Activity" -> {
                    ActivityLogScreenContent(
                        activityLogs = activityLogs,
                        viewModel = viewModel
                    )
                }
                "Settings" -> {
                    SettingsScreenContent(
                        viewModel = viewModel,
                        isFirebaseEnabled = isFirebaseEnabled,
                        syncStatus = syncStatus
                    )
                }
            }

            // Interactive token detail sheet popup dialog
            selectedDetailToken?.let { token ->
                TokenDetailDialog(
                    token = token,
                    isStarred = watchlistIds.contains(token.id),
                    onToggleStar = { viewModel.toggleWatchlist(token.id) },
                    onDismiss = { selectedDetailToken = null }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    // Beautiful Geometric Balance soft bottom navigation bar
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(
                width = 1.dp,
                color = GeoBorder.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ),
        color = Color.White,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavButtonItem(
                label = "Dashboard",
                icon = Icons.Default.Home,
                selected = currentTab == "Home",
                onClick = { onTabSelected("Home") },
                testTag = "nav_home_tab"
            )
            BottomNavButtonItem(
                label = "Watchlist",
                icon = Icons.Default.Star,
                selected = currentTab == "Watchlist",
                onClick = { onTabSelected("Watchlist") },
                testTag = "nav_watchlist_tab"
            )
            BottomNavButtonItem(
                label = "Activity",
                icon = Icons.Default.Refresh,
                selected = currentTab == "Activity",
                onClick = { onTabSelected("Activity") },
                testTag = "nav_activity_tab"
            )
            BottomNavButtonItem(
                label = "Settings",
                icon = Icons.Default.Settings,
                selected = currentTab == "Settings",
                onClick = { onTabSelected("Settings") },
                testTag = "nav_settings_tab"
            )
        }
    }
}

@Composable
fun BottomNavButtonItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Indigo600 else Slate500,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Indigo600 else Slate500
        )
    }
}

@Composable
fun HomeScreenContent(
    viewModel: CryptoViewModel,
    tokens: List<CryptoToken>,
    watchlistIds: List<String>,
    onTokenClick: (CryptoToken) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedSector by viewModel.selectedSector.collectAsStateWithLifecycle()
    val minMarketCap by viewModel.minMarketCap.collectAsStateWithLifecycle()
    val maxPsRatio by viewModel.maxPsRatio.collectAsStateWithLifecycle()
    val columnVisibility by viewModel.columnVisibility.collectAsStateWithLifecycle()
    val sortByColumn by viewModel.sortByColumn.collectAsStateWithLifecycle()
    val sortAscending by viewModel.sortAscending.collectAsStateWithLifecycle()

    var showFiltersSetup by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Core Profile Greeting - Geometric Balance layout pattern
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Morning,",
                        fontSize = 14.sp,
                        color = Slate500,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Dian Pratama",
                        fontSize = 24.sp,
                        color = Slate900,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Custom Rounded avatar profile
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                        .background(Indigo100, RoundedCornerShape(14.dp))
                        .border(2.dp, Color.White, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DP",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Indigo600
                    )
                }
            }
        }

        // 2. Featured Portfolio Card (Extracted Indigo Panel with dynamic statistics slider)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .border(1.dp, Indigo100.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Indigo600)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CIS Active Screener Profile",
                            color = Color.White.copy(alpha = 0.82f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "Live Engine",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Total DeFi TVL Tracked",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )

                    Text(
                        text = "$111.4 Billion USD",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated progressive indexing balance tracker
                    Text(
                        text = "On-Chain Database Synchronization Integrity",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { 1.0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = Emerald500,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // 3. Search and Quick Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val keyboardController = LocalSoftwareKeyboardController.current
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_input")
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    placeholder = { Text("Search screening tokens...", color = Slate400, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate500) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Indigo600,
                        unfocusedBorderColor = GeoBorder.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
                )

                // Expanded Toggle Filters Button
                IconButton(
                    onClick = { showFiltersSetup = !showFiltersSetup },
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(1.dp, GeoBorder.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        imageVector = if (showFiltersSetup) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand controls",
                        tint = Slate900
                    )
                }
            }
        }

        // 4. Dropdown Filters panel (P/S ratio slider and Min Market cap sliding constraints)
        item {
            AnimatedVisibility(
                visible = showFiltersSetup,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                        .border(1.dp, GeoBorder.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "Fundamental Screening Constraints",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Max P/S Ratio
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Max P/S Valuation Ratio:", fontSize = 12.sp, color = Slate500)
                            Text(
                                text = if (maxPsRatio >= 500f) "Any Value (Unlimited)" else "${maxPsRatio.toInt()}x",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Indigo600
                            )
                        }
                        Slider(
                            value = maxPsRatio,
                            onValueChange = { viewModel.maxPsRatio.value = it },
                            valueRange = 0f..500f,
                            colors = SliderDefaults.colors(
                                thumbColor = Indigo600,
                                activeTrackColor = Indigo600,
                                inactiveTrackColor = Indigo100
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Min Market Cap
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Minimum Market Cap (USD Millions):", fontSize = 12.sp, color = Slate500)
                            Text(
                                text = if (minMarketCap == 0f) "Any Cap (Micro/Small)" else "$${minMarketCap.toInt()}M+",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Indigo600
                            )
                        }
                        Slider(
                            value = minMarketCap,
                            onValueChange = { viewModel.minMarketCap.value = it },
                            valueRange = 0f..1000f,
                            colors = SliderDefaults.colors(
                                thumbColor = Indigo600,
                                activeTrackColor = Indigo600,
                                inactiveTrackColor = Indigo100
                            )
                        )

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Slate100)

                        // Custom Columns Toggle Selection layout
                        Text(
                            text = "Toggle Screener Grid Columns",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Horizontally scrollable checklist of visibility columns
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            columnVisibility.keys.forEach { col ->
                                val isVisible = columnVisibility[col] ?: true
                                Box(
                                    modifier = Modifier
                                        .testTag("visible_column_${col}")
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isVisible) Indigo50 else Slate100)
                                        .border(
                                            1.dp,
                                            if (isVisible) Indigo100 else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.toggleColumnVisibility(col) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (isVisible) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Indigo600,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(
                                            text = col,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isVisible) Indigo600 else Slate500
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Horizontal sectors categories scroll (All, L1/L2, DEX, Lending, Liquid Staking)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sectorsList = listOf("All", "L1/L2", "DEX", "Lending", "Liquid Staking")
                sectorsList.forEach { tab ->
                    val isSelected = selectedSector == tab
                    Box(
                        modifier = Modifier
                            .testTag("sector_chip_${tab}")
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Indigo600 else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Indigo600 else GeoBorder.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                viewModel.selectedSector.value = tab
                                viewModel.showWatchlistOnly.value = false
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = tab,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Slate500
                        )
                    }
                }
            }
        }

        // 6. Dynamic Main Screens Grid (Table headers for sorting on-fly)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .border(1.dp, GeoBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                color = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column {
                    // Header Row with Sort Indicators
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate100)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Token / Sector",
                            modifier = Modifier
                                .weight(2f)
                                .clickable {
                                    if (sortByColumn == "Market Cap") {
                                        viewModel.sortAscending.value = !sortAscending
                                    } else {
                                        viewModel.sortByColumn.value = "Market Cap"
                                        viewModel.sortAscending.value = false
                                    }
                                },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        if (sortByColumn == "Market Cap") {
                            Icon(
                                imageVector = if (sortAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Active sorting",
                                tint = Indigo600,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Text(
                            text = "Price",
                            modifier = Modifier
                                .weight(1.5f)
                                .clickable {
                                    if (sortByColumn == "Price") {
                                        viewModel.sortAscending.value = !sortAscending
                                    } else {
                                        viewModel.sortByColumn.value = "Price"
                                        viewModel.sortAscending.value = false
                                    }
                                },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            textAlign = TextAlign.End
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        if (sortByColumn == "Price") {
                            Icon(
                                imageVector = if (sortAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Active sorting",
                                tint = Indigo600,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Text(
                            text = "P/S Ratio",
                            modifier = Modifier
                                .weight(1.2f)
                                .clickable {
                                    if (sortByColumn == "P/S Ratio") {
                                        viewModel.sortAscending.value = !sortAscending
                                    } else {
                                        viewModel.sortByColumn.value = "P/S Ratio"
                                        viewModel.sortAscending.value = true // default ascending query for valuation!
                                    }
                                },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            textAlign = TextAlign.End
                        )
                        if (sortByColumn == "P/S Ratio") {
                            Icon(
                                imageVector = if (sortAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Active sorting",
                                tint = Indigo600,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    // Tokens Rows
                    if (tokens.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No assets match selection constraints.",
                                color = Slate500,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        tokens.forEach { token ->
                            TokenTableRow(
                                token = token,
                                isStarred = watchlistIds.contains(token.id),
                                columnVisibility = columnVisibility,
                                onStarToggle = { viewModel.toggleWatchlist(token.id) },
                                onClick = { onTokenClick(token) }
                            )
                            Divider(color = Slate100)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TokenTableRow(
    token: CryptoToken,
    isStarred: Boolean,
    columnVisibility: Map<String, Boolean>,
    onStarToggle: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .testTag("token_row_${token.id}")
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Star & Info
        IconButton(
            onClick = onStarToggle,
            modifier = Modifier
                .size(28.dp)
                .testTag("star_toggle_${token.id}")
        ) {
            Icon(
                imageVector = if (isStarred) Icons.Default.Star else Icons.Default.FavoriteBorder,
                contentDescription = "Star token",
                tint = if (isStarred) Orange500 else Slate400,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Name and details
        Column(
            modifier = Modifier.weight(2f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = token.symbol,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                // Small Sector Tag
                Box(
                    modifier = Modifier
                        .background(Indigo50, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = token.sector,
                        fontSize = 8.sp,
                        color = Indigo600,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = token.name,
                fontSize = 11.sp,
                color = Slate500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Price formatting
        Column(
            modifier = Modifier.weight(1.5f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = FormatUtils.formatPrice(token.price),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Text(
                text = FormatUtils.formatPercentage(token.revenue24hChange),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (token.revenue24hChange >= 0) Emerald500 else Color.Red
            )
        }

        // P/S Valuation ratios
        Column(
            modifier = Modifier.weight(1.2f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${token.psRatio.toInt()}x",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (token.psRatio < 15.0) Emerald500 else Slate900
            )
            Text(
                text = "P/S ratio",
                fontSize = 9.sp,
                color = Slate500
            )
        }
    }
}

@Composable
fun WatchlistScreenContent(
    viewModel: CryptoViewModel,
    tokens: List<CryptoToken>,
    watchlistIds: List<String>,
    onTokenClick: (CryptoToken) -> Unit
) {
    val watchlistTokens = tokens.filter { watchlistIds.contains(it.id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Personal",
                    fontSize = 14.sp,
                    color = Slate500,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Watchlist Screener",
                    fontSize = 24.sp,
                    color = Slate900,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Sync button trigger
            Button(
                onClick = { viewModel.triggerFirebaseSync() },
                modifier = Modifier
                    .testTag("firebase_sync_btn")
                    .height(40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Sync", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Merge Cloud", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (watchlistTokens.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Empty",
                        tint = Slate400,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Your watchlist is currently empty.",
                        fontSize = 14.sp,
                        color = Slate900,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Star tokens on Dashboard to bookmark them.",
                        fontSize = 11.sp,
                        color = Slate500,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(watchlistTokens) { token ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTokenClick(token) }
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .border(1.dp, GeoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.8f)) {
                                Text(
                                    token.symbol,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    token.name,
                                    fontSize = 11.sp,
                                    color = Slate500,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Tiny embed spark chart in watch lists
                            SparklineChart(
                                data = token.historyPrices,
                                lineColor = if (token.revenue24hChange >= 0) Emerald500 else Color.Red,
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(35.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(
                                modifier = Modifier.weight(1.2f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    FormatUtils.formatPrice(token.price),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    FormatUtils.formatPercentage(token.revenue24hChange),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (token.revenue24hChange >= 0) Emerald500 else Color.Red
                                )
                            }

                            IconButton(
                                onClick = { viewModel.toggleWatchlist(token.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Starred",
                                    tint = Emerald500,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityLogScreenContent(
    activityLogs: List<com.example.viewmodel.ActivityLog>,
    viewModel: CryptoViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "System Sync",
                    fontSize = 14.sp,
                    color = Slate500,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Recent Activity Log",
                    fontSize = 24.sp,
                    color = Slate900,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Quick clear ticks
            IconButton(
                onClick = { viewModel.simulateHourlyUpdate() },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("hourly_simulate_btn")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Simulate Tick", tint = Indigo600)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(activityLogs) { log ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(14.dp)),
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Soft container pill based on log type matching Geometric balance
                        val iconBg = when (log.type) {
                            "sync" -> Indigo50
                            "alert" -> Orange50
                            else -> Slate100
                        }
                        val tint = when (log.type) {
                            "sync" -> Indigo600
                            "alert" -> Orange500
                            else -> Slate500
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(iconBg, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (log.type == "alert") Icons.Default.Search else Icons.Default.Settings,
                                contentDescription = log.type,
                                tint = tint,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = log.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = log.timeAgo,
                                    fontSize = 10.sp,
                                    color = Slate400
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = log.desc,
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreenContent(
    viewModel: CryptoViewModel,
    isFirebaseEnabled: Boolean,
    syncStatus: String
) {
    var apiKey by remember { mutableStateOf("") }
    var appId by remember { mutableStateOf("") }
    var projectId by remember { mutableStateOf("") }

    val configState by viewModel.firebaseSyncManager.firebaseConfig.collectAsStateWithLifecycle()

    // Grab stored values on first display
    LaunchedEffect(configState) {
        configState?.let {
            apiKey = it.apiKey
            appId = it.appId
            projectId = it.projectId
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Preferences",
                fontSize = 14.sp,
                color = Slate500,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Settings & Synchronization",
                fontSize = 24.sp,
                color = Slate900,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SharedPreferences settings inputs
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .border(1.dp, GeoBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            color = Color.White,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Firebase Firestore Connector Config",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Firebase API Key", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo600, unfocusedBorderColor = GeoBorder)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = appId,
                    onValueChange = { appId = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Firebase Application ID", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo600, unfocusedBorderColor = GeoBorder)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = projectId,
                    onValueChange = { projectId = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Firebase Project ID", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo600, unfocusedBorderColor = GeoBorder)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val ok = viewModel.firebaseSyncManager.saveConfig(apiKey, appId, projectId)
                            if (ok) {
                                viewModel.addLog("Config saved", "New remote Firebase configuration set.", "sync")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Config", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.firebaseSyncManager.clearConfig()
                            apiKey = ""
                            appId = ""
                            projectId = ""
                            viewModel.addLog("Config cleared", "Restored local offline operations.", "sync")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate900)
                    ) {
                        Text("Clear Keys", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Connection status card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .border(1.dp, GeoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            color = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(if (isFirebaseEnabled) Emerald500 else Orange500, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Connection Integrity Status:", fontSize = 11.sp, color = Slate500)
                    Text(
                        text = syncStatus,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulating offline tick helper inside settings
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .border(1.dp, GeoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            color = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Screener Engine Controls",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Simulate dynamic on-chain network updates (prices, addresses, history blocks).",
                    fontSize = 11.sp,
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.simulateHourlyUpdate()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Tick metrics")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simulate Hourly Market Tick", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TokenDetailDialog(
    token: CryptoToken,
    isStarred: Boolean,
    onToggleStar: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Price") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .border(2.dp, GeoBorder, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header (Symbol, Name and Favorite Toggler)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = token.symbol,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900
                            )
                            Box(
                                modifier = Modifier
                                    .background(Indigo50, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(token.sector, fontSize = 9.sp, color = Indigo600, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = token.name,
                            fontSize = 14.sp,
                            color = Slate500
                        )
                    }

                    Row {
                        IconButton(onClick = onToggleStar) {
                            Icon(
                                imageVector = if (isStarred) Icons.Default.Star else Icons.Default.FavoriteBorder,
                                contentDescription = "Watchlist toggle",
                                tint = if (isStarred) Orange500 else Slate400,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Settings, contentDescription = "Close description dialogue indicator", tint = Slate500)
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Slate100)

                // Large Main Price Metric
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Current Fundamental Price:", fontSize = 11.sp, color = Slate500)
                        Text(
                            text = FormatUtils.formatPrice(token.price),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (token.revenue24hChange >= 0) Emerald50.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = FormatUtils.formatPercentage(token.revenue24hChange),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (token.revenue24hChange >= 0) Emerald500 else Color.Red
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chart Tab selectors (Price, Annualized revenue, TVL)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabOptions = listOf("Price", "Revenue", "TVL")
                    tabOptions.forEach { option ->
                        val isSel = selectedTab == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Indigo600 else Slate100)
                                .clickable { selectedTab = option }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Slate500
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Integrated chart area
                val targetChartData = when (selectedTab) {
                    "Price" -> token.historyPrices
                    "Revenue" -> token.historyRevenue
                    "TVL" -> token.historyTvl
                    else -> token.historyPrices
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Slate100, RoundedCornerShape(16.dp))
                        .padding(8.dp)
                ) {
                    SparklineChart(
                        data = targetChartData,
                        lineColor = if (token.revenue24hChange >= 0) Emerald500 else Color.Red,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fundamental details sheet
                Text("Token Description", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(4.dp))
                Text(token.description, fontSize = 11.sp, color = Slate500)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Valuation Metrics & On-Chain Financials", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(8.dp))

                DetailItemRow("Market Capitalization (MCAP)", FormatUtils.formatCompact(token.marketCap))
                DetailItemRow("Fully Diluted Valuation (FDV)", FormatUtils.formatCompact(token.fullyDilutedValuation))
                DetailItemRow("Total Value Locked (TVL)", FormatUtils.formatCompact(token.tvl))
                DetailItemRow("Annualized Revenue (Fees)", FormatUtils.formatCompact(token.annualizedRevenue))
                DetailItemRow("P/S Ratio (Market Cap / Rev)", "${token.psRatio}x")
                DetailItemRow("Active Wallets (Daily)", FormatUtils.formatLargeNumberRaw(token.activeAddresses.toDouble()))

                Spacer(modifier = Modifier.height(12.dp))

                Text("Tokenomics Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(4.dp))
                Text(token.tokenomicsDetails, fontSize = 11.sp, color = Slate500)

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = Slate500)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate900)
    }
}
