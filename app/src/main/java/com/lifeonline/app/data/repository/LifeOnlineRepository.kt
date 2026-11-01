package com.lifeonline.app.data.repository

import android.content.Context
import com.lifeonline.app.data.local.AppDatabase
import com.lifeonline.app.data.model.BuffItem
import com.lifeonline.app.data.model.CharacterGenesisResult
import com.lifeonline.app.data.model.DailyLog
import com.lifeonline.app.data.model.SettlementResult
import com.lifeonline.app.data.model.TaskItem
import com.lifeonline.app.data.model.TaskType
import com.lifeonline.app.data.model.UserStats
import com.lifeonline.app.data.remote.AiClient
import com.lifeonline.app.data.sensor.SensorDataManager
import com.lifeonline.app.data.remote.DaemonClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LifeOnlineRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    val aiClient = AiClient(context)
    val daemonClient = DaemonClient(context)
    val sensorManager = SensorDataManager(context)

    val userStatsFlow: Flow<UserStats?> = database.userStatsDao().getUserStatsFlow()
    val dailyLogsFlow: Flow<List<DailyLog>> = database.dailyLogDao().getAllLogsFlow()
    val tasksFlow: Flow<List<TaskItem>> = database.taskDao().getAllTasksFlow()
    val buffsFlow: Flow<List<BuffItem>> = database.buffDao().getAllBuffsFlow()
    val stepsFlow: StateFlow<Int> = sensorManager.currentSteps

    suspend fun getUserStats(): UserStats {
        return database.userStatsDao().getUserStats() ?: UserStats()
    }

    /**
     * 角色初始化（经历录入 -> AI 生成五维属性与天生特质）
     */
    suspend fun initializeCharacter(historyInput: String): CharacterGenesisResult {
        // 优先通过电脑端 Agent Daemon 计算并持久化，若未连接则优雅降级为本地规则
        val daemonResult = daemonClient.genesis(historyInput)
        val result = daemonResult ?: aiClient.generateCharacterGenesis(historyInput)

        val newStats = UserStats(
            id = 1,
            name = "玩家",
            title = result.title,
            level = result.level.coerceAtLeast(1),
            exp = 0,
            maxExp = 100 * result.level.coerceAtLeast(1),
            tec = result.tec.coerceIn(5, 100),
            vit = result.vit.coerceIn(5, 100),
            san = result.san.coerceIn(5, 100),
            gold = result.gold.coerceIn(5, 100),
            rel = result.rel.coerceIn(5, 100),
            isInitialized = true,
            backgroundStory = historyInput,
            updatedAt = System.currentTimeMillis()
        )
        database.userStatsDao().insertOrUpdate(newStats)

        // 插入天生特质为 Buff/Debuff
        val buffItems = result.initialTraits.map {
            BuffItem(
                name = it.name,
                description = it.description,
                isDebuff = it.isDebuff,
                remainingDays = 999
            )
        }
        database.buffDao().insertBuffs(buffItems)

        // 默认预置一些高性价比人生指南日常微任务
        val defaultTasks = listOf(
            TaskItem(title = "规律作息：7.5小时睡眠", description = "最廉价最高收益的免疫力防线", taskType = TaskType.DAILY, targetAttribute = "VIT", expReward = 20),
            TaskItem(title = "轻量运动：快走或散步6000步", description = "促进血液循环与多巴胺分泌", taskType = TaskType.DAILY, targetAttribute = "VIT", expReward = 15),
            TaskItem(title = "深度学习：心流专注1小时", description = "构筑个人专业壁垒", taskType = TaskType.DAILY, targetAttribute = "TEC", expReward = 20),
            TaskItem(title = "低成本生活：记录今日每一笔开支", description = "建立现金流掌控感，避免拿铁因子", taskType = TaskType.DAILY, targetAttribute = "GOLD", expReward = 15)
        )
        database.taskDao().insertTasks(defaultTasks)

        return result
    }

    /**
     * 极简流水账与每日战报结算（AI 多周期标尺裁决）
     */
    suspend fun executeSettlement(rawInput: String): SettlementResult {
        val currentStats = getUserStats()
        val stepCount = stepsFlow.value
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 获取已完成任务
        val allTasks = mutableListOf<String>()
        // 优先通过电脑端 Agent Daemon 结算并沉淀长程知识库档案
        val daemonResult = daemonClient.settle(todayStr, rawInput, stepCount, allTasks)
        val result = daemonResult ?: aiClient.generateDailySettlement(currentStats, rawInput, stepCount, allTasks)

        // 经验累加与升级判定
        var newExp = currentStats.exp + result.expGained
        var newLevel = currentStats.level
        var newMaxExp = currentStats.maxExp

        while (newExp >= newMaxExp) {
            newExp -= newMaxExp
            newLevel += 1
            newMaxExp = (newMaxExp * 1.25f).toInt()
        }

        // 属性更新 (限制在 1 - 999)
        val d = result.attributeDeltas
        val updatedStats = currentStats.copy(
            level = newLevel,
            exp = newExp.coerceAtLeast(0),
            maxExp = newMaxExp,
            tec = (currentStats.tec + d.tec).coerceIn(1, 999),
            vit = (currentStats.vit + d.vit).coerceIn(1, 999),
            san = (currentStats.san + d.san).coerceIn(1, 999),
            gold = (currentStats.gold + d.gold).coerceIn(1, 999),
            rel = (currentStats.rel + d.rel).coerceIn(1, 999),
            updatedAt = System.currentTimeMillis()
        )
        database.userStatsDao().insertOrUpdate(updatedStats)

        // 保存流水战报记录
        val dailyLog = DailyLog(
            dateString = todayStr,
            rawInput = rawInput,
            dialogue = result.dialogue,
            expGained = result.expGained,
            tecDelta = d.tec,
            vitDelta = d.vit,
            sanDelta = d.san,
            goldDelta = d.gold,
            relDelta = d.rel,
            stepCount = stepCount,
            shortTermEval = result.scaleEvaluation.shortTerm,
            midTermEval = result.scaleEvaluation.midTerm,
            longTermEval = result.scaleEvaluation.longTerm
        )
        database.dailyLogDao().insertLog(dailyLog)

        // 插入新的 Buff / Debuff
        val newBuffs = mutableListOf<BuffItem>()
        result.buffsApplied.forEach {
            newBuffs.add(BuffItem(name = it, description = "今日结算获得增益", isDebuff = false, remainingDays = 2))
        }
        result.debuffsApplied.forEach {
            newBuffs.add(BuffItem(name = it, description = "今日结算获得负面状态", isDebuff = true, remainingDays = 1))
        }
        if (newBuffs.isNotEmpty()) {
            database.buffDao().insertBuffs(newBuffs)
        }

        return result
    }

    suspend fun toggleTask(task: TaskItem) {
        val updated = task.copy(isCompleted = !task.isCompleted)
        database.taskDao().updateTask(updated)
    }

    suspend fun addTask(title: String, type: TaskType, attribute: String, exp: Int) {
        val task = TaskItem(
            title = title,
            taskType = type,
            targetAttribute = attribute,
            expReward = exp
        )
        database.taskDao().insertTask(task)
    }

    suspend fun deleteTask(task: TaskItem) {
        database.taskDao().deleteTask(task)
    }

    suspend fun deleteBuff(buff: BuffItem) {
        database.buffDao().deleteBuff(buff)
    }
}
