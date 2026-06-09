package com.example

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BadgesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BackgroundBlack
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.InstaOrange
import com.example.ui.theme.InstaRose
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.InstaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: InstaViewModel = viewModel()
                val selectedTab by viewModel.selectedTab.collectAsState()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundBlack),
                    bottomBar = {
                        InstaFocusBottomBar(
                            selectedTab = selectedTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BackgroundBlack)
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> HomeScreen(viewModel = viewModel)
                            1 -> AnalyticsScreen(viewModel = viewModel)
                            2 -> BadgesScreen(viewModel = viewModel)
                            3 -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstaFocusBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color(0x7F0E0E12), // translucent dark
        tonalElevation = 0.dp,
        modifier = Modifier
            .padding(12.dp)
            .border(1.dp, BorderGlass, shape = RoundedCornerShape(24.dp))
            .background(Color(0x7F0E0E12), shape = RoundedCornerShape(24.dp))
            .height(68.dp)
            .padding(horizontal = 8.dp),
        windowInsets = WindowInsets(0) // ignore edge-to-edge for cleaner look
    ) {
        val items = listOf(
            Triple("Today", Icons.Default.HourglassEmpty, 0),
            Triple("Analytics", Icons.Default.BarChart, 1),
            Triple("Focus", Icons.Default.LocalFireDepartment, 2),
            Triple("Settings", Icons.Default.Settings, 3)
        )

        items.forEach { (label, icon, index) ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) InstaRose else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0x2BFC297B) // Rose bubble behind icon
                )
            )
        }
    }
}
