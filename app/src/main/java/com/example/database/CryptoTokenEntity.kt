package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crypto_tokens")
data class CryptoTokenEntity(
    @PrimaryKey val id: String,
    val name: String,
    val symbol: String,
    val sector: String,
    val price: Double,
    val marketCap: Double,
    val fullyDilutedValuation: Double,
    val tvl: Double,
    val annualizedRevenue: Double,
    val psRatio: Double,
    val activeAddresses: Int,
    val volume24h: Double,
    val revenue24hChange: Double,
    val description: String,
    val tokenomicsDetails: String,
    val historyPrices: List<Double>,
    val historyRevenue: List<Double>,
    val historyTvl: List<Double>
)
