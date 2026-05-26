package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.AppDatabase
import com.example.database.FirebaseSyncManager
import com.example.database.TokenRepository
import com.example.model.CryptoToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class CryptoViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = TokenRepository(database.cryptoTokenDao())
    val firebaseSyncManager = FirebaseSyncManager(application)

    // Last update tracking
    private val _lastUpdated = MutableStateFlow(System.currentTimeMillis())
    val lastUpdated: StateFlow<Long> = _lastUpdated

    // Filtering & Sorting State
    val searchQuery = MutableStateFlow("")
    val selectedSector = MutableStateFlow("All")
    val showWatchlistOnly = MutableStateFlow(false)
    val maxPsRatio = MutableStateFlow(500f) // slider threshold
    val minMarketCap = MutableStateFlow(0f) // min MC thresholds in Millions USD

    // Sorting parameters
    val sortByColumn = MutableStateFlow("Market Cap") // Column name to sort on
    val sortAscending = MutableStateFlow(false) // Toggle asc/desc

    // App Navigation Option State (Home, Watchlist, Activity Log, Firebase Config)
    val currentNavigationTab = MutableStateFlow("Home")

    // Simulating general system activity logs (like HTML's "Recent Activity") and dynamic state updates
    private val _activityLogs = MutableStateFlow<List<ActivityLog>>(
        listOf(
            ActivityLog("Data sync initialized", "Local DB is warm and populated with premium records.", "Just now", "sync"),
            ActivityLog("AERO price target alert", "Market Maker volume spike of 18.2% registered on Base.", "5m ago", "alert")
        )
    )
    val activityLogs: StateFlow<List<ActivityLog>> = _activityLogs

    // Active column visibility checklist
    val columnVisibility = MutableStateFlow(
        mapOf(
            "FDV" to true,
            "TVL" to true,
            "Revenue" to true,
            "P/S Ratio" to true,
            "Active Addresses" to true,
            "24h Volume" to true
        )
    )

    // Watchlist set
    val watchlistIds: StateFlow<List<String>> = repository.watchlistIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All available tokens from DB cache
    val allTokens: StateFlow<List<CryptoToken>> = repository.allTokens
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered & Sorted presentation tokens
    val displayedTokens: StateFlow<List<CryptoToken>> = combine(
        allTokens,
        searchQuery,
        selectedSector,
        showWatchlistOnly,
        watchlistIds,
        maxPsRatio,
        minMarketCap,
        sortByColumn,
        sortAscending
    ) { arrayOfFlows ->
        @Suppress("UNCHECKED_CAST")
        val tokens = arrayOfFlows[0] as List<CryptoToken>
        val query = arrayOfFlows[1] as String
        val sector = arrayOfFlows[2] as String
        val watchlistOnly = arrayOfFlows[3] as Boolean
        val watchIds = arrayOfFlows[4] as List<String>
        val maxPS = arrayOfFlows[5] as Float
        val minMC = arrayOfFlows[6] as Float
        val sortCol = arrayOfFlows[7] as String
        val sortAsc = arrayOfFlows[8] as Boolean

        var filteredList = tokens

        // 1. Sector Search filter
        if (query.trim().isNotEmpty()) {
            filteredList = filteredList.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.symbol.contains(query, ignoreCase = true)
            }
        }

        // 2. Sector Category filter
        if (sector != "All" && !watchlistOnly) {
            filteredList = filteredList.filter { it.sector == sector }
        }

        // 3. Watchlist Filter
        if (watchlistOnly) {
            filteredList = filteredList.filter { watchIds.contains(it.id) }
        }

        // 4. Fundamental metrics constraints
        filteredList = filteredList.filter {
            it.psRatio <= maxPS && (it.marketCap / 1e6) >= minMC
        }

        // 5. Dynamic Sorting
        val sortedList = when (sortCol) {
            "Price" -> filteredList.sortedBy { it.price }
            "Market Cap" -> filteredList.sortedBy { it.marketCap }
            "FDV" -> filteredList.sortedBy { it.fullyDilutedValuation }
            "TVL" -> filteredList.sortedBy { it.tvl }
            "Revenue" -> filteredList.sortedBy { it.annualizedRevenue }
            "P/S Ratio" -> filteredList.sortedBy { it.psRatio }
            "Active Addresses" -> filteredList.sortedBy { it.activeAddresses }
            "24h Volume" -> filteredList.sortedBy { it.volume24h }
            "24h Change" -> filteredList.sortedBy { it.revenue24hChange }
            else -> filteredList.sortedBy { it.marketCap }
        }

        if (sortAsc) sortedList else sortedList.reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Check cache empty, initialize prepopulated CIS tokens
            repository.initializeDatabaseIfEmpty()
        }
    }

    // Toggle target token stars on Watchlist
    fun toggleWatchlist(tokenId: String) {
        viewModelScope.launch {
            val list = watchlistIds.value
            if (list.contains(tokenId)) {
                repository.removeFromWatchlist(tokenId)
                addLog("Watchlist update", "Removed $tokenId from personal tracking.", "sync")
            } else {
                repository.addToWatchlist(tokenId)
                addLog("Watchlist update", "Added $tokenId to personal tracking.", "sync")
            }
        }
    }

    // Toggle customized visibility columns
    fun toggleColumnVisibility(column: String) {
        val current = columnVisibility.value.toMutableMap()
        current[column] = !(current[column] ?: true)
        columnVisibility.value = current
    }

    fun addLog(title: String, desc: String, type: String = "info") {
        val logs = _activityLogs.value.toMutableList()
        logs.add(0, ActivityLog(title, desc, "Just now", type))
        if (logs.size > 15) {
            logs.removeLast()
        }
        _activityLogs.value = logs
    }

    // Sync watchlist directly to Firebase Firestore bidirectional merge
    fun triggerFirebaseSync() {
        viewModelScope.launch {
            addLog("Firebase sync requested", "Attempting bidirectional merge with Firestore collection.", "sync")
            val localIds = watchlistIds.value
            val mergedIds = firebaseSyncManager.syncWatchlist(localIds)
            
            // Re-reconcile returned merged watchlist to local Room cache
            // First clear removed
            for (id in localIds) {
                if (!mergedIds.contains(id)) {
                    repository.removeFromWatchlist(id)
                }
            }
            // Add new missing ones
            for (id in mergedIds) {
                if (!localIds.contains(id)) {
                    repository.addToWatchlist(id)
                }
            }
            addLog("Firebase sync complete", "Merged state synchronized (${mergedIds.size} watch tokens).", "sync")
        }
    }

    // Hourly Update background simulate
    fun simulateHourlyUpdate() {
        viewModelScope.launch {
            val currentTokens = allTokens.value
            if (currentTokens.isNotEmpty()) {
                val updated = currentTokens.map { token ->
                    // Mutate price by small random percentage (-2% to +3%)
                    val priceMultiplier = 1.0 + (Random.nextDouble(-0.02, 0.03))
                    val newPrice = token.price * priceMultiplier
                    val change24h = (Random.nextDouble(-12.0, 15.0))
                    
                    // Mutate stats
                    val activeAddressesDelta = Random.nextInt(-1500, 2000)
                    val newActiveAddresses = (token.activeAddresses + activeAddressesDelta).coerceAtLeast(100)

                    val newPriceHistory = token.historyPrices.toMutableList()
                    if (newPriceHistory.size >= 7) newPriceHistory.removeAt(0)
                    newPriceHistory.add(newPrice)

                    token.copy(
                        price = newPrice,
                        marketCap = token.marketCap * priceMultiplier,
                        fullyDilutedValuation = token.fullyDilutedValuation * priceMultiplier,
                        revenue24hChange = change24h,
                        activeAddresses = newActiveAddresses,
                        historyPrices = newPriceHistory
                    )
                }
                repository.saveTokensToCache(updated)
                _lastUpdated.value = System.currentTimeMillis()
                addLog("Metrics tick updated", "All fundamental on-chain records mutated and recalculated.", "alert")
            }
        }
    }
}

data class ActivityLog(
    val title: String,
    val desc: String,
    val timeAgo: String,
    val type: String // "sync", "alert", "auth", "info"
)
