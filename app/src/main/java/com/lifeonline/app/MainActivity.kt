package com.lifeonline.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.lifeonline.app.service.KeepAliveMonitorService
import com.lifeonline.app.ui.screens.dashboard.DashboardScreen
import com.lifeonline.app.ui.screens.history.HistoryScreen
import com.lifeonline.app.ui.screens.journal.JournalScreen
import com.lifeonline.app.ui.screens.onboarding.OnboardingScreen
import com.lifeonline.app.ui.screens.settings.SettingsScreen
import com.lifeonline.app.ui.theme.CartridgeBg
import com.lifeonline.app.ui.theme.CartridgeCard
import com.lifeonline.app.ui.theme.CartridgeGreen
import com.lifeonline.app.ui.theme.CartridgeTextMuted
import com.lifeonline.app.ui.theme.LifeOnlineTheme
import com.lifeonline.app.ui.theme.RetroAmber
import com.lifeonline.app.ui.theme.RetroCardBg
import com.lifeonline.app.ui.theme.RetroDarkBg
import com.lifeonline.app.ui.theme.RetroGreenPrimary
import com.lifeonline.app.ui.theme.RetroMuted

enum class NavTab(val title: String) {
    STATUS("状态"),
    JOURNAL("流水"),
    HISTORY("历程"),
    CONFIG("配置")
}

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // 权限回调处理，步数传感器已自适应
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 申请通知权限 (Android 13+) 与计步器运动识别权限 (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        // 启动常驻通知中心监控与后台保活前台服务
        try {
            KeepAliveMonitorService.start(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val repository = LifeOnlineApp.instance.repository

        setContent {
            val isCartridge by repository.isCartridgeThemeFlow.collectAsState()
            LaunchedEffect(Unit) {
                repository.ensureDefaultDataSeeded()
            }
            LifeOnlineTheme(isCartridge = isCartridge) {
                MainContent(repository, isCartridge)
            }
        }
    }
}

@Composable
fun MainContent(
    repository: com.lifeonline.app.data.repository.LifeOnlineRepository,
    isCartridge: Boolean
) {
    val userStats by repository.userStatsFlow.collectAsState(initial = null)
    var currentTab by remember { mutableStateOf(NavTab.STATUS) }
    var forceOnboarding by remember { mutableStateOf(false) }

    val isInitialized = userStats?.isInitialized == true && !forceOnboarding

    if (!isInitialized) {
        // 未初始化，进入角色创建/转生引导
        OnboardingScreen(
            repository = repository,
            onComplete = { forceOnboarding = false }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = if (isCartridge) CartridgeCard else RetroCardBg,
                    tonalElevation = 0.dp
                ) {
                    NavTab.values().forEach { tab ->
                        val selected = currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = {
                                val icon = when (tab) {
                                    NavTab.STATUS -> Icons.Default.Person
                                    NavTab.JOURNAL -> Icons.Default.Assignment
                                    NavTab.HISTORY -> Icons.Default.History
                                    NavTab.CONFIG -> Icons.Default.Settings
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isCartridge) CartridgeBg else RetroDarkBg,
                                selectedTextColor = if (isCartridge) CartridgeGreen else RetroGreenPrimary,
                                indicatorColor = if (isCartridge) CartridgeGreen else RetroGreenPrimary,
                                unselectedIconColor = if (isCartridge) CartridgeTextMuted else RetroMuted,
                                unselectedTextColor = if (isCartridge) CartridgeTextMuted else RetroMuted
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(if (isCartridge) CartridgeBg else RetroDarkBg)
            ) {
                when (currentTab) {
                    NavTab.STATUS -> DashboardScreen(repository)
                    NavTab.JOURNAL -> JournalScreen(repository)
                    NavTab.HISTORY -> HistoryScreen(repository)
                    NavTab.CONFIG -> SettingsScreen(
                        repository = repository,
                        onResetCharacter = { forceOnboarding = true }
                    )
                }
            }
        }
    }
}
