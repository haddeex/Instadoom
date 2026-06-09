package com.example.data.repository

import com.example.data.database.InstaDao
import com.example.data.model.DailyStats
import com.example.data.model.UserSettings
import com.example.data.model.Achievement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class InstaRepository(private val dao: InstaDao) {

    val allDailyStats: Flow<List<DailyStats>> = dao.getAllDailyStats()
    val userSettings: Flow<UserSettings?> = dao.getUserSettingsFlow()
    val achievements: Flow<List<Achievement>> = dao.getAllAchievementsFlow()

    fun getDailyStatsForDateFlow(date: String): Flow<DailyStats?> = dao.getDailyStatsForDateFlow(date)

    suspend fun getOrCreateDailyStats(date: String): DailyStats {
        val existing = dao.getDailyStatsForDate(date)
        if (existing != null) return existing

        val settings = getOrCreateUserSettings()
        val newStats = DailyStats(
            date = date,
            reelsCount = 0,
            usageDurationMs = 0L,
            limitExceeded = false
        )
        dao.insertDailyStats(newStats)
        return newStats
    }

    suspend fun getOrCreateUserSettings(): UserSettings {
        val existing = dao.getUserSettings()
        if (existing != null) return existing

        val defaultSettings = UserSettings()
        dao.insertUserSettings(defaultSettings)
        return defaultSettings
    }

    suspend fun updateDailyLimit(newLimit: Int) {
        val settings = getOrCreateUserSettings()
        val updated = settings.copy(dailyReelLimit = newLimit)
        dao.insertUserSettings(updated)

        // Re-evaluate limit exceeded for today
        val todayStr = getTodayString()
        val todayStats = dao.getDailyStatsForDate(todayStr)
        if (todayStats != null) {
            val exceeded = todayStats.reelsCount > newLimit
            dao.insertDailyStats(todayStats.copy(limitExceeded = exceeded))
        }
        recalculateStreaks()
    }

    suspend fun incrementReelsCount(date: String): DailyStats {
        val stats = getOrCreateDailyStats(date)
        val settings = getOrCreateUserSettings()
        
        val newCount = stats.reelsCount + 1
        val exceeded = newCount > settings.dailyReelLimit

        val updatedStats = stats.copy(
            reelsCount = newCount,
            limitExceeded = exceeded
        )
        dao.insertDailyStats(updatedStats)

        // Check if we should update streaks or unlock notifications/achievements
        recalculateStreaks()
        checkAndUnlockAchievements()

        return updatedStats
    }

    suspend fun incrementUsageDuration(date: String, durationMs: Long): DailyStats {
        val stats = getOrCreateDailyStats(date)
        val updatedStats = stats.copy(
            usageDurationMs = stats.usageDurationMs + durationMs
        )
        dao.insertDailyStats(updatedStats)
        
        checkAndUnlockAchievements()
        return updatedStats
    }

    suspend fun updateUsageDurationDirect(date: String, durationMs: Long): DailyStats {
        val stats = getOrCreateDailyStats(date)
        val updatedStats = stats.copy(
            usageDurationMs = durationMs
        )
        dao.insertDailyStats(updatedStats)
        return updatedStats
    }

    suspend fun initializeAchievements() {
        val existing = dao.getAllAchievements()
        if (existing.isNotEmpty()) return

        val defaults = listOf(
            Achievement(
                id = "first_steps",
                title = "Mindfulness Spark",
                description = "Keep reels under your limit for a full day.",
                isUnlocked = false,
                iconName = "ic_spark",
                unlockCondition = "STREAK_1"
            ),
            Achievement(
                id = "clean_focus_3",
                title = "Focused Warrior",
                description = "Maintain focus streak for 3 consecutive days.",
                isUnlocked = false,
                iconName = "ic_warrior",
                unlockCondition = "STREAK_3"
            ),
            Achievement(
                id = "ascetic_7",
                title = "Doom Scroll Slayer",
                description = "Stay under your limit for 7 consecutive days.",
                isUnlocked = false,
                iconName = "ic_slayer",
                unlockCondition = "STREAK_7"
            ),
            Achievement(
                id = "clean_slate",
                title = "Zen Master",
                description = "Complete a day with exactly 0 reels watched.",
                isUnlocked = false,
                iconName = "ic_zen",
                unlockCondition = "ZERO_REELS"
            ),
            Achievement(
                id = "warning_badge",
                title = "Self-Awareness",
                description = "Trigger a 10, 25, or 50 reels warning nudge.",
                isUnlocked = false,
                iconName = "ic_aware",
                unlockCondition = "WARNING_TRIGGERED"
            )
        )
        dao.insertAchievements(defaults)
    }

    suspend fun unlockAchievementDirect(id: String) {
        val achievementsList = dao.getAllAchievements()
        val match = achievementsList.find { it.id == id }
        if (match != null && !match.isUnlocked) {
            dao.updateAchievement(match.copy(isUnlocked = true))
        }
    }

    suspend fun checkAndUnlockAchievements() {
        val settings = getOrCreateUserSettings()
        val allStats = dao.getAllDailyStats().firstOrNull() ?: emptyList()
        val achievementsList = dao.getAllAchievements()

        for (achievement in achievementsList) {
            if (achievement.isUnlocked) continue

            var shouldUnlock = false
            when (achievement.unlockCondition) {
                "STREAK_1" -> {
                    if (settings.currentStreak >= 1) {
                        shouldUnlock = true
                    }
                }
                "STREAK_3" -> {
                    if (settings.currentStreak >= 3) {
                        shouldUnlock = true
                    }
                }
                "STREAK_7" -> {
                    if (settings.currentStreak >= 7) {
                        shouldUnlock = true
                    }
                }
                "ZERO_REELS" -> {
                    // check if there is any completed day (not today, unless today is finished, but we can look for any past day with reelsCount == 0 and usageDurationMs > 0)
                    val zeroDay = allStats.any { it.date != getTodayString() && it.reelsCount == 0 && it.usageDurationMs > 10000 }
                    if (zeroDay) {
                        shouldUnlock = true
                    }
                }
                "WARNING_TRIGGERED" -> {
                    // if reels today passed 10
                    val todayStats = dao.getDailyStatsForDate(getTodayString())
                    if (todayStats != null && todayStats.reelsCount >= 10) {
                        shouldUnlock = true
                    }
                }
            }

            if (shouldUnlock) {
                dao.updateAchievement(achievement.copy(isUnlocked = true))
            }
        }
    }

    suspend fun recalculateStreaks() {
        val settings = getOrCreateUserSettings()
        val allStats = dao.getAllDailyStats().firstOrNull()?.sortedByDescending { it.date } ?: emptyList()
        if (allStats.isEmpty()) return

        val todayStr = getTodayString()
        
        // Let's loop back starting from today or yesterday to calculate the streak
        var streak = 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Check if today is completed or below limit
        val todayStats = allStats.find { it.date == todayStr }
        val yesterdayCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayStr = sdf.format(yesterdayCalendar.time)
        val yesterdayStats = allStats.find { it.date == yesterdayStr }

        var checkDate = Calendar.getInstance()
        
        // If today has exceeded the limit, the current streak is broken (0)
        if (todayStats != null && todayStats.limitExceeded) {
            streak = 0
        } else {
            // Let's chain backwards starting today or yesterday
            var currentCheckStr = todayStr
            var dateInCalendar = Calendar.getInstance()

            // If today doesn't exist yet, or is not exceeded, we start counting from yesterday
            if (todayStats == null) {
                currentCheckStr = yesterdayStr
                dateInCalendar = yesterdayCalendar
            }

            var stop = false
            while (!stop) {
                val statsForDate = allStats.find { it.date == currentCheckStr }
                if (statsForDate != null) {
                    if (!statsForDate.limitExceeded && (statsForDate.reelsCount > 0 || statsForDate.usageDurationMs > 0)) {
                        streak++
                        // go to previous day
                        dateInCalendar.add(Calendar.DAY_OF_YEAR, -1)
                        currentCheckStr = sdf.format(dateInCalendar.time)
                    } else if (statsForDate.limitExceeded) {
                        stop = true
                    } else {
                        // recorded but 0 use (no scrolling recorded), we don't break but don't count, or we continue checking
                        dateInCalendar.add(Calendar.DAY_OF_YEAR, -1)
                        currentCheckStr = sdf.format(dateInCalendar.time)
                    }
                } else {
                    // No entry for this day - if it's the start (today/yesterday), we can allow today gaps. 
                    // But if it's further back, a gap breaks the streak.
                    stop = true
                }
            }
        }

        val newBest = maxOf(settings.bestStreak, streak)
        val updated = settings.copy(
            currentStreak = streak,
            bestStreak = newBest,
            lastActiveDate = todayStr
        )
        dao.insertUserSettings(updated)
    }

    private fun getTodayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
