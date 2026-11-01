package com.lifeonline.app.data.model

/**
 * 全套人生蓝图预设模板 (包含篇章 + 配套主线战役 + 建议支线)
 */
data class FullBlueprintTemplate(
    val id: String,
    val name: String,
    val tag: String,
    val description: String,
    val chapter: LifeChapter,
    val campaign: Campaign,
    val sideQuests: List<SideQuest>
)

object LifeBlueprintTemplates {

    val allFullBlueprints: List<FullBlueprintTemplate> = listOf(
        FullBlueprintTemplate(
            id = "gke_defense",
            name = "体制考公攻坚 · 护城河冲刺",
            tag = "25~35岁 · 稳健避险",
            description = "根据《高性价比人生指南》，在经济周期波动中全力构筑体制内防守反击壁垒。以真题海、模块复盘为核心推进。",
            chapter = LifeChapter(
                title = "25 ~ 35 岁 · 立业与职业护城河黄金十年",
                startAge = 25,
                endAge = 35,
                currentAge = 25,
                daysPassed = 319,
                description = "25~35岁是个人核心技能定型、抗风险安全垫搭建的最关键窗口期。此时防御优先于盲目盲干，全力攻坚体制或专业资质护城河。",
                focusAttributes = "TEC,GOLD,VIT",
                isActive = true
            ),
            campaign = Campaign(
                title = "2026 公务员考公上岸冲刺战役",
                description = "以国家与省公务员考试为目标，构筑体制内职业护城河。通过每日真题、法条背诵与申论模考，累计实战能力。",
                targetDate = "2026-12-01",
                currentProgress = 504,
                targetTotal = 1200,
                unit = "题",
                chapterId = 1,
                isActive = true,
                stageName = "阶段二：真题与模块题海冲刺"
            ),
            sideQuests = listOf(
                SideQuest(
                    title = "情绪自救手册 (认知疗法研读)",
                    description = "精读伯恩斯经典，掌握认知扭曲纠偏与抗焦虑工具",
                    icon = "📖",
                    currentProgress = 18,
                    targetTotal = 25,
                    unit = "章",
                    targetAttribute = "SAN",
                    expRewardPerStep = 15
                ),
                SideQuest(
                    title = "申论高分范文精析与仿写",
                    description = "按人民网评与官方公文标准精析30篇核心申论范文",
                    icon = "✍️",
                    currentProgress = 12,
                    targetTotal = 30,
                    unit = "篇",
                    targetAttribute = "TEC",
                    expRewardPerStep = 20
                )
            )
        ),

        FullBlueprintTemplate(
            id = "tech_indie",
            name = "IT技术出海 · 独立开发者",
            tag = "22~30岁 · 技能立身",
            description = "构筑全球化数字技能，掌握全栈架构与AI工具流，以打造自给自足的数字化现金流为核心。",
            chapter = LifeChapter(
                title = "22 ~ 30 岁 · 技能淬炼与全球数字游民期",
                startAge = 22,
                endAge = 30,
                currentAge = 24,
                daysPassed = 120,
                description = "掌握直接面对真实市场的产品构建与交付能力，积累可复利数字资产，用技术杠杆放大个人产出。",
                focusAttributes = "TEC,GOLD,BND",
                isActive = true
            ),
            campaign = Campaign(
                title = "独立产品海外上线与首期交付战役",
                description = "完成一个轻量商业产品从技术原型、支付网关到海外市场冷启动的全流程闭环。",
                targetDate = "2026-08-30",
                currentProgress = 16,
                targetTotal = 40,
                unit = "模块",
                chapterId = 1,
                isActive = true,
                stageName = "核心功能实现与交互对齐"
            ),
            sideQuests = listOf(
                SideQuest(
                    title = "英文技术播客与文档精读",
                    description = "坚持听原版播客与海外前沿开源文档，打破语言信息茧房",
                    icon = "🎙️",
                    currentProgress = 22,
                    targetTotal = 50,
                    unit = "期",
                    targetAttribute = "TEC",
                    expRewardPerStep = 15
                )
            )
        ),

        FullBlueprintTemplate(
            id = "family_asset",
            name = "成家立业 · 家庭资产护城河",
            tag = "28~38岁 · 风险防御",
            description = "严格执行高性价比家庭资产配置：建立6个月纯现金流动性备用金、梳理医疗险底线、极简现金流掌控。",
            chapter = LifeChapter(
                title = "28 ~ 38 岁 · 家庭资产防守与抗风险安全垫搭建",
                startAge = 28,
                endAge = 38,
                currentAge = 29,
                daysPassed = 180,
                description = "进入多角色责任期，坚决避免重大资本支出踩坑与隐性负债，以稳固家庭安全底线为第一原则。",
                focusAttributes = "GOLD,SAN,BND",
                isActive = true
            ),
            campaign = Campaign(
                title = "家庭半年高流动性应急金积累战役",
                description = "通过极简生活与戒断拿铁因子，在低波稳健账户积累满6个月刚性开支的安全气囊。",
                targetDate = "2026-12-31",
                currentProgress = 3,
                targetTotal = 6,
                unit = "月度开支",
                chapterId = 1,
                isActive = true,
                stageName = "阶段一：控制非必要消费"
            ),
            sideQuests = listOf(
                SideQuest(
                    title = "极简生活与连续无冲动消费记账",
                    description = "用纯记账约束现金流，连续记录每一笔真实开支",
                    icon = "📒",
                    currentProgress = 14,
                    targetTotal = 30,
                    unit = "天",
                    targetAttribute = "GOLD",
                    expRewardPerStep = 10
                )
            )
        ),

        FullBlueprintTemplate(
            id = "health_vitality",
            name = "身体防御 · 精力与抗衰工程",
            tag = "35~45岁 · 健康自律",
            description = "把身体当成核心生产力资产来运营。以规范睡眠、心肺耐力、抗炎饮食与年度体检为护盾。",
            chapter = LifeChapter(
                title = "35 ~ 45 岁 · 身体防御与高质量精力运营期",
                startAge = 35,
                endAge = 45,
                currentAge = 35,
                daysPassed = 90,
                description = "慢性病与疲劳最易滋生的窗口期，必须以极其严肃的科学态度对待睡眠、血压与心肺运动。",
                focusAttributes = "VIT,SAN,GOLD",
                isActive = true
            ),
            campaign = Campaign(
                title = "心肺活力与体脂达标年度战役",
                description = "全年保持科学心率有氧运动累计120次，保障心肺功能与血管弹性。",
                targetDate = "2026-12-31",
                currentProgress = 42,
                targetTotal = 120,
                unit = "次",
                chapterId = 1,
                isActive = true,
                stageName = "第二季度规律慢跑期"
            ),
            sideQuests = listOf(
                SideQuest(
                    title = "7.5小时规律深睡眠打卡",
                    description = "遵循昼夜节律固定作息，提升免疫力与大脑代谢清除效率",
                    icon = "🌙",
                    currentProgress = 19,
                    targetTotal = 30,
                    unit = "天",
                    targetAttribute = "VIT",
                    expRewardPerStep = 10
                )
            )
        )
    )
}
