package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.InstaOrange
import com.example.ui.theme.InstaPurple
import com.example.ui.theme.InstaRose
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.InstaViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    viewModel: InstaViewModel,
    modifier: Modifier = Modifier
) {
    val todayStats by viewModel.todayStats.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val scrollState = rememberScrollState()

    val reelsCount = todayStats?.reelsCount ?: 0
    val dailyLimit = settings?.dailyReelLimit ?: 20
    val usageTimeMs = todayStats?.usageDurationMs ?: 0L

    val ratio = if (dailyLimit > 0) reelsCount.toFloat() / dailyLimit.toFloat() else 0f
    val remaining = maxOf(0, dailyLimit - reelsCount)

    // Animated percentage progress
    val progressAnimate by animateFloatAsState(
        targetValue = minOf(1f, ratio),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Circular Gauge with Ambient Glowing Ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .size(240.dp)
        ) {
            // Background track
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color(0xFF1E1E24),
                    startAngle = -220f,
                    sweepAngle = 260f,
                    useCenter = false,
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Foreground active track (instarose -> instaorange gradient)
            val strokeGradient = Brush.sweepGradient(
                colors = listOf(InstaPurple, InstaRose, InstaOrange, InstaPurple)
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    brush = strokeGradient,
                    startAngle = -220f,
                    sweepAngle = 260f * progressAnimate,
                    useCenter = false,
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Info within center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$reelsCount",
                    color = Color.White,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = "REELS TODAY",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                Surface(
                    color = if (reelsCount > dailyLimit) Color(0x32FF1744) else Color(0x1F4CAF50),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (reelsCount > dailyLimit) "Limit Exceeded" else "$remaining Remaining",
                        color = if (reelsCount > dailyLimit) Color(0xFFFF5252) else Color(0xFF81C784),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Real-Time Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instagram Active Screen Time Card
            GlassCard(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x1F2979FF), CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = "Screen Time",
                            tint = Color(0xFF2979FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "INSTAGRAM TIME",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = viewModel.formatDuration(usageTimeMs),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Streak Metric Card
            GlassCard(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x1FEEFF41), CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFFF9100),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "CURRENT STREAK",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${settings?.currentStreak ?: 0} Day(s)",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Behavioural Psychology Nudge Banner
        if (reelsCount >= 10) {
            val nudgeMessage = when {
                reelsCount >= 50 -> "⚠️ Extreme focus breach! 50 Reels watched. Close Instagram and ground yourself immediately! Your attention span is too valuable."
                reelsCount >= 25 -> "⚠️ You've crossed 25 Reels. This is excessive scrolling. Take a walk, do some breathing, or read a page of a book!"
                else -> "🛑 10 Reels watched today. You are at a vital choice point. Choose focus over mindless consumption now."
            }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x21FC297B)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(InstaRose, InstaOrange))
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Nudge warning",
                            tint = InstaRose
                        )
                        Text(
                            text = "IN-THE-MOMENT NUDGE",
                            fontWeight = FontWeight.Bold,
                            color = InstaRose,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = nudgeMessage,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Interactive Simulator Frame (Outstanding UX & Testing for preview emulator)
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Simulation Playground",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Simulate Instagram activity within the AI Studio emulator to trigger alerts, warnings, and unlock badges instantly.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.simulateReelWatch() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InstaRose
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Simulate",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Watch a Reel", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.simulateActiveTime(5) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAlarm,
                        contentDescription = "Add Time",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+5 Mins", fontSize = 12.sp)
                }
            }

            TextButton(
                onClick = { viewModel.resetSimulation() },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFFF5252)
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Reset",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset Playground", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
