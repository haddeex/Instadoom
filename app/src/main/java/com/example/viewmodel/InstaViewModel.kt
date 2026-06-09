package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.DailyStats
import com.example.data.model.UserSettings
import com.example.data.model.Achievement
import com.example.data.repository.InstaRepository
import com.example.widget.InstaFocusWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class InstaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InstaRepository

    val allDailyStats: StateFlow<List<DailyStats>>
    val userSettings: StateFlow<UserSettings?>
    val achievements: StateFlow<List<Achievement>>

    private val _todayStats = MutableStateFlow<DailyStats?>(null)
    val todayStats: StateFlow<DailyStats?> = _todayStats.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Home, 1: Analytics, 2: Badges & Streaks, 3: Settings/Helper
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = InstaRepository(db.instaDao)

        allDailyStats = repository.allDailyStats
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        userSettings = repository.userSettings
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        achievements = repository.achievements
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Periodically sync today's stats reactively
        viewModelScope.launch {
            repository.initializeAchievements()
            repository.getOrCreateUserSettings()
            repository.recalculateStreaks()

            val todayStr = getTodayString()
            repository.getDailyStatsForDateFlow(todayStr).collect { stats ->
                _todayStats.value = stats ?: DailyStats(todayStr, 0, 0, false)
            }
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun updateDailyLimit(newLimit: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateDailyLimit(newLimit)
            InstaFocusWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    // Playful Simulator for UI Testing in Emulator (without having Instagram installed)
    fun simulateReelWatch() {
        viewModelScope.launch(Dispatchers.IO) {
            val todayStr = getTodayString()
            val stats = repository.incrementReelsCount(todayStr)
            
            // Randomly simulate usage duration (add 12-45 seconds per Reel)
            val randomDuration = (12000L..45000L).random()
            repository.incrementUsageDuration(todayStr, randomDuration)

            // Trigger widget updates
            InstaFocusWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun simulateActiveTime(minutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val todayStr = getTodayString()
            repository.incrementUsageDuration(todayStr, minutes * 60 * 1000L)
            InstaFocusWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun resetSimulation() {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(getApplication())
            // Remove everything to let them test fresh
            db.clearAllTables()
            repository.initializeAchievements()
            repository.getOrCreateUserSettings()
            
            val todayStr = getTodayString()
            db.instaDao.insertDailyStats(DailyStats(todayStr, 0, 0, false))
            InstaFocusWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun formatDuration(durationMs: Long): String {
        val totalSecs = durationMs / 1000
        val hrs = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60

        return when {
            hrs > 0 -> "${hrs}h ${mins}m"
            mins > 0 -> "${mins}m ${secs}s"
            else -> "${secs}s"
        }
    }

    fun getTodayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
