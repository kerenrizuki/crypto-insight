package com.example.database

import android.content.Context
import com.example.model.CryptoToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class TokenRepository(private val cryptoTokenDao: CryptoTokenDao) {

    // Retrieve all tokens mapped from entity to domain model
    val allTokens: Flow<List<CryptoToken>> = cryptoTokenDao.getAllTokens().map { entities ->
        entities.map { it.toDomain() }
    }

    // Retrieve the list of watchlist item IDs
    val watchlistIds: Flow<List<String>> = cryptoTokenDao.getWatchlistItems().map { items ->
        items.map { it.id }
    }

    suspend fun initializeDatabaseIfEmpty() {
        val existing = cryptoTokenDao.getAllTokens().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val prePopulated = CryptoToken.getPrePopulatedTokens().map { it.toEntity() }
            cryptoTokenDao.insertAllTokens(prePopulated)
        }
    }

    suspend fun saveTokensToCache(tokens: List<CryptoToken>) {
        cryptoTokenDao.insertAllTokens(tokens.map { it.toEntity() })
    }

    suspend fun addToWatchlist(tokenId: String) {
        cryptoTokenDao.addToWatchlist(WatchlistEntity(id = tokenId))
    }

    suspend fun removeFromWatchlist(tokenId: String) {
        cryptoTokenDao.removeFromWatchlist(tokenId)
    }

    fun isTokenInWatchlist(tokenId: String): Flow<Boolean> {
        return cryptoTokenDao.isInWatchlist(tokenId)
    }

    // Map Domain to Entity
    private fun CryptoToken.toEntity() = CryptoTokenEntity(
        id = id,
        name = name,
        symbol = symbol,
        sector = sector,
        price = price,
        marketCap = marketCap,
        fullyDilutedValuation = fullyDilutedValuation,
        tvl = tvl,
        annualizedRevenue = annualizedRevenue,
        psRatio = psRatio,
        activeAddresses = activeAddresses,
        volume24h = volume24h,
        revenue24hChange = revenue24hChange,
        description = description,
        tokenomicsDetails = tokenomicsDetails,
        historyPrices = historyPrices,
        historyRevenue = historyRevenue,
        historyTvl = historyTvl
    )

    // Map Entity to Domain
    private fun CryptoTokenEntity.toDomain() = CryptoToken(
        id = id,
        name = name,
        symbol = symbol,
        sector = sector,
        price = price,
        marketCap = marketCap,
        fullyDilutedValuation = fullyDilutedValuation,
        tvl = tvl,
        annualizedRevenue = annualizedRevenue,
        psRatio = psRatio,
        activeAddresses = activeAddresses,
        volume24h = volume24h,
        revenue24hChange = revenue24hChange,
        description = description,
        tokenomicsDetails = tokenomicsDetails,
        historyPrices = historyPrices,
        historyRevenue = historyRevenue,
        historyTvl = historyTvl
    )
}
