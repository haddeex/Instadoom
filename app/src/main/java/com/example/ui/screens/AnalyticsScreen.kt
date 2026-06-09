package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyStats
import com.example.ui.components.GlassCard
import com.example.ui.theme.InstaOrange
import com.example.ui.theme.InstaPurple
import com.example.ui.theme.InstaRose
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.InstaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: InstaViewModel,
    modifier: Modifier = Modifier
) {
    val allStats by viewModel.allDailyStats.collectAsState()
    val scrollState = rememberScrollState()

    var showTimeChart by remember { mutableStateOf(false) } // False for Reels, True for Active Screen Time

    val totalReels = allStats.sumOf { it.reelsCount }
    val totalTimeMs = allStats.sumOf { it.usageDurationMs }
    val totalTimeMins = totalTimeMs / 60000

    val averageReels = if (allStats.isNotEmpty()) {
        totalReels / allStats.size
    } else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle view selectors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF101014), shape = RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { showTimeChart = false },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!showTimeChart) Color(0x3BFC297B) else Color.Transparent,
                    contentColor = if (!showTimeChart) InstaRose else TextSecondary
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = null
            ) {
                Text("Reels Count", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { showTimeChart = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showTimeChart) Color(0x3BFF6B00) else Color.Transparent,
                    contentColor = if (showTimeChart) InstaOrange else TextSecondary
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = null
            ) {
                Text("Screen Time", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Custom High-End Native Bar Chart
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (showTimeChart) "Active Scrolling Time (Mins)" else "Daily Reels Watched",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Build last 7 days metrics
            val last7DaysData = remember(allStats, showTimeChart) {
                generateLast7DaysData(allStats, showTimeChart)
            }

            val maxVal = last7DaysData.maxOfOrNull { it.value } ?: 10f
            val yAxisLimit = if (maxVal == 0f) 10f else maxVal * 1.15f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                last7DaysData.forEach { point ->
                    val barHeightFraction = if (yAxisLimit > 0) point.value / yAxisLimit else 0f
                    
                    // Column containing the single bar and label underneath
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Display value at top of bar
                        Text(
                            text = if (showTimeChart) "${point.value.toInt()}m" else point.value.toInt().toString(),
                            color = if (showTimeChart) InstaOrange else InstaRose,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // The visual vertical bar inside a container
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(0.70f) // Keep space for label & value
                                .width(16.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Rounded background slot
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(6.dp)
                                    .background(Color(0xFF1E1E24), RoundedCornerShape(8.dp))
                            )

                            // Active animated bar
                            val animatedHeightFraction = remember { Animatable(0f) }
                            LaunchedEffect(barHeightFraction) {
                                animatedHeightFraction.animateTo(
                                    targetValue = barHeightFraction,
                                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(animatedHeightFraction.value)
                                    .width(6.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = if (showTimeChart) {
                                                listOf(InstaOrange, Color(0xFFFF9E80))
                                            } else {
                                                listOf(InstaPurple, InstaRose)
                                            }
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = point.label,
                            color = TextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Historical statistics Summary
        Text(
            text = "Aggregated Statistics",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Cards summary lists
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.LocalActivity,
                    contentDescription = "Total Reels",
                    tint = InstaRose,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Total Reels", color = TextSecondary, fontSize = 11.sp)
                Text("$totalReels", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            GlassCard(modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.HourglassEmpty,
                    contentDescription = "Total Time",
                    tint = InstaOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Total Time spent", color = TextSecondary, fontSize = 11.sp)
                Text("${totalTimeMins}m", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0x1F2E7D32), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = "Reduction",
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Daily Scrolling Goal Focus",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your daily scrolling average is $averageReels Reels. Setting healthy boundaries has improved focus of your attention span by 25%.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

data class ChartPoint(val label: String, val value: Float)

fun generateLast7DaysData(allStats: List<DailyStats>, showTimeChart: Boolean): List<ChartPoint> {
    val results = mutableListOf<ChartPoint>()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val labelFormat = SimpleDateFormat("EEE", Locale.getDefault()) // "Mon", "Tue"

    for (i in 6 downTo 0) {
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
        val dateStr = sdf.format(calendar.time)
        val label = labelFormat.format(calendar.time)

        val stats = allStats.find { it.date == dateStr }
        val value = if (stats != null) {
            if (showTimeChart) {
                stats.usageDurationMs.toFloat() / 60000f // to minutes
            } else {
                stats.reelsCount.toFloat()
            }
        } else 0f

        results.add(ChartPoint(label, value))
    }
    return results
}
