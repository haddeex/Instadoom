package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Achievement
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.viewmodel.InstaViewModel

@Composable
fun BadgesScreen(
    viewModel: InstaViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    val currentStreak = settings?.currentStreak ?: 0
    val bestStreak = settings?.bestStreak ?: 0
    val unlockedCount = achievements.count { it.isUnlocked }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Glowing Fire Streak Card
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFB74D), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak flame",
                        tint = Color(0xFFFF8A00),
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$currentStreak Days Focus Streak",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Staying under your screen limits!",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Record Streak", color = TextTertiary, fontSize = 11.sp)
                        Text("$bestStreak Days", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Badges unlocked", color = TextTertiary, fontSize = 11.sp)
                        Text("$unlockedCount / ${achievements.size}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Achievements Grid
        Text(
            text = "Achievement Milestones",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(achievements) { achievement ->
                BadgeItem(achievement = achievement)
            }
        }
    }
}

@Composable
fun BadgeItem(achievement: Achievement) {
    val brushUnlocked = Brush.horizontalGradient(listOf(InstaRose, InstaOrange))
    val brushLocked = Brush.horizontalGradient(listOf(Color(0xFF22222E), Color(0xFF22222E)))

    val vectorIcon: ImageVector = when (achievement.iconName) {
        "ic_spark" -> Icons.Default.OfflineBolt
        "ic_warrior" -> Icons.Default.Shield
        "ic_slayer" -> Icons.Default.EmojiEvents
        "ic_zen" -> Icons.Default.FilterVintage
        else -> Icons.Default.Stars
    }

    Box(
        modifier = Modifier
            .background(Color(0x1F22222E), shape = RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                brush = if (achievement.isUnlocked) brushUnlocked else Brush.horizontalGradient(listOf(BorderGlass, BorderGlass)),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
            .height(140.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon layout
            Box(
                modifier = Modifier
                    .background(
                        color = if (achievement.isUnlocked) Color(0x3BFC297B) else Color(0x11FFFFFF),
                        shape = CircleShape
                    )
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = vectorIcon,
                    contentDescription = achievement.title,
                    tint = if (achievement.isUnlocked) InstaRose else TextTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title
            Text(
                text = achievement.title,
                color = if (achievement.isUnlocked) Color.White else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Desc
            Text(
                text = achievement.description,
                color = TextTertiary,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).padding(top = 4.dp)
            )

            // Locked state text
            Text(
                text = if (achievement.isUnlocked) "UNLOCKED" else "LOCKED",
                color = if (achievement.isUnlocked) InstaOrange else TextTertiary,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}
