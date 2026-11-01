package com.lifeonline.app.data.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.lifeonline.app.data.local.AppDatabase
import com.lifeonline.app.data.model.SedentaryStatus
import com.lifeonline.app.data.model.SleepRecord
import com.lifeonline.app.data.model.UserStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 作息、熬夜与日间久坐智能检测器
 *
 * 核心标准：
 * 1. 22:30 之后入睡即为熬夜 (Late night)
 * 2. 07:00 前后起床为正常作息 (Early bird / Normal)
 * 3. 09:00 ~ 18:00 日间连续 90 分钟步数增量 < 30 步判定为久坐
 * 4. 久坐后走动 100 步自动解除
 */
class RoutineAndHealthDetector(
    private val context: Context,
    private val database: AppDatabase,
    private val scope: CoroutineScope,
    private val onAchievementCheck: (suspend (UserStats, String) -> Unit)? = null
) {
    private val prefs = context.getSharedPreferences("routine_detector_prefs", Context.MODE_PRIVATE)

    private val _latestSleepRecord = MutableStateFlow<SleepRecord?>(null)
    val latestSleepRecord: StateFlow<SleepRecord?> = _latestSleepRecord.asStateFlow()

    private val _sedentaryStatus = MutableStateFlow(SedentaryStatus())
    val sedentaryStatus: StateFlow<SedentaryStatus> = _sedentaryStatus.asStateFlow()

    private var lastActiveTimeMillis: Long = System.currentTimeMillis()
    private var lastRecordedSteps: Int = 0
    private var sedentaryStartSteps: Int = 0

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> handleScreenOff()
                Intent.ACTION_USER_PRESENT, Intent.ACTION_SCREEN_ON -> handleScreenOnOrUnlock()
                Intent.ACTION_POWER_CONNECTED -> handlePowerConnected()
            }
        }
    }

    init {
        registerReceivers()
        loadPersistedState()
    }

    private fun registerReceivers() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        context.registerReceiver(receiver, filter)
    }

    fun unregister() {
        try {
            context.unregisterReceiver(receiver)
        } catch (_: Exception) {}
    }

    private fun loadPersistedState() {
        scope.launch(Dispatchers.IO) {
            val todayStr = getTodayDate()
            val existing = database.sleepRecordDao().getSleepRecordByDate(todayStr)
                ?: database.sleepRecordDao().getLatestSleepRecord()
            _latestSleepRecord.value = existing
        }
    }

    private fun handleScreenOff() {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        // 晚上 20:00 到次日凌晨 05:00 之间的灭屏，视为可能入睡候选点
        if (hour >= 20 || hour < 5) {
            val existingCandidate = prefs.getLong("candidate_sleep_time", 0L)
            // 若之前未记录，或距离上一次记录已过去40分钟，更新为当前灭屏时刻
            if (existingCandidate == 0L || (now - existingCandidate > 40 * 60 * 1000L)) {
                prefs.edit().putLong("candidate_sleep_time", now).apply()
            }
        }
    }

    private fun handlePowerConnected() {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        // 深夜插上充电线，极大概率是临睡前放置手机
        if (hour >= 21 || hour < 4) {
            prefs.edit().putLong("night_power_connected_time", now).apply()
        }
    }

    private fun handleScreenOnOrUnlock() {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        // 重置久坐活动时间（开屏代表玩家有活动）
        checkSedentaryActivity(0)

        // 仅在清晨 05:00 ~ 12:00 期间触发晨间苏醒分析
        if (hour in 5..12) {
            val candidateSleepTime = prefs.getLong("candidate_sleep_time", 0L)
            if (candidateSleepTime > 0L) {
                val elapsedDuration = now - candidateSleepTime
                // 睡眠时长至少大于 3.5 小时才计为一次有效长觉
                if (elapsedDuration >= 3.5 * 3600 * 1000L) {
                    processWakeUpSettlement(candidateSleepTime, now, hour, minute)
                    // 重置候选时间，防止重复触发
                    prefs.edit().remove("candidate_sleep_time").apply()
                }
            }
        }
    }

    /**
     * 计算并落盘昨夜睡眠记录
     */
    fun processWakeUpSettlement(
        sleepTimeMillis: Long,
        wakeTimeMillis: Long,
        wakeHour: Int,
        wakeMinute: Int
    ) {
        val durationMinutes = ((wakeTimeMillis - sleepTimeMillis) / (60 * 1000)).toInt()
        val sleepCal = Calendar.getInstance().apply { timeInMillis = sleepTimeMillis }
        val sleepHour = sleepCal.get(Calendar.HOUR_OF_DAY)
        val sleepMinute = sleepCal.get(Calendar.MINUTE)

        // 判定规则：晚于 22:30 入睡即为熬夜
        val isLateNight = when {
            sleepHour > 22 -> true
            sleepHour == 22 && sleepMinute > 30 -> true
            sleepHour in 0..4 -> true // 凌晨入睡属于严重熬夜
            else -> false
        }

        // 早起判定：07:30 前起床且睡足 6.5 小时
        val isEarlyBird = (wakeHour < 7 || (wakeHour == 7 && wakeMinute <= 30)) && durationMinutes >= 390

        val sleepScore: Int
        val statusTitle: String
        val description: String
        val vitDelta: Int
        val hpDelta: Int

        val sleepTimeFmt = String.format(Locale.getDefault(), "%02d:%02d", sleepHour, sleepMinute)
        val wakeTimeFmt = String.format(Locale.getDefault(), "%02d:%02d", wakeHour, wakeMinute)
        val durationFmt = String.format(Locale.getDefault(), "%.1f", durationMinutes / 60.0f)

        if (isLateNight) {
            statusTitle = "熬夜透支·气血亏虚"
            sleepScore = (50 + (durationMinutes / 15)).coerceIn(45, 68)
            vitDelta = -15
            hpDelta = -10
            description = "昨夜于 $sleepTimeFmt 入睡 (晚于22:30标尺)，睡眠时长 ${durationFmt}h。气血流速透支，经络未得完全充养，建议今晚早歇归元！"
        } else if (isEarlyBird) {
            statusTitle = "气血充盈·晨光润养"
            sleepScore = (85 + (durationMinutes / 30)).coerceIn(88, 100)
            vitDelta = +15
            hpDelta = +10
            description = "遵循天时！于 $sleepTimeFmt 安歇并在清晨 $wakeTimeFmt 醒来，睡眠时长 ${durationFmt}h。体魄活力回满，今日专注力与免疫安全垫大幅提升！"
        } else {
            statusTitle = "作息稳健·精力重置"
            sleepScore = 80
            vitDelta = +10
            hpDelta = +5
            description = "于 $sleepTimeFmt 入睡，$wakeTimeFmt 醒来，睡眠时长 ${durationFmt}h。节奏稳健，保持生活秩序感。"
        }

        val record = SleepRecord(
            dateString = getTodayDate(),
            sleepTimeMillis = sleepTimeMillis,
            wakeTimeMillis = wakeTimeMillis,
            durationMinutes = durationMinutes,
            isLateNight = isLateNight,
            isEarlyBird = isEarlyBird,
            sleepScore = sleepScore,
            statusTitle = statusTitle,
            description = description,
            vitDelta = vitDelta,
            hpDelta = hpDelta,
            isDismissed = false
        )

        scope.launch(Dispatchers.IO) {
            database.sleepRecordDao().insertSleepRecord(record)
            _latestSleepRecord.value = record

            // 更新用户属性
            val stats = database.userStatsDao().getUserStats() ?: UserStats()
            val updatedStats = stats.copy(
                vit = (stats.vit + vitDelta).coerceIn(0, 100),
                hp = (stats.hp + hpDelta).coerceIn(0, 100)
            )
            database.userStatsDao().insertOrUpdate(updatedStats)

            // 若触发熬夜，检查并点亮夜猫子相关成就
            if (isLateNight && (sleepHour in 1..4)) {
                onAchievementCheck?.invoke(updatedStats, "熬夜 凌晨")
            }
        }
    }

    /**
     * 步数更新钩子：检测日间连续久坐
     */
    fun onStepsUpdated(totalSteps: Int) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        // 仅在日间工作学习时间 09:00 ~ 18:00 进行久坐分析
        if (hour in 9..18) {
            val stepDelta = totalSteps - lastRecordedSteps
            if (stepDelta >= 30) {
                // 有效走动，刷新活动时刻
                lastRecordedSteps = totalSteps
                lastActiveTimeMillis = now

                // 如果当前处于久坐，累加解除步数
                val currentSedentary = _sedentaryStatus.value
                if (currentSedentary.isSedentary) {
                    val walked = currentSedentary.stepsWalkedSinceSedentary + stepDelta
                    if (walked >= currentSedentary.targetStepsToClear) {
                        // 达成 100 步走动，成功解除久坐状态
                        _sedentaryStatus.value = SedentaryStatus(
                            isSedentary = false,
                            inactiveMinutes = 0,
                            stepsWalkedSinceSedentary = 0
                        )
                    } else {
                        _sedentaryStatus.value = currentSedentary.copy(
                            stepsWalkedSinceSedentary = walked
                        )
                    }
                }
            } else {
                checkSedentaryActivity(totalSteps)
            }
        }
    }

    private fun checkSedentaryActivity(currentSteps: Int) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        if (hour in 9..18) {
            val inactiveMillis = now - lastActiveTimeMillis
            val inactiveMinutes = (inactiveMillis / (60 * 1000)).toInt()

            if (inactiveMinutes >= 90) {
                // 超过 90 分钟未走动，标记为久坐异常
                val current = _sedentaryStatus.value
                if (!current.isSedentary) {
                    sedentaryStartSteps = currentSteps
                    _sedentaryStatus.value = SedentaryStatus(
                        isSedentary = true,
                        inactiveMinutes = inactiveMinutes,
                        stepsWalkedSinceSedentary = 0,
                        targetStepsToClear = 100
                    )
                } else {
                    _sedentaryStatus.value = current.copy(
                        inactiveMinutes = inactiveMinutes
                    )
                }
            }
        }
    }

    /**
     * 确认并关闭晨间报告弹窗
     */
    fun dismissLatestSleepRecord() {
        val record = _latestSleepRecord.value ?: return
        scope.launch(Dispatchers.IO) {
            database.sleepRecordDao().markDismissed(record.id)
            _latestSleepRecord.value = record.copy(isDismissed = true)
        }
    }

    /**
     * 测试/模拟工具：立即注入一条作息模拟报告（方便玩家体验与测试）
     */
    fun mockSleepRecordForTest(isLate: Boolean) {
        val now = System.currentTimeMillis()
        val sleepMillis = if (isLate) {
            now - (5 * 3600 * 1000L + 20 * 60 * 1000L) // 睡了5.3小时，昨天 01:15 睡
        } else {
            now - (8 * 3600 * 1000L + 15 * 60 * 1000L) // 睡了8.2小时，昨天 22:15 睡
        }
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        processWakeUpSettlement(
            sleepTimeMillis = sleepMillis,
            wakeTimeMillis = now,
            wakeHour = cal.get(Calendar.HOUR_OF_DAY),
            wakeMinute = cal.get(Calendar.MINUTE)
        )
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
