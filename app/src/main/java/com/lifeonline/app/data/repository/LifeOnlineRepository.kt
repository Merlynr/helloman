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
import com.lifeonline.app.data.model.Achievement
import com.lifeonline.app.data.model.AchievementCategory
import com.lifeonline.app.data.model.AchievementRarity
import com.lifeonline.app.data.model.SideQuest
import com.lifeonline.app.data.model.FullBlueprintTemplate
import com.lifeonline.app.data.model.LifeBlueprintTemplates
import com.lifeonline.app.data.model.SedentaryStatus
import com.lifeonline.app.data.model.SleepRecord
import com.lifeonline.app.data.remote.AiClient
import com.lifeonline.app.data.remote.DaemonClient
import com.lifeonline.app.data.sensor.RoutineAndHealthDetector
import com.lifeonline.app.data.sensor.SensorDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LifeOnlineRepository(
    private val context: Context,
    val database: AppDatabase
) {
    private val repoScope = CoroutineScope(Dispatchers.Default)

    val aiClient = AiClient(context)
    val daemonClient = DaemonClient(context)
    val sensorManager = SensorDataManager(context)
    val voiceRecorder = VoiceRecorder(context)
    val routineDetector = RoutineAndHealthDetector(
        context = context,
        database = database,
        scope = repoScope,
        onAchievementCheck = { stats, rawInput ->
            checkAndUnlockAchievements(stats, rawInput)
        }
    )

    init {
        // 实时连通步数与久坐检测
        repoScope.launch {
            sensorManager.currentSteps.collect { steps ->
                routineDetector.onStepsUpdated(steps)
            }
        }
    }

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
    val achievementsFlow: Flow<List<Achievement>> = database.achievementDao().getAllAchievementsFlow()

    // 作息与久坐 Flow
    val latestSleepRecordFlow: StateFlow<SleepRecord?> = routineDetector.latestSleepRecord
    val recent7DaysSleepFlow: Flow<List<SleepRecord>> = database.sleepRecordDao().getRecent7DaysSleepRecordsFlow()
    val sedentaryStatusFlow: StateFlow<SedentaryStatus> = routineDetector.sedentaryStatus

    // 三层时间嵌套体系：阶段战役与人生篇章 Flow
    val campaignsFlow: Flow<List<Campaign>> = database.campaignDao().getAllCampaignsFlow()
    val activeCampaignFlow: Flow<Campaign?> = database.campaignDao().getActiveCampaignFlow()
    val chaptersFlow: Flow<List<LifeChapter>> = database.lifeChapterDao().getAllChaptersFlow()
    val activeChapterFlow: Flow<LifeChapter?> = database.lifeChapterDao().getActiveChapterFlow()
    val sideQuestsFlow: Flow<List<SideQuest>> = database.sideQuestDao().getActiveSideQuestsFlow()

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

        ensureAchievementsSeeded()

        // 植入默认经典支线挑战
        if (database.sideQuestDao().getCount() == 0) {
            LifeBlueprintTemplates.allFullBlueprints.firstOrNull()?.sideQuests?.let {
                database.sideQuestDao().insertSideQuests(it)
            }
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
            bnd = result.rel.coerceIn(5, 100),
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
            bnd = (currentStats.bnd + d.rel).coerceIn(1, 999),
            updatedAt = System.currentTimeMillis()
        )
        database.userStatsDao().insertOrUpdate(updatedStats)

        // 检查成就解锁 (包含文本语义/关键词与属性里程碑检测)
        val unlockedTitles = checkAndUnlockAchievements(updatedStats, rawInput)
        val finalDialogue = if (unlockedTitles.isNotEmpty()) {
            "${result.dialogue}\n\n🏆 恭喜解锁新成就：【${unlockedTitles.joinToString("】【")}】！"
        } else {
            result.dialogue
        }

        // 动态更新 AI 称号 (若用户尚未自定义专属新称号，或取得新突破)
        if (unlockedTitles.isNotEmpty() || updatedStats.level > currentStats.level) {
            val candidateTitle = when {
                updatedStats.level >= 5 -> "渐入佳境的体制求索者"
                updatedStats.vitStreak >= 7 -> "晨光自律践行者"
                updatedStats.tecStreak >= 7 -> "真题题海征服者"
                rawInput.contains("考公") || rawInput.contains("真题") -> "稳扎稳打的备考者"
                else -> updatedStats.aiTitle.ifBlank { "稳步前行的现实玩家" }
            }
            database.userStatsDao().insertOrUpdate(updatedStats.copy(aiTitle = candidateTitle))
        }

        val dailyLog = DailyLog(
            dateString = todayStr,
            rawInput = rawInput,
            dialogue = finalDialogue,
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

        // 检查成就解锁
        checkAndUnlockAchievements(updatedStats)

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

    /**
     * 初始化预设成就列表（40枚，参考《高性价比人生指南》人生里程碑与小MO规范）
     */
    suspend fun ensureAchievementsSeeded() {
        val count = database.achievementDao().getTotalCount()
        if (count > 0) return

        val seedAchievements = listOf(
            // === 1.《高性价比人生指南》重大人生事件与办事 (隐藏/里程碑) ===
            Achievement(
                id = "life_pregnant",
                title = "新生命孕育中",
                description = "记录怀孕或备孕阶段",
                icon = "🤰",
                rarity = AchievementRarity.GOLD,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第27章 · 怀孕和生产",
                guideTip = "孕前及孕早期每日补充0.4mg叶酸可将神经管缺陷风险降低约七成；尽早办理母子健康手册。",
                triggerKeywords = "怀孕,怀上了,备孕,测出两条杠,有身孕,做产检",
                isHidden = true
            ),
            Achievement(
                id = "life_maternal_handbook",
                title = "母子健康手册",
                description = "办理母子健康手册或医院建档",
                icon = "📕",
                rarity = AchievementRarity.SILVER,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第27章 · 怀孕和生产",
                guideTip = "满6周即可去社区卫生服务中心建册，是后续产检、生育险报销与疫苗接种的通关文牒。",
                triggerKeywords = "母子手册,母子健康手册,建档,产检建册,社区建册",
                isHidden = true
            ),
            Achievement(
                id = "life_folic_acid",
                title = "0.4mg的关怀",
                description = "按指南科学补充叶酸或维生素",
                icon = "💊",
                rarity = AchievementRarity.BRONZE,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第27章 · 怀孕和生产",
                guideTip = "基础0.4mg叶酸几元钱一瓶与昂贵复合维生素预防效果无硬性差异，高性价比典范。",
                triggerKeywords = "叶酸,补叶酸,斯利安,复合维生素",
                isHidden = false
            ),
            Achievement(
                id = "life_epidural",
                title = "现代医学之光",
                description = "分娩时了解并选择硬膜外分娩镇痛（无痛）",
                icon = "🕊️",
                rarity = AchievementRarity.GOLD,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第27章 · 怀孕和生产",
                guideTip = "无痛分娩安全可靠，无需忍受无意义的剧痛，积极争取自身医疗权益。",
                triggerKeywords = "无痛,无痛分娩,打无痛,硬膜外镇痛",
                isHidden = true
            ),
            Achievement(
                id = "life_birth_cert",
                title = "第一张通行证",
                description = "办理新生儿《出生医学证明》或落户",
                icon = "👶",
                rarity = AchievementRarity.SILVER,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第27章 · 怀孕和生产",
                guideTip = "出院前核对好宝宝名字办理出生证，一个月内办理医保落户，享受新生儿待遇追溯。",
                triggerKeywords = "出生医学证明,出生证明,上户口,新生儿落户,办理落户",
                isHidden = true
            ),
            Achievement(
                id = "life_maternity_allowance",
                title = "国库回血令",
                description = "申领生育津贴或报销生育医疗费",
                icon = "💰",
                rarity = AchievementRarity.SILVER,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第27章 · 怀孕和生产",
                guideTip = "由用人单位在医保平台申报，生育津贴免征个人所得税，保障产假基本现金流。",
                triggerKeywords = "生育津贴,生育险,生育保险,报销产检,领生育津贴",
                isHidden = false
            ),
            Achievement(
                id = "life_marriage_cert",
                title = "双人成行",
                description = "在民政局登记领结婚证",
                icon = "💍",
                rarity = AchievementRarity.GOLD,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第10章 · 结婚与家庭",
                guideTip = "了解民法典关于夫妻共同财产与个人财产约定，建立坦诚财务沟通机制。",
                triggerKeywords = "结婚证,领证,领结婚证,民政局登记,正式结婚",
                isHidden = true
            ),
            Achievement(
                id = "life_pre_marriage_check",
                title = "坦诚相见",
                description = "婚前主动进行规范婚前医学检查",
                icon = "🩺",
                rarity = AchievementRarity.BRONZE,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第10章 · 结婚与家庭",
                guideTip = "多数地区妇幼保健院提供免费婚检，是对双方与下一代负责任的高性价比筛查。",
                triggerKeywords = "婚检,婚前体检,婚前检查",
                isHidden = false
            ),
            Achievement(
                id = "health_annual_checkup",
                title = "身体保养守则",
                description = "完成一次年度健康体检",
                icon = "🏥",
                rarity = AchievementRarity.SILVER,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第1章 · 身体保养",
                guideTip = "关注血常规、肝肾功、胸部低剂量CT与胃肠镜指标，预防远胜于治疗。",
                triggerKeywords = "体检,做体检,年度体检,全身体检,查血常规",
                isHidden = false
            ),
            Achievement(
                id = "health_dental",
                title = "牙口长青",
                description = "完成一次洁牙/洗牙或牙科检查",
                icon = "🦷",
                rarity = AchievementRarity.BRONZE,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第1章 · 身体保养",
                guideTip = "每年洁牙1~2次能彻底清理牙结石，预防牙周炎造成的不可逆骨吸收。",
                triggerKeywords = "洗牙,洁牙,补牙,看牙医,拔智齿",
                isHidden = false
            ),
            Achievement(
                id = "health_first_aid_cpr",
                title = "生命守护者",
                description = "学习心肺复苏(CPR)、海姆立克或AED使用",
                icon = "🫀",
                rarity = AchievementRarity.GOLD,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第2章 · 应急与急救",
                guideTip = "黄金4分钟内有效胸外按压与AED除颤，是挽救心源性猝死的关键技能。",
                triggerKeywords = "急救,心肺复苏,CPR,海姆立克,AED,急救培训",
                isHidden = false
            ),
            Achievement(
                id = "health_vaccine",
                title = "免疫防线",
                description = "接种流感、乙肝、HPV或破伤风等疫苗",
                icon = "💉",
                rarity = AchievementRarity.BRONZE,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第1章 · 身体保养",
                guideTip = "疫苗是医学史上成本收益比最高的健康投资手段之一。",
                triggerKeywords = "打疫苗,接种疫苗,流感疫苗,HPV,乙肝疫苗,狂犬疫苗",
                isHidden = false
            ),
            Achievement(
                id = "career_gke_exam",
                title = "题海筑城",
                description = "完成考公真题训练或模拟考试",
                icon = "🎯",
                rarity = AchievementRarity.BRONZE,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第5章 · 职业护城河",
                guideTip = "利用模块真题海与错题复盘，是突破体制内笔试行测极限的不二法门。",
                triggerKeywords = "考公,行测,申论,公安真题,刷题,模拟考,真题模考",
                isHidden = false
            ),
            Achievement(
                id = "career_gke_passed",
                title = "稳稳着陆",
                description = "考公/考编笔试进面或成功录用",
                icon = "🏆",
                rarity = AchievementRarity.SECRET,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第5章 · 职业护城河",
                guideTip = "体制内职业护城河构筑成功，开启人生阶段全新稳健主线！",
                triggerKeywords = "考公进面,考上公务员,考公上岸,录用公示,体检政审通过,成功上岸",
                isHidden = true
            ),
            Achievement(
                id = "finance_emergency_fund",
                title = "安全气囊",
                description = "建立并保持3~6个月生活开支应急备用金",
                icon = "🛡️",
                rarity = AchievementRarity.SILVER,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第3章 · 省钱与资产配置",
                guideTip = "备用金放在高流动性活期或低波理财中，应对失业与突发疾病风险。",
                triggerKeywords = "备用金,应急金,应急备用金,存满3个月,存满6个月",
                isHidden = false
            ),
            Achievement(
                id = "finance_social_security",
                title = "底线护盾",
                description = "查询并核验社保公积金缴纳流水",
                icon = "🏛️",
                rarity = AchievementRarity.BRONZE,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第9章 · 社保公积金",
                guideTip = "医保与养老金断缴超过3个月有等待期风险，用掌上12333随时自查。",
                triggerKeywords = "社保,公积金,五险一金,医保,查询社保,掌上12333",
                isHidden = false
            ),
            Achievement(
                id = "finance_zero_cost",
                title = "无消费禅师",
                description = "全天践行零冲动消费与极简饮食",
                icon = "🍃",
                rarity = AchievementRarity.BRONZE,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第3章 · 消费自律",
                guideTip = "砍掉拿铁因子与诱导消费，获得对现金流的绝对掌控力。",
                triggerKeywords = "0消费,零消费,没有花钱,没花钱,零支出,不买东西",
                isHidden = false
            ),
            Achievement(
                id = "finance_clear_debt",
                title = "斩断负债",
                description = "还清一笔高息网贷或信用卡账单",
                icon = "✂️",
                rarity = AchievementRarity.GOLD,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第3章 · 省钱与债务",
                guideTip = "优先清偿真实年化超过8%的借贷，远离消费主义陷阱。",
                triggerKeywords = "还清网贷,还清信用卡,还清贷款,账单还清,分期结清",
                isHidden = false
            ),
            Achievement(
                id = "travel_driver_license",
                title = "移动自由",
                description = "考取机动车驾驶证",
                icon = "🚗",
                rarity = AchievementRarity.BRONZE,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第4章 · 个人行动半径",
                guideTip = "熟练掌握防御性驾驶原则，扩大应急与家庭通勤半径。",
                triggerKeywords = "拿到驾照,考下驾照,科四过了,领驾照,考驾照",
                isHidden = false
            ),
            Achievement(
                id = "travel_passport",
                title = "世界通行证",
                description = "办理护照或港澳通行证签注",
                icon = "🛂",
                rarity = AchievementRarity.BRONZE,
                category = AchievementCategory.LIFE_GUIDE,
                guideChapter = "第4章 · 个人行动半径",
                guideTip = "办好证件随时拥有离境备用选择，拓展全球视野。",
                triggerKeywords = "办护照,护照办好,港澳通行证,签证办好",
                isHidden = false
            ),

            // === 2. 习惯打卡与 Streak 连击成就 ===
            Achievement("vit_streak_7", "晨光使者·连7", "连续7天完成气血早起/作息打卡", "🌅", AchievementRarity.BRONZE, AchievementCategory.HABIT_STREAK),
            Achievement("vit_streak_14", "晨光使者·连14", "连续14天完成气血早起/作息打卡", "☀️", AchievementRarity.SILVER, AchievementCategory.HABIT_STREAK),
            Achievement("vit_streak_30", "铁血晨跑者", "连续30天保持气血维自律", "🔥", AchievementRarity.GOLD, AchievementCategory.HABIT_STREAK),
            Achievement("tec_streak_7", "算力觉醒·连7", "连续7天完成专注学习/真题训练", "💻", AchievementRarity.BRONZE, AchievementCategory.HABIT_STREAK),
            Achievement("tec_streak_14", "折腾者·进阶", "连续14天完成算力与技能精进", "📚", AchievementRarity.SILVER, AchievementCategory.HABIT_STREAK),
            Achievement("san_streak_7", "韧者不屈", "连续7天完成心力抗压/冥想记录", "🧘", AchievementRarity.BRONZE, AchievementCategory.HABIT_STREAK),
            Achievement("gold_streak_7", "记账铁人·7天", "连续7天记录每日收支", "📒", AchievementRarity.BRONZE, AchievementCategory.HABIT_STREAK),
            Achievement("gold_total_100", "百笔账本", "累计记账超过100笔", "💰", AchievementRarity.SILVER, AchievementCategory.HABIT_STREAK),
            Achievement("bnd_chat_5", "超级连接者", "单日与5位以上良师益友深度交流", "🤝", AchievementRarity.BRONZE, AchievementCategory.HABIT_STREAK),

            // === 3. 成长里程碑 ===
            Achievement("level_5", "初窥门径", "角色综合等级达到 Lv.5", "⭐", AchievementRarity.SILVER, AchievementCategory.MILESTONE),
            Achievement("level_10", "百炼成钢", "角色综合等级达到 Lv.10", "🏆", AchievementRarity.GOLD, AchievementCategory.MILESTONE),
            Achievement("day_100", "百日修炼", "人生Online记录坚持达到100天", "🗓️", AchievementRarity.SILVER, AchievementCategory.MILESTONE),
            Achievement("day_365", "一年之约", "人生Online记录坚持达到365天", "🎖️", AchievementRarity.GOLD, AchievementCategory.MILESTONE),
            Achievement("task_total_50", "执行行者", "累计打卡完成50个现实任务", "✅", AchievementRarity.BRONZE, AchievementCategory.MILESTONE),
            Achievement("task_total_200", "现实任务大师", "累计打卡完成200个现实任务", "🎯", AchievementRarity.SILVER, AchievementCategory.MILESTONE),
            Achievement("first_settlement", "开服元老", "完成人生Online首次战报结算", "🎮", AchievementRarity.BRONZE, AchievementCategory.MILESTONE, isHidden = false, unlockedAt = System.currentTimeMillis()),
            Achievement("first_voice", "口若悬河", "首次使用 SenseVoice 语音拆解任务", "🎤", AchievementRarity.BRONZE, AchievementCategory.MILESTONE),

            // === 4. 隐藏彩蛋 ===
            Achievement("secret_night_owl", "肝帝之夜", "深夜23:00后依然完成自律复盘", "🌙", AchievementRarity.SECRET, AchievementCategory.SECRET, isHidden = true),
            Achievement("secret_cheapskate", "3分钱哲学", "记录单笔极低花费，不花冤枉钱", "🪙", AchievementRarity.SECRET, AchievementCategory.SECRET, isHidden = true)
        )

        // 补足至40枚锁定插槽（对齐小MO 9/40 风格）
        val placeholders = (seedAchievements.size + 1..40).map { i ->
            Achievement(
                id = "placeholder_$i",
                title = "??????",
                description = "尚未解锁的神秘人生决策成就",
                icon = "?",
                rarity = AchievementRarity.BRONZE,
                category = AchievementCategory.SECRET,
                isHidden = true
            )
        }

        database.achievementDao().insertAchievements(seedAchievements + placeholders)
    }

    /**
     * 检查并解锁符合条件的成就（支持属性阈值、Streak连击、以及《高性价比人生指南》文本关键词语义识别）
     * @return 刚刚解锁的成就标题列表
     */
    suspend fun checkAndUnlockAchievements(stats: UserStats, rawInput: String = ""): List<String> {
        val now = System.currentTimeMillis()
        val allAchievements = database.achievementDao().getAllAchievementsFlow().firstOrNull() ?: return emptyList()
        val newlyUnlocked = mutableListOf<String>()

        allAchievements.filter { it.unlockedAt == null && !it.id.startsWith("placeholder") }.forEach { ach ->
            var shouldUnlock = false

            // 1. 文本关键词语义命中 (针对高性价比人生指南重大事件)
            if (rawInput.isNotBlank() && ach.triggerKeywords.isNotBlank()) {
                val keywords = ach.triggerKeywords.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                if (keywords.any { rawInput.contains(it, ignoreCase = true) }) {
                    shouldUnlock = true
                }
            }

            // 2. 属性 / Streak 规则命中
            if (!shouldUnlock) {
                shouldUnlock = when (ach.id) {
                    "vit_streak_7" -> stats.vitStreak >= 7
                    "vit_streak_14" -> stats.vitStreak >= 14
                    "vit_streak_30" -> stats.vitStreak >= 30
                    "tec_streak_7" -> stats.tecStreak >= 7
                    "tec_streak_14" -> stats.tecStreak >= 14
                    "san_streak_7" -> stats.sanStreak >= 7
                    "gold_streak_7" -> stats.goldStreak >= 7
                    "level_5" -> stats.level >= 5
                    "level_10" -> stats.level >= 10
                    "first_settlement" -> true
                    "secret_night_owl" -> {
                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        hour >= 23 || hour < 4
                    }
                    else -> false
                }
            }

            if (shouldUnlock) {
                database.achievementDao().updateAchievement(ach.copy(unlockedAt = now))
                newlyUnlocked.add(ach.title)
            }
        }
        return newlyUnlocked
    }

    /**
     * 推进支线任务进度并奖励经验
     */
    suspend fun boostSideQuest(sideQuestId: Long, addAmount: Int = 1) {
        val quest = database.sideQuestDao().getSideQuestById(sideQuestId) ?: return
        val newProg = (quest.currentProgress + addAmount).coerceAtMost(quest.targetTotal)
        val isCompleted = newProg >= quest.targetTotal
        database.sideQuestDao().updateSideQuest(
            quest.copy(
                currentProgress = newProg,
                isCompleted = isCompleted
            )
        )

        // 奖励经验值
        val currentStats = getUserStats()
        val expGain = quest.expRewardPerStep * addAmount
        var newExp = currentStats.exp + expGain
        var newLevel = currentStats.level
        var newMaxExp = currentStats.maxExp
        while (newExp >= newMaxExp) {
            newExp -= newMaxExp
            newLevel += 1
            newMaxExp = (newMaxExp * 1.25f).toInt()
        }
        database.userStatsDao().insertOrUpdate(
            currentStats.copy(
                exp = newExp,
                level = newLevel,
                maxExp = newMaxExp,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun insertSideQuest(sideQuest: SideQuest): Long {
        return database.sideQuestDao().insertSideQuest(sideQuest)
    }

    suspend fun updateSideQuest(sideQuest: SideQuest) {
        database.sideQuestDao().updateSideQuest(sideQuest)
    }

    suspend fun deleteSideQuest(sideQuest: SideQuest) {
        database.sideQuestDao().deleteSideQuest(sideQuest)
    }

    /**
     * 一键套用全套人生蓝图模板 (篇章 + 战役 + 推荐支线)
     */
    suspend fun applyFullBlueprint(template: FullBlueprintTemplate) {
        // 更新或替换活跃篇章
        val currentChapter = database.lifeChapterDao().getActiveChapter()
        val chapterId = if (currentChapter != null) {
            database.lifeChapterDao().updateChapter(template.chapter.copy(id = currentChapter.id))
            currentChapter.id
        } else {
            database.lifeChapterDao().insertChapter(template.chapter)
        }

        // 更新或替换活跃战役
        val currentCampaign = database.campaignDao().getActiveCampaign()
        val campaignId = if (currentCampaign != null) {
            database.campaignDao().updateCampaign(template.campaign.copy(id = currentCampaign.id, chapterId = chapterId))
            currentCampaign.id
        } else {
            database.campaignDao().insertCampaign(template.campaign.copy(chapterId = chapterId))
        }

        // 插入配套支线挑战
        template.sideQuests.forEach { sq ->
            database.sideQuestDao().insertSideQuest(sq.copy(campaignId = campaignId))
        }
    }
}
