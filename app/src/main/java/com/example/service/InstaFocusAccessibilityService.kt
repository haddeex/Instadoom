package com.example.service

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.database.AppDatabase
import com.example.data.repository.InstaRepository
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class InstaFocusAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var repository: InstaRepository? = null

    private var lastScrollTime: Long = 0L
    private val SCROLL_DEBOUNCE_MS = 2500L // Min time between reel counts

    // Time tracking
    private var instagramStartTime: Long = 0L
    private var activeTrackingJob: Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("InstaFocus", "Accessibility Service Connected")
        val db = AppDatabase.getDatabase(this)
        repository = InstaRepository(db.instaDao)
        createNotificationChannel()

        // Sync and initialize achievements
        serviceScope.launch {
            repository?.initializeAchievements()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return

        // 1. App Foreground Tracking: Detect open/close of Instagram
        if (packageName == "com.instagram.android") {
            if (instagramStartTime == 0L) {
                instagramStartTime = System.currentTimeMillis()
                startRealtimeTimeTracking()
            }
        } else {
            if (instagramStartTime != 0L) {
                stopRealtimeTimeTracking()
            }
        }

        // 2. Count Reels from vertical scroll actions within Instagram
        if (packageName == "com.instagram.android" && event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastScrollTime >= SCROLL_DEBOUNCE_MS) {
                // To be certain this is a meaningful swipe, verify scroll changes
                val fromIndex = event.fromIndex
                val toIndex = event.toIndex
                val scrollY = event.scrollY

                // Verify it's a vertical scrolling gesture
                if (event.isScrollable) {
                    lastScrollTime = currentTime
                    incrementReelCount()
                }
            }
        }
    }

    private fun startRealtimeTimeTracking() {
        activeTrackingJob?.cancel()
        activeTrackingJob = serviceScope.launch {
            while (isActive && instagramStartTime != 0L) {
                delay(5000L) // update every 5 seconds
                val now = System.currentTimeMillis()
                val delta = now - instagramStartTime
                instagramStartTime = now

                val todayStr = getTodayString()
                repository?.incrementUsageDuration(todayStr, delta)
                com.example.widget.InstaFocusWidgetProvider.triggerWidgetUpdate(applicationContext)
            }
        }
    }

    private fun stopRealtimeTimeTracking() {
        activeTrackingJob?.cancel()
        val now = System.currentTimeMillis()
        if (instagramStartTime != 0L) {
            val delta = now - instagramStartTime
            instagramStartTime = 0L
            serviceScope.launch {
                val todayStr = getTodayString()
                repository?.incrementUsageDuration(todayStr, delta)
                com.example.widget.InstaFocusWidgetProvider.triggerWidgetUpdate(applicationContext)
            }
        }
    }

    private fun incrementReelCount() {
        serviceScope.launch {
            val todayStr = getTodayString()
            val updatedStats = repository?.incrementReelsCount(todayStr) ?: return@launch
            val reelsCount = updatedStats.reelsCount
            com.example.widget.InstaFocusWidgetProvider.triggerWidgetUpdate(applicationContext)

            // Highlight alerts for 10, 25, 50 reels
            withContext(Dispatchers.Main) {
                when (reelsCount) {
                    10 -> {
                        showMotivationalWarning(
                            "Mindfulness Check 🛑",
                            "You have scrolled through 10 Reels. Take a deep breath. Is Instagram really where you want to spend your day?"
                        )
                    }
                    25 -> {
                        showMotivationalWarning(
                            "Moderate Scrolling Alert! ⚠️",
                            "25 Reels watched. Your focus is slipping away. Close Instagram and do 5 pushups!"
                        )
                    }
                    50 -> {
                        showMotivationalWarning(
                            "CRITICAL SCROLL WARNING! 🔥",
                            "50 Reels! You are officially in a doom loop. Put the phone down now!"
                        )
                    }
                }

                // Check limit exceeded alert
                val settings = repository?.getOrCreateUserSettings()
                if (settings != null && reelsCount == settings.dailyReelLimit) {
                    showLimitExceededNotification()
                }
            }
        }
    }

    private fun showMotivationalWarning(title: String, message: String) {
        // Show Toast on Main Thread which displays over standard windows
        Toast.makeText(applicationContext, "InstaFocus: $title - $message", Toast.LENGTH_LONG).show()
        sendNotification(title, message, 1001)
    }

    private fun showLimitExceededNotification() {
        val title = "🎯 Daily Reel Limit Reached!"
        val message = "You've watched your limit of Reels today. Maintain your focus and streak by closing Instagram now!"
        Toast.makeText(applicationContext, "InstaFocus: $title\n$message", Toast.LENGTH_LONG).show()
        sendNotification(title, message, 1002)

        // Automatically unlock the milestone badge
        serviceScope.launch {
            repository?.unlockAchievementDirect("warning_badge")
        }
    }

    private fun sendNotification(title: String, message: String, notificationId: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notif_channel_name)
            val descriptionText = getString(R.string.notif_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onInterrupt() {
        stopRealtimeTimeTracking()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRealtimeTimeTracking()
        serviceScope.cancel()
    }

    private fun getTodayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    companion object {
        private const val CHANNEL_ID = "instafocus_behavioral_nudges"
    }
}
