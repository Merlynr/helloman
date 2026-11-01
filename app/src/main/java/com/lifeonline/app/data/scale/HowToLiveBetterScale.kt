package com.lifeonline.app.data.scale

import com.lifeonline.app.data.model.AttributeDeltas
import com.lifeonline.app.data.model.CharacterGenesisResult
import com.lifeonline.app.data.model.DecomposedTask
import com.lifeonline.app.data.model.ScaleEvaluation
import com.lifeonline.app.data.model.SettlementResult
import com.lifeonline.app.data.model.TaskType
import com.lifeonline.app.data.model.TraitItem
import com.lifeonline.app.data.model.UserStats

/**
 * 依据 GitHub 开源项目《高性价比人生指南》(eternity4719/HowToLiveBetter)
 * 构建的多周期（近期/阶段性/长期）科学经验与成长评价标尺
 */
object HowToLiveBetterScale {

    val SYSTEM_BENCHMARK_PROMPT = """
你是一款硬核现实 RPG 游戏《人生 Online》的裁决导师。
你的经验结算与评价严格遵循开源项目《高性价比人生指南》(HowToLiveBetter) 的循证与高性价比哲学：
1. 【循证与证据等级】：拒绝鸡汤与虚妄玄学，只认可具备真实复利的行为。
2. 【成本与收益比】：衡量用户行为所消耗的时间、金钱和意志力成本，与其带来的短期即时收益与长期复利。
3. 【底线防御大于盲目进攻】：保护睡眠、防病防猝死、防金融债务陷阱、防法律风险的权重，远高于短期的盲目亢奋。
4. 【多周期三层标尺】：
   - 近期 (Daily 即时反馈)：关注睡眠时长与作息、低成本运动、健康饮食、心流工作、防久坐与防蓝光；
   - 阶段性 (Weekly/Monthly 习惯成型)：关注打卡连续性、月度储蓄率与现金流结余、系统性知识输出、体能进阶；
   - 长期 (Yearly 人生蓝图)：关注健康安全底线(年度体检/疫苗)、社会保障合规(医保社保防致贫)、核心职业技能护城河。

五维属性说明：
- TEC (智力/脑力): 专业技能、深度思考、认知提升、知识吸收
- VIT (气血/体魄): 睡眠质量、有氧与抗阻运动、心肺机能、饮食健康
- SAN (心力/精神): 情绪稳定、专注力、意志力韧性、自省与冥想
- GOLD (财力/资产): 储蓄、记账、避免非必要消费、长期投资
- REL (人脉/社交): 沟通协作、亲密关系维系、家庭支持、高质量社交圈

请以幽默但极富洞察力的 RPG 旁白语气给出结算与战报。输出必须严格为 JSON 格式。
""".trimIndent()

    /**
     * 构建角色初始化（转生经历生成）Prompt
     */
    fun buildCharacterGenesisPrompt(userInput: String): String {
        return """
用户录入了其现实生活中的真实经历、教育成就与生活习惯：
【用户背景】
$userInput

请根据《高性价比人生指南》的现实标尺，为该用户初始化其《人生 Online》角色面板。
计算初始等级 (建议 1 - 5 级)、五维初始属性点 (每项基准值 10~50 之间，总点数与其实际积累成正比)、专属头衔、初始评价与 2~3 个天生特质 (Trait)。

请严格输出以下 JSON 结构，不要包含任何多余文字：
{
  "title": "例如：初窥门径的工程师 / 稳健求索的学者",
  "level": 2,
  "tec": 35,
  "vit": 25,
  "san": 30,
  "gold": 20,
  "rel": 20,
  "commentary": "对用户过往经历的硬核点评，指出其优势维度与当前亟需防守的短板",
  "initialTraits": [
    {"name": "夜间灵感", "description": "深夜思考经验+10%，但气血消耗增加", "isDebuff": false},
    {"name": "亚健康预警", "description": "久坐运动不足，体能恢复速度-10%", "isDebuff": true}
  ]
}
""".trimIndent()
    }

    /**
     * 构建每日流水与任务结算 Prompt
     */
    fun buildSettlementPrompt(
        currentStats: UserStats,
        todayInput: String,
        stepCount: Int,
        completedTasks: List<String>
    ): String {
        return """
当前角色状态：
- 角色称号：${currentStats.title} (Lv.${currentStats.level})
- 五维属性：TEC=${currentStats.tec}, VIT=${currentStats.vit}, SAN=${currentStats.san}, GOLD=${currentStats.gold}, REL=${currentStats.bnd}

今日安卓端自动采集与完成情况：
- 传感器步数：$stepCount 步
- 已完成预定任务：${if (completedTasks.isEmpty()) "无" else completedTasks.joinToString("、")}

用户提交的今日极简流水记录：
【流水内容】
$todayInput

请根据《高性价比人生指南》三层标尺裁决：
1. 计算经验掉落 (expGained, 通常在 15 - 80 之间，若有严重摆烂或熬夜可极低甚至扣减惩罚)；
2. 计算五维增减 (attributeDeltas: tec, vit, san, gold, rel，整数，可正可负)；
3. 判定是否附带 BUFF 或 DEBUFF (各提供 0~2 个)；
4. 给出简短幽默且富含人生哲理的战报旁白 (dialogue)；
5. 分别从 近期、阶段性、长期 三个层次给出基于指南的精辟建议 (scaleEvaluation)。

请严格输出以下 JSON 结构，不要包含多余文本：
{
  "dialogue": "今日战报短评...",
  "expGained": 40,
  "attributeDeltas": {
    "tec": 10,
    "vit": 15,
    "san": 5,
    "gold": -20,
    "rel": 0
  },
  "buffsApplied": ["体能充沛 (明日气血增长加速)"],
  "debuffsApplied": [],
  "scaleEvaluation": {
    "shortTerm": "近期微习惯点评...",
    "midTerm": "阶段性习惯与现金流点评...",
    "longTerm": "长期复利与防御底线点评..."
  }
}
""".trimIndent()
    }

    /**
     * 离线兜底计算引擎（当无网络或未填写 API Key 时自动运行，保证 App 100% 随时可用）
     */
    fun calculateLocalFallbackSettlement(
        todayInput: String,
        stepCount: Int,
        completedTasks: List<String>
    ): SettlementResult {
        var tecDelta = 0
        var vitDelta = 0
        var sanDelta = 0
        var goldDelta = 0
        var relDelta = 0
        var exp = 20

        // 步数分析
        if (stepCount >= 10000) {
            vitDelta += 15
            exp += 25
        } else if (stepCount >= 6000) {
            vitDelta += 10
            exp += 15
        } else if (stepCount > 0) {
            vitDelta += 5
            exp += 5
        }

        // 任务完成奖励
        exp += completedTasks.size * 15

        val lower = todayInput.lowercase()

        // 智力相关
        if (lower.contains("代码") || lower.contains("学习") || lower.contains("读书") || lower.contains("写") || lower.contains("研究") || lower.contains("work")) {
            tecDelta += 12
            exp += 15
        }

        // 运动健康相关
        if (lower.contains("跑") || lower.contains("健身") || lower.contains("游泳") || lower.contains("骑") || lower.contains("锻炼")) {
            vitDelta += 15
            sanDelta += 5
            exp += 20
        }

        // 睡眠与休息
        if (lower.contains("早睡") || lower.contains("午休") || lower.contains("睡够") || lower.contains("冥想")) {
            vitDelta += 8
            sanDelta += 8
            exp += 10
        }

        // 熬夜负反馈
        if (lower.contains("熬夜") || lower.contains("失眠") || lower.contains("通宵")) {
            vitDelta -= 10
            sanDelta -= 8
        }

        // 消费与财务相关
        val moneyMatch = Regex("""(?:花了|支出|消费|买|饭|外卖).*?(\d+)""").find(todayInput)
        if (moneyMatch != null) {
            val amount = moneyMatch.groupValues[1].toIntOrNull() ?: 20
            goldDelta -= amount.coerceAtMost(100)
        }
        if (lower.contains("记账") || lower.contains("存") || lower.contains("理财")) {
            goldDelta += 10
            exp += 10
        }

        // 社交相关
        if (lower.contains("聚") || lower.contains("朋友") || lower.contains("家人") || lower.contains("聊天") || lower.contains("合作")) {
            relDelta += 10
            sanDelta += 5
            exp += 10
        }

        val buffs = mutableListOf<String>()
        val debuffs = mutableListOf<String>()

        if (vitDelta > 15) buffs.add("气血蓬勃: 恢复速度提升")
        if (sanDelta > 10) buffs.add("心如止水: 专注力加成")
        if (lower.contains("熬夜")) debuffs.add("睡眠欠债: 精神易疲倦")

        return SettlementResult(
            dialogue = "【离线高性价比结算】检测到今日有效行动！步数与打卡带来扎实成长，持续把日常过成有经验掉落的副本吧！",
            expGained = exp.coerceAtLeast(10),
            attributeDeltas = AttributeDeltas(tecDelta, vitDelta, sanDelta, goldDelta, relDelta),
            buffsApplied = buffs,
            debuffsApplied = debuffs,
            scaleEvaluation = ScaleEvaluation(
                shortTerm = "日常微习惯稳步推进，传感器步数与输入事件已折算经验。",
                midTerm = "保持规律行动，避免高意志力消耗的剧烈波动，遵循复利效应。",
                longTerm = "根据《高性价比人生指南》，持续筑牢健康与储蓄安全底线，是人生最高收益的防御投资。"
            )
        )
    }

    /**
     * 离线角色初始生成（当无网络、未填 API Key 或余额不足时自动触发，循证量化五维属性与特质）
     */
    fun createDefaultGenesis(userInput: String): CharacterGenesisResult {
        val lower = userInput.lowercase()
        val tecBase: Int
        val vitBase: Int
        val sanBase: Int
        val goldBase: Int
        val relBase: Int
        val title: String
        val commentary: String
        val traits = mutableListOf<TraitItem>()

        when {
            lower.any { it in "代码编程开发软件架构算法工黑客极客技术前端后端" } -> {
                title = "全栈极客·代码破壁者"
                tecBase = 38
                vitBase = 22
                sanBase = 26
                goldBase = 28
                relBase = 20
                commentary = "以代码为砖石，你在现实世界构筑逻辑秩序与技术护城河。《高性价比人生指南》建议在深耕硬核技能的同时，严密防护久坐风险与天时作息。"
                traits.add(TraitItem("逻辑心流", "深度脑力心流专注度+15%，技术攻坚效率提升", isDebuff = false))
                traits.add(TraitItem("久坐预警", "需重点关注日间活动量与颈椎保护", isDebuff = true))
            }
            lower.any { it in "考公体制上岸公考编制行测申论机关事业单位" } -> {
                title = "上岸破局·铁规行者"
                tecBase = 32
                vitBase = 25
                sanBase = 36
                goldBase = 24
                relBase = 26
                commentary = "志在体制破局与稳定扎根。结构化思维与抗压定力是你的核心武器，行稳致远，知识的厚积与心力的坚韧将助你跨越千军万马。"
                traits.add(TraitItem("申论定力", "长周期抗压耐力与结构化思维+15%", isDebuff = false))
                traits.add(TraitItem("阶段性内耗", "备考高压期易产生精神内耗，需呵护心力安全垫", isDebuff = true))
            }
            lower.any { it in "跑健身运动体魄马拉松体能自律锻炼游泳骑行" } -> {
                title = "淬体行者·自律引擎"
                tecBase = 24
                vitBase = 42
                sanBase = 32
                goldBase = 22
                relBase = 24
                commentary = "身体是承受命运之重的神殿。高水平的体能与多巴胺正循环，是你探索现实世界最高确定性复利的护城河底座。"
                traits.add(TraitItem("强健体魄", "抗衰老生命流速减缓，精力回复速度提升", isDebuff = false))
                traits.add(TraitItem("多巴胺正循环", "通过运动快速清除皮质醇，心流更平稳", isDebuff = false))
            }
            lower.any { it in "投资理财金融资产记账定投基金现金流股票" } -> {
                title = "稳健精算·防御操盘手"
                tecBase = 30
                vitBase = 24
                sanBase = 28
                goldBase = 42
                relBase = 22
                commentary = "防御先于进攻，复利胜过暴利。严谨的资产规划与6个月以上应急备用金，赋予你面对任何风浪的从容底气。"
                traits.add(TraitItem("复利直觉", "财富防御与风险厌恶本能，防意外致贫", isDebuff = false))
                traits.add(TraitItem("理性节制", "消费诱惑免疫力提升，强制储蓄率极佳", isDebuff = false))
            }
            lower.any { it in "自由设计创意插画艺术游民自媒体写作摄影" } -> {
                title = "灵感漫步·自由创作者"
                tecBase = 32
                vitBase = 24
                sanBase = 26
                goldBase = 24
                relBase = 36
                commentary = "以独特的视角洞察世界。保持敏锐与自由心境，深度社交与作品复利正在把你的日常雕刻成独一无二的作品。"
                traits.add(TraitItem("灵感心流", "跨界创造力与深度表达加成+15%", isDebuff = false))
                traits.add(TraitItem("现金流波动", "需建立充足的自由职业现金流缓冲池", isDebuff = true))
            }
            lower.any { it in "极简清修冥想静心读书考研学术研究生" } -> {
                title = "从容笃定·笃志求索者"
                tecBase = 35
                vitBase = 25
                sanBase = 35
                goldBase = 22
                relBase = 22
                commentary = "沉潜蓄势，知行合一。在浮躁的世界中坚守深度思考与极简主义，每一份积累都在为未来的跃迁注入底蕴。"
                traits.add(TraitItem("澄澈心智", "抗信息过载与抗干扰能力极强", isDebuff = false))
                traits.add(TraitItem("厚积薄发", "长期主义复利型成长路线", isDebuff = false))
            }
            else -> {
                title = "启程探索者·循证行者"
                tecBase = 26
                vitBase = 26
                sanBase = 26
                goldBase = 24
                relBase = 24
                commentary = "根据你的现实坐标，初始五维已均衡建立。真实人生的每一份认真记录，都将在此处化作可感知的力量。"
                traits.add(TraitItem("觉醒新手", "初入人生Online，经验获取效率+5%", isDebuff = false))
                traits.add(TraitItem("习惯探索中", "每日任务打卡将强化意志韧性", isDebuff = false))
            }
        }

        return CharacterGenesisResult(
            title = title,
            level = 1,
            tec = tecBase,
            vit = vitBase,
            san = sanBase,
            gold = goldBase,
            rel = relBase,
            commentary = commentary,
            initialTraits = traits
        )
    }

    /**
     * 构建 AI 任务拆解 Prompt
     */
    fun buildDecomposePrompt(userInput: String): String {
        return """
用户输入了一段打算做的事或语音随记：
"$userInput"

请根据《高性价比人生指南》的循证理念，将这段内容智能拆解为 1~6 个具体的、可执行的原子任务（Task Item）。
对每个任务：
1. title: 简明行动标题（如："完成服务器部署文档", "户外跑步 5 公里", "记账核算今日开销"）
2. taskType: 高频微习惯填 "DAILY"，单次/具体事项填 "SIDE_QUEST"
3. targetAttribute: 五维属性之一："TEC" (技能/脑力), "VIT" (体魄/健康), "SAN" (心力/自律), "GOLD" (财务/资产), "REL" (社交/人际)
4. expReward: 预计耗时与难度匹配的经验值 (10 ~ 30 之间)

请严格输出 JSON 数组，不要包含多余文字：
[
  {
    "title": "任务简明标题",
    "taskType": "DAILY",
    "targetAttribute": "VIT",
    "expReward": 15
  }
]
""".trimIndent()
    }

    /**
     * 离线确定性任务拆解兜底规则
     */
    fun createOfflineDecomposedTasks(input: String): List<DecomposedTask> {
        val parts = input.split(Regex("[，,。；;、!\\?！？\\n\\r]+|\\s+(?:然后|并且|顺便|接着|再)\\s*|(?:然后|并且|顺便|接着|再去|再做)"))
        val results = mutableListOf<DecomposedTask>()
        for (part in parts) {
            var s = part.trim()
            if (s.isEmpty()) continue
            s = s.replace(Regex("^(?:今天|打算|先|准备|下午|上午|晚上|中午|计划|要|去)+"), "").trim()
            if (s.length < 2) continue

            val lower = s.lowercase()
            val attr = when {
                lower.any { it in "代码编程开发写学读书系统研究文档算法部署工" } -> "TEC"
                lower.any { it in "跑健身走步运动拉伸游泳骑行球睡体检练" } -> "VIT"
                lower.any { it in "冥想静心专注复盘反思深呼吸早起" } -> "SAN"
                lower.any { it in "花买账记账支出理财省钱资产" } -> "GOLD"
                lower.any { it in "聊聚朋友家人电话沟通拜访聚餐" } -> "REL"
                else -> "VIT"
            }

            val isDaily = lower.contains("早睡") || lower.contains("步") || lower.contains("水") ||
                          lower.contains("拉伸") || lower.contains("打卡") || lower.contains("记账") ||
                          lower.contains("背单词")

            val type = if (isDaily) TaskType.DAILY else TaskType.SIDE_QUEST
            val exp = if (isDaily) 15 else 20

            results.add(DecomposedTask(
                title = s,
                taskType = type,
                targetAttribute = attr,
                expReward = exp,
                isSelected = true
            ))
        }

        if (results.isEmpty()) {
            results.add(DecomposedTask(
                title = input.trim(),
                taskType = TaskType.DAILY,
                targetAttribute = "VIT",
                expReward = 15,
                isSelected = true
            ))
        }
        return results
    }
}
