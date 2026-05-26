package com.example.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptoTokenDao {
    @Query("SELECT * FROM crypto_tokens")
    fun getAllTokens(): Flow<List<CryptoTokenEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTokens(tokens: List<CryptoTokenEntity>)

    @Query("SELECT * FROM crypto_tokens WHERE id = :id")
    fun getTokenById(id: String): Flow<CryptoTokenEntity?>

    @Query("SELECT * FROM watchlist_items")
    fun getWatchlistItems(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(item: WatchlistEntity)

    @Query("DELETE FROM watchlist_items WHERE id = :id")
    suspend fun removeFromWatchlist(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_items WHERE id = :id)")
    fun isInWatchlist(id: String): Flow<Boolean>
}
