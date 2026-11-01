package com.lifeonline.app.data.repository

import android.content.Context
import com.lifeonline.app.data.audio.VoiceRecorder
import com.lifeonline.app.data.local.AppDatabase
import com.lifeonline.app.data.model.BuffItem
import com.lifeonline.app.data.model.Campaign
import com.lifeonline.app.data.model.CharacterGenesisResult
import com.lifeonline.app.data.model.DailyLog
import com.lifeonline.app.data.model.DecomposedTask
import com.lifeonline.app.data.model.LifeChapter
import com.lifeonline.app.data.model.SettlementResult
import com.lifeonline.app.data.model.TaskItem
import com.lifeonline.app.data.model.TaskType
import com.lifeonline.app.data.model.UserStats
import com.lifeonline.app.data.remote.AiClient
import com.lifeonline.app.data.remote.DaemonClient
import com.lifeonline.app.data.sensor.SensorDataManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
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
    val voiceRecorder = VoiceRecorder(context)

    // 主题风格配置 (默认开启用户喜爱的暖白复古卡带风格)
    private val themePrefs = context.getSharedPreferences("theme_settings", Context.MODE_PRIVATE)
    val isCartridgeThemeFlow = MutableStateFlow(themePrefs.getBoolean("is_cartridge_theme", true))

    fun setCartridgeTheme(enabled: Boolean) {
        isCartridgeThemeFlow.value = enabled
        themePrefs.edit().putBoolean("is_cartridge_theme", enabled).apply()
    }

    suspend fun transcribeAudio(file: File): String {
        return aiClient.transcribeAudio(file)
    }

    val userStatsFlow: Flow<UserStats?> = database.userStatsDao().getUserStatsFlow()
    val dailyLogsFlow: Flow<List<DailyLog>> = database.dailyLogDao().getAllLogsFlow()
    val tasksFlow: Flow<List<TaskItem>> = database.taskDao().getAllTasksFlow()
    val buffsFlow: Flow<List<BuffItem>> = database.buffDao().getAllBuffsFlow()
    val stepsFlow: StateFlow<Int> = sensorManager.currentSteps

    // 三层时间嵌套体系：阶段战役与人生篇章 Flow
    val campaignsFlow: Flow<List<Campaign>> = database.campaignDao().getAllCampaignsFlow()
    val activeCampaignFlow: Flow<Campaign?> = database.campaignDao().getActiveCampaignFlow()
    val chaptersFlow: Flow<List<LifeChapter>> = database.lifeChapterDao().getAllChaptersFlow()
    val activeChapterFlow: Flow<LifeChapter?> = database.lifeChapterDao().getActiveChapterFlow()

    suspend fun getUserStats(): UserStats {
        return database.userStatsDao().getUserStats() ?: UserStats()
    }

    /**
     * 初始化或确保默认篇章与考公战役已就绪
     */
    suspend fun ensureDefaultDataSeeded() {
        var chapter = database.lifeChapterDao().getActiveChapter()
        var chapterId = chapter?.id ?: 0L
        if (chapter == null) {
            chapterId = database.lifeChapterDao().insertChapter(
                LifeChapter(
                    title = "25 ~ 35 岁 · 立业与职业护城河黄金十年",
                    startAge = 25,
                    endAge = 35,
                    currentAge = 25,
                    daysPassed = 319,
                    description = "根据《高性价比人生指南》，25~35岁是个人核心技能定型、抗风险安全垫搭建的最关键窗口期。此时应防御优先于盲目盲干，全力攻坚体制或专业资质护城河。",
                    focusAttributes = "TEC,GOLD,VIT",
                    isActive = true
                )
            )
        }

        val existingCampaign = database.campaignDao().getActiveCampaign()
        if (existingCampaign == null) {
            database.campaignDao().insertCampaign(
                Campaign(
                    title = "2026 公务员考公上岸冲刺战役",
                    description = "以国家与省公务员考试为目标，构筑体制内职业护城河。通过每日真题、法条背诵与申论模考，累计实战能力。",
                    targetDate = "2026-12-01",
                    currentProgress = 504,
                    targetTotal = 1200,
                    unit = "题",
                    chapterId = chapterId,
                    isActive = true,
                    stageName = "阶段二：真题与模块题海冲刺"
                )
            )
        }
    }

    /**
     * 角色初始化（经历录入 -> AI 生成五维属性与天生特质）
     */
    suspend fun initializeCharacter(historyInput: String): CharacterGenesisResult {
        val result = if (aiClient.apiKey.isNotBlank()) {
            aiClient.generateCharacterGenesis(historyInput)
        } else {
            val daemonResult = daemonClient.genesis(historyInput)
            daemonResult ?: aiClient.generateCharacterGenesis(historyInput)
        }

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

        // 默认预置高性价比微任务
        val defaultTasks = listOf(
            TaskItem(
                title = "完成 50 道行测真题 (言语与资料)",
                description = "题感保持与错题总结",
                taskType = TaskType.DAILY,
                targetAttribute = "TEC",
                expReward = 25,
                campaignId = 1,
                campaignTag = "🎯 2026考公",
                campaignProgressAdd = 50
            ),
            TaskItem(
                title = "背诵公安专业知识核心考点 30分钟",
                description = "考点法条记忆强化",
                taskType = TaskType.DAILY,
                targetAttribute = "TEC",
                expReward = 20,
                campaignId = 1,
                campaignTag = "🎯 2026考公",
                campaignProgressAdd = 1
            ),
            TaskItem(
                title = "规律作息：7.5小时睡眠",
                description = "最廉价最高收益的免疫力防线",
                taskType = TaskType.DAILY,
                targetAttribute = "VIT",
                expReward = 20
            ),
            TaskItem(
                title = "轻量运动：快走或散步6000步",
                description = "促进血液循环与多巴胺分泌",
                taskType = TaskType.DAILY,
                targetAttribute = "VIT",
                expReward = 15
            ),
            TaskItem(
                title = "低成本生活：记录今日每一笔开支",
                description = "建立现金流掌控感，避免拿铁因子",
                taskType = TaskType.DAILY,
                targetAttribute = "GOLD",
                expReward = 15
            )
        )
        database.taskDao().insertTasks(defaultTasks)
        ensureDefaultDataSeeded()

        return result
    }

    /**
     * 极简流水账与每日战报结算（AI 多周期标尺裁决）
     */
    suspend fun executeSettlement(rawInput: String): SettlementResult {
        val currentStats = getUserStats()
        val stepCount = stepsFlow.value
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val allTasks = mutableListOf<String>()
        val result = if (aiClient.apiKey.isNotBlank()) {
            aiClient.generateDailySettlement(currentStats, rawInput, stepCount, allTasks)
        } else {
            val daemonResult = daemonClient.settle(todayStr, rawInput, stepCount, allTasks)
            daemonResult ?: aiClient.generateDailySettlement(currentStats, rawInput, stepCount, allTasks)
        }

        var newExp = currentStats.exp + result.expGained
        var newLevel = currentStats.level
        var newMaxExp = currentStats.maxExp

        while (newExp >= newMaxExp) {
            newExp -= newMaxExp
            newLevel += 1
            newMaxExp = (newMaxExp * 1.25f).toInt()
        }

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

    /**
     * 勾选任务打卡并自动联动战役进度！
     */
    suspend fun toggleTask(task: TaskItem) {
        val willBeCompleted = !task.isCompleted
        val updated = task.copy(isCompleted = willBeCompleted)
        database.taskDao().updateTask(updated)

        // 若有关联阶段战役，联动增减战役进度
        if (task.campaignId != null) {
            val delta = if (task.campaignProgressAdd > 0) task.campaignProgressAdd else 1
            val campaign = database.campaignDao().getCampaignById(task.campaignId)
            if (campaign != null) {
                val newProgress = if (willBeCompleted) {
                    (campaign.currentProgress + delta).coerceAtMost(campaign.targetTotal)
                } else {
                    (campaign.currentProgress - delta).coerceAtLeast(0)
                }
                database.campaignDao().updateCampaign(campaign.copy(currentProgress = newProgress))
            }
        }
    }

    suspend fun addTask(
        title: String,
        type: TaskType,
        attribute: String,
        exp: Int,
        campaignId: Long? = null,
        campaignTag: String = "",
        progressAdd: Int = 0
    ) {
        val task = TaskItem(
            title = title,
            taskType = type,
            targetAttribute = attribute,
            expReward = exp,
            campaignId = campaignId,
            campaignTag = campaignTag,
            campaignProgressAdd = progressAdd
        )
        database.taskDao().insertTask(task)
    }

    suspend fun decomposeInputToTasks(text: String): List<DecomposedTask> {
        return if (aiClient.apiKey.isNotBlank()) {
            aiClient.decomposeTasks(text)
        } else {
            val daemonResult = daemonClient.decomposeTasks(text)
            daemonResult ?: aiClient.decomposeTasks(text)
        }
    }

    suspend fun addBatchTasks(decomposedTasks: List<DecomposedTask>) {
        val activeCampaign = database.campaignDao().getActiveCampaignFlow().firstOrNull()
        val taskItems = decomposedTasks.filter { it.isSelected && it.title.isNotBlank() }.map {
            // 自动检测是否涉及考公/备考关键词
            val isExamRelated = it.title.contains("题") || it.title.contains("行测") ||
                                it.title.contains("申论") || it.title.contains("考公") ||
                                it.title.contains("公安") || it.title.contains("做题") ||
                                it.title.contains("真题")
            val cId = if (isExamRelated && activeCampaign != null) activeCampaign.id else null
            val cTag = if (isExamRelated) "🎯 2026考公" else ""
            val cAdd = if (isExamRelated) 50 else 0

            TaskItem(
                title = it.title,
                taskType = it.taskType,
                targetAttribute = it.targetAttribute,
                expReward = it.expReward,
                isCompleted = false,
                campaignId = cId,
                campaignTag = cTag,
                campaignProgressAdd = cAdd
            )
        }
        if (taskItems.isNotEmpty()) {
            database.taskDao().insertTasks(taskItems)
        }
    }

    suspend fun boostCampaign(campaignId: Long, addAmount: Int = 10) {
        val campaign = database.campaignDao().getCampaignById(campaignId)
        if (campaign != null) {
            val newProgress = (campaign.currentProgress + addAmount).coerceAtMost(campaign.targetTotal)
            database.campaignDao().updateCampaign(campaign.copy(currentProgress = newProgress))
        }
    }

    suspend fun updateCampaign(campaign: Campaign) {
        database.campaignDao().updateCampaign(campaign)
    }

    suspend fun insertCampaign(campaign: Campaign): Long {
        return database.campaignDao().insertCampaign(campaign)
    }

    suspend fun updateChapter(chapter: LifeChapter) {
        database.lifeChapterDao().updateChapter(chapter)
    }

    suspend fun deleteTask(task: TaskItem) {
        database.taskDao().deleteTask(task)
    }

    suspend fun deleteBuff(buff: BuffItem) {
        database.buffDao().deleteBuff(buff)
    }
}
