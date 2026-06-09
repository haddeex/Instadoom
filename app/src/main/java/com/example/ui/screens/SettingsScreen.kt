package com.example.ui.screens

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.InstaFocusAccessibilityService
import com.example.ui.components.GlassCard
import com.example.ui.theme.InstaOrange
import com.example.ui.theme.InstaRose
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.InstaViewModel

@Composable
fun SettingsScreen(
    viewModel: InstaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()
    val scrollState = rememberScrollState()

    val dailyLimit = settings?.dailyReelLimit ?: 20

    // Stateful permission checks
    var isAccessibilityEnabled by remember {
        mutableStateOf(isAccessibilityServiceEnabled(context, InstaFocusAccessibilityService::class.java))
    }
    var isUsageAccessEnabled by remember {
        mutableStateOf(isUsageStatsPermissionGranted(context))
    }

    // Update permission status of current lifecycle
    LaunchedEffect(Unit) {
        isAccessibilityEnabled = isAccessibilityServiceEnabled(context, InstaFocusAccessibilityService::class.java)
        isUsageAccessEnabled = isUsageStatsPermissionGranted(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Limit configuration card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0x1FEEFF41), CircleShape)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = "Target",
                        tint = Color(0xFFFFD600),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Configure Boundaries",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Daily Reels watch limit. We recommend 15-20 reels a day. Going lower drastically boosts mental health, focusing stamina, and attention span.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                FilledIconButton(
                    onClick = {
                        if (dailyLimit > 5) viewModel.updateDailyLimit(dailyLimit - 5)
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFF1E1E24)
                    )
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                }

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$dailyLimit",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "REELS / DAY",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                FilledIconButton(
                    onClick = {
                        if (dailyLimit < 100) viewModel.updateDailyLimit(dailyLimit + 5)
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFF1E1E24)
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }

        // Live Real-Time Tracking permissions cards
        Text(
            text = "System Real-Time Tracking & Hooks",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // 1. Accessibility Service Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = "Accessibility Service Hook",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Required to count Reel scroll swaps accurately inside Instagram. Runs local-only with 0 connections.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Surface(
                    color = if (isAccessibilityEnabled) Color(0x1F4CAF50) else Color(0x1FFF1744),
                    shape = CircleShape,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = if (isAccessibilityEnabled) "CONNECTED" else "STOPPED",
                        color = if (isAccessibilityEnabled) Color(0xFF81C784) else Color(0xFFFF5252),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // fallback
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAccessibilityEnabled) Color(0xFF1E1E24) else InstaRose
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "settings",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAccessibilityEnabled) "Manage Service" else "Enable Accessibility Service",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2. Usage Stats Foreground Tracking Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = "Stats Foreground Time Tracking",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Matches active Instagram open window logs for absolute precision.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Surface(
                    color = if (isUsageAccessEnabled) Color(0x1F4CAF50) else Color(0x1FFF1744),
                    shape = CircleShape,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = if (isUsageAccessEnabled) "GRANTED" else "REQUIRED",
                        color = if (isUsageAccessEnabled) Color(0xFF81C784) else Color(0xFFFF5252),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // fallback
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUsageAccessEnabled) Color(0xFF1E1E24) else InstaOrange
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Lock",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isUsageAccessEnabled) "Manage Usage Access" else "Grant Usage Stats Permission",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Quick setup steps helper
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "How to configure real-time tracking:",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "1. Click 'Enable Accessibility Service' above.\n" +
                       "2. Look for 'InstaFocus' in the download services list.\n" +
                       "3. Turn the switch ON.\n" +
                       "4. Return here. InstaFocus will now auto-increment Reel counts live whenever you vertically scroll in Instagram Reels feed!",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

private fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<out AccessibilityService>): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
    for (enabledService in enabledServices) {
        val enabledServiceInfo = enabledService.resolveInfo.serviceInfo
        if (enabledServiceInfo.packageName == context.packageName && enabledServiceInfo.name == serviceClass.name) {
            return true
        }
    }
    return false
}

private fun isUsageStatsPermissionGranted(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}
