package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keyword_traps")
data class KeywordTrapEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val keyword: String,
    val categoryFilter: String = "ALL",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val matchCount: Int = 0
)
