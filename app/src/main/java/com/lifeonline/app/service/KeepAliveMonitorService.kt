package com.lifeonline.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lifeonline.app.LifeOnlineApp
import com.lifeonline.app.MainActivity
import com.lifeonline.app.R
import com.lifeonline.app.data.model.Campaign
import com.lifeonline.app.data.model.SedentaryStatus
import com.lifeonline.app.data.model.SleepRecord
import com.lifeonline.app.data.model.TaskItem
import com.lifeonline.app.data.model.UserStats
import com.lifeonline.app.data.scale.LifeVelocityCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * 人生ONLINE · 状态与进度常驻通知监控中心 (Foreground Keep-Alive Service)
 * 作用：
 * 1. 后台长效保活，防止系统杀死传感器计步与状态监听服务
 * 2. 在系统通知栏以 HUD 卡片形式实时汇报当前现实进度、打卡统计、作息评估与久坐提醒
 */
class KeepAliveMonitorService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var currentStats: UserStats? = null
    private var currentSteps: Int = 0
    private var currentCampaign: Campaign? = null
    private var currentTasks: List<TaskItem> = emptyList()
    private var currentSleepRecord: SleepRecord? = null
    private var currentSedentaryStatus: SedentaryStatus = SedentaryStatus()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val initialNotification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH or ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            }
            startForeground(NOTIFICATION_ID, initialNotification, serviceType)
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        observeData()
    }

    private fun observeData() {
        val repo = LifeOnlineApp.instance.repository

        // 监听核心游戏属性与战役任务
        serviceScope.launch {
            combine(
                repo.userStatsFlow,
                repo.stepsFlow,
                repo.activeCampaignFlow,
                repo.tasksFlow
            ) { stats, steps, campaign, tasks ->
                currentStats = stats
                currentSteps = steps
                currentCampaign = campaign
                currentTasks = tasks
                updateNotification()
            }.collect()
        }

        // 监听作息与睡眠分析
        serviceScope.launch {
            repo.latestSleepRecordFlow.collect { sleepRecord ->
                currentSleepRecord = sleepRecord
                updateNotification()
            }
        }

        // 监听日间久坐状态
        serviceScope.launch {
            repo.sedentaryStatusFlow.collect { sedentary ->
                currentSedentaryStatus = sedentary
                updateNotification()
            }
        }
    }

    private fun buildNotification(): Notification {
        val stats = currentStats ?: UserStats()
        val steps = currentSteps
        val campaign = currentCampaign
        val tasks = currentTasks
        val completedTasks = tasks.count { it.isCompleted }
        val totalTasks = tasks.size
        val sedentary = currentSedentaryStatus
        val sleep = currentSleepRecord

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val titlePrefix = if (sedentary.isSedentary) "⚠️[久坐预警 ${sedentary.inactiveMinutes}m] " else "🎮 "
        val title = "${titlePrefix}人生ONLINE · Lv.${stats.level} ${stats.aiTitle.ifBlank { stats.title }}"

        val campaignSummary = if (campaign != null) {
            "🎯 ${(campaign.progressPercent * 100).toInt()}%"
        } else {
            "🎯 战役中"
        }
        val sedentaryTag = if (sedentary.isSedentary) "⚠️已坐${sedentary.inactiveMinutes}m" else "●状态良"
        val subText = "$campaignSummary | 🏃${steps}步 | $sedentaryTag | 任务: $completedTasks/$totalTasks"

        val bigText = buildString {
            appendLine("玩家：${stats.name} (Lv.${stats.level}) · 称号: ${stats.aiTitle.ifBlank { stats.title }}")
            appendLine("🏃 实时步数：$steps 步 · VIT回血中 (传感器保活)")
            
            // 久坐状态警报
            if (sedentary.isSedentary) {
                appendLine("⚠️ 久坐警报：已连续静止伏案 ${sedentary.inactiveMinutes} 分钟！请起身走动 100 步解除 (已走 ${sedentary.stepsWalkedSinceSedentary}/${sedentary.targetStepsToClear} 步)")
            } else {
                appendLine("🧘 身体状态：日间活力充沛 · 经络通畅")
            }

            // 作息状态
            if (sleep != null) {
                val sleepTag = if (sleep.isLateNight) "⚠️ 熬夜 (${sleep.statusTitle})" else "✨ 良好 (${sleep.statusTitle})"
                val durationHrs = String.format(java.util.Locale.getDefault(), "%.1f", sleep.durationMinutes / 60.0f)
                appendLine("🌙 作息研判：$sleepTag · 睡眠 ${durationHrs}h (健康分:${sleep.sleepScore})")
            }

            // 宏观生命流速 (综合身体属性、步数、睡眠与久坐)
            val velocity = LifeVelocityCalculator.calculate(stats, steps, sleep, sedentary)
            appendLine("⚡ 生命流速：${velocity.factorText} · ${velocity.statusTag}")

            if (campaign != null) {
                appendLine("🎯 阶段战役：${campaign.title} [${campaign.currentProgress}/${campaign.targetTotal}${campaign.unit}] (${(campaign.progressPercent * 100).toInt()}%)")
            }
            appendLine("📝 今日任务：已完成 $completedTasks / $totalTasks 项现实挑战")
            append("⚡ 系统状态：人生Online守护进程正常运行中")
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(subText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(if (sedentary.isSedentary) NotificationCompat.PRIORITY_DEFAULT else NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun updateNotification() {
        val notification = buildNotification()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "人生ONLINE 状态与现实进度监控",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "常驻显示当前角色状态、计步数据与战役进度，维持后台保活"
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "life_online_keep_alive_monitor"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, KeepAliveMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, KeepAliveMonitorService::class.java)
            context.stopService(intent)
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
