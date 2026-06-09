package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.R
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class InstaFocusWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val db = AppDatabase.getDatabase(context)
        val dao = db.instaDao

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())

        CoroutineScope(Dispatchers.IO).launch {
            val stats = dao.getDailyStatsForDate(todayStr)
            val settings = dao.getUserSettings()

            val streak = settings?.currentStreak ?: 0
            val limit = settings?.dailyReelLimit ?: 20
            val reels = stats?.reelsCount ?: 0
            val timeMs = stats?.usageDurationMs ?: 0L
            val minutes = timeMs / 60000

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_layout)
                
                views.setTextViewText(R.id.widget_streak, "🔥 $streak day${if (streak == 1) "" else "s"}")
                views.setTextViewText(R.id.widget_reels, "Reels: $reels / $limit")
                views.setTextViewText(R.id.widget_time, "Instagram: $minutes min${if (minutes == 1L) "" else "s"}")

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidget = ComponentName(context.packageName, InstaFocusWidgetProvider::class.java.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.widget.ACTION_UPDATE_WIDGET"

        fun triggerWidgetUpdate(context: Context) {
            val intent = Intent(context, InstaFocusWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}
