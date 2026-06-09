package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStats(
    @PrimaryKey val date: String, // format: "YYYY-MM-DD"
    val reelsCount: Int,
    val usageDurationMs: Long,
    val limitExceeded: Boolean
)

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: String = "settings",
    val dailyReelLimit: Int = 20,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastActiveDate: String = "" // format: "YYYY-MM-DD"
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val iconName: String,
    val unlockCondition: String // e.g. "REELS_10", "STREAK_5", "ZERO_REELS"
)
