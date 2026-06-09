package com.example.data.database

import androidx.room.*
import com.example.data.model.DailyStats
import com.example.data.model.UserSettings
import com.example.data.model.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface InstaDao {
    // Daily Stats
    @Query("SELECT * FROM daily_stats ORDER BY date DESC")
    fun getAllDailyStats(): Flow<List<DailyStats>>

    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    suspend fun getDailyStatsForDate(date: String): DailyStats?

    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    fun getDailyStatsForDateFlow(date: String): Flow<DailyStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyStats(stats: DailyStats)

    // User Settings
    @Query("SELECT * FROM user_settings WHERE id = 'settings' LIMIT 1")
    fun getUserSettingsFlow(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 'settings' LIMIT 1")
    suspend fun getUserSettings(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(settings: UserSettings)

    // Achievements
    @Query("SELECT * FROM achievements")
    fun getAllAchievementsFlow(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievements(): List<Achievement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)
}
