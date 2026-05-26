package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist_items")
data class WatchlistEntity(
    @PrimaryKey val id: String,
    val addedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
