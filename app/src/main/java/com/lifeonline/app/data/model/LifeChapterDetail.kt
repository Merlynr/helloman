package com.lifeonline.app.data.model

/**
 * 篇章核心发展任务项 (Developmental Task)
 */
data class DevelopmentalTaskItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // 职业, 财务, 生理, 心理, 关系
    val defaultAchieved: Boolean = false
)

/**
 * 权威调查与学术支撑数据项 (CFPS / CHARLS / 张侃“5个20年” / 毕生发展心理学)
 */
data class EmpiricalInsight(
    val source: String,     // e.g. "北京大学 CFPS 中国家庭追踪调查"
    val dataQuote: String,  // e.g. "CFPS 数据表明：35岁后被动离职平均再就业周期延长至7.8个月..."
    val keyTakeaway: String // 落地核心启示
)

/**
 * 五大本土循证人生篇章静态完整定义
 */
data class EvidenceBasedChapter(
    val stageIndex: Int,
    val name: String,
    val enTag: String,
    val ageRange: String,
    val startAge: Int,
    val endAge: Int,
    val theorySource: String,      // 理论来源
    val subtitle: String,
    val macroStrategy: String,     // 宏观指导思想
    val focusAttributes: String,   // 重点属性
    val developmentalTasks: List<DevelopmentalTaskItem>,
    val empiricalInsight: EmpiricalInsight,
    val recommendedCampaignTitle: String,
    val recommendedCampaignTarget: String
)

object ChineseLifeCourseFramework {

    val allChapters: List<EvidenceBasedChapter> = listOf(
        // 1. 成年初显期 (18 ~ 25 岁)
        EvidenceBasedChapter(
            stageIndex = 1,
            name = "成年初显期 · 技能淬炼与认知破局",
            enTag = "EMERGING ADULTHOOD",
            ageRange = "18 ~ 25 岁",
            startAge = 18,
            endAge = 25,
            theorySource = "张侃“第一个20年”成人期 × 伯格尔《毕生发展心理学》第18章",
            subtitle = "从依赖走向独立，完成心理断乳与核心生存硬技能初验",
            macroStrategy = "此阶段核心矛盾是角色探索与生存焦虑。保持极低物质欲望，以技能积累和真实商业试错为最高优先级，警惕虚无主义与沉溺消费主义。",
            focusAttributes = "TEC,SAN,BND",
            developmentalTasks = listOf(
                DevelopmentalTaskItem("t1_1", "经济独立断乳", "掌握不依赖家庭的最基本谋生与现金流获取能力", "财务", true),
                DevelopmentalTaskItem("t1_2", "核心硬技能初验", "掌握至少一项在劳动力市场具有明确交换价值的专业技能", "职业", true),
                DevelopmentalTaskItem("t1_3", "走出学生思维模式", "理解真实社会运转逻辑与体制/商业规则，建立契约精神", "认知", false),
                DevelopmentalTaskItem("t1_4", "情绪自我调节机制", "建立至少一种不依赖外界物质刺激的情绪复原工具(运动/阅读)", "心理", true),
                DevelopmentalTaskItem("t1_5", "良师益友圈层构建", "建立能提供真实认知反馈的2~3位良性社交连接", "关系", false)
            ),
            empiricalInsight = EmpiricalInsight(
                source = "北京大学 CFPS (中国家庭追踪调查) 长期面板",
                dataQuote = "CFPS 调查追踪显示：初职进入年龄在23~25岁且首份工作专业对口度高的人群，其工作前10年的收入增长斜率较频繁跨行业试错者高出约38%。",
                keyTakeaway = "在25岁前找准一个高复利赛道做深，比盲目频繁转行更有利于搭建安全垫。"
            ),
            recommendedCampaignTitle = "核心硬技能从零到一实战交付战役",
            recommendedCampaignTarget = "完成500小时深度专注或独立项目交付"
        ),

        // 2. 成家立业期 (25 ~ 35 岁) - 当前主力阶段
        EvidenceBasedChapter(
            stageIndex = 2,
            name = "成家立业 · 职业护城河与安全垫黄金十年",
            enTag = "EARLY ADULTHOOD",
            ageRange = "25 ~ 35 岁",
            startAge = 25,
            endAge = 35,
            theorySource = "张侃“第二个20年”立业期（三十而立） × CFPS就业与家庭数据",
            subtitle = "人生最关键的攻守窗口期，构筑不可逆的职业护城河与资产防线",
            macroStrategy = "根据《高性价比人生指南》，25~35岁是个人核心技能定型、抗风险安全垫搭建的最后无阻碍窗口。防守远优先于冒险，攻坚体制资质或绝对专业护城河，防范盲目负债与拿铁因子。",
            focusAttributes = "TEC,GOLD,VIT",
            developmentalTasks = listOf(
                DevelopmentalTaskItem("t2_1", "职业护城河确立", "考取体制内编制资质或在专业领域建立不可替代的技术壁垒", "职业", false),
                DevelopmentalTaskItem("t2_2", "6个月流动性安全气囊", "在低波动账户常备足以支撑家庭6个月纯刚性开支的应急备用金", "财务", true),
                DevelopmentalTaskItem("t2_3", "终身防守作息防线", "坚持规律7.5小时睡眠与每周心肺运动，拒绝慢性透支身体", "生理", false),
                DevelopmentalTaskItem("t2_4", "坦诚婚姻与家庭财务机制", "如果成家，建立合规透明的家庭资产约定与抗风险医疗险配置", "关系", false),
                DevelopmentalTaskItem("t2_5", "消费主义脱敏", "彻底戒断拿铁因子与诱导分期借贷，拥有绝对现金流掌控力", "财务", true)
            ),
            empiricalInsight = EmpiricalInsight(
                source = "北京大学 CFPS 劳动力市场流动性专题分析",
                dataQuote = "CFPS 数据显示：35岁后发生非自愿离职的求职者，平均再就业周期延长至7.8个月，且薪资平移难度增加45%。但在体制内或持有高阶专业资质者抗风险系数达到普通职位的3.2倍。",
                keyTakeaway = "在35岁前必须以真题海或硬资质完成护城河构筑，莫在职业安全垫未稳前消耗体力。"
            ),
            recommendedCampaignTitle = "2026 公务员考公上岸冲刺战役",
            recommendedCampaignTarget = "真题海1200道与模块实战模拟"
        ),

        // 3. 中流砥柱期 (35 ~ 45 岁)
        EvidenceBasedChapter(
            stageIndex = 3,
            name = "中流砥柱 · 身体防御与精力抗衰工程",
            enTag = "MIDDLE ADULTHOOD I",
            ageRange = "35 ~ 45 岁",
            startAge = 35,
            endAge = 45,
            theorySource = "CHARLS 中老龄健康基线调查 × 伯格尔《成年中期生理衰退》",
            subtitle = "把身体当成核心生产力资产来运营，严控代谢综合征与家庭承重危机",
            macroStrategy = "此阶段上有老下有小，抚养比达到一生峰值。生理基础代谢开始断崖式下行，必须以极其严肃的科学态度对待心肺运动、防癌筛查与精力管理，以静水深流应对外界风浪。",
            focusAttributes = "VIT,SAN,GOLD",
            developmentalTasks = listOf(
                DevelopmentalTaskItem("t3_1", "心脑血管防守壁垒", "保持体脂率正常，每周完成至少150分钟中高强度有氧运动", "生理", false),
                DevelopmentalTaskItem("t3_2", "消化道早期防癌筛查", "40岁前完成人生首次胃肠镜检查，规避可预防性重大器质病变", "生理", false),
                DevelopmentalTaskItem("t3_3", "家庭资产攻守平衡", "家庭负债率压缩至40%安全线以下，完善大病商业医疗保底", "财务", false),
                DevelopmentalTaskItem("t3_4", "职场第二曲线储备", "提炼方法论与管理/咨询复合能力，抵御单一技能老化", "职业", false),
                DevelopmentalTaskItem("t3_5", "心智内聚与抗焦虑", "接纳人生局限性，在责任重压下保持自洽、平静与专注", "心理", false)
            ),
            empiricalInsight = EmpiricalInsight(
                source = "CHARLS (中国健康与养老调查) 2011-2020 追踪成果",
                dataQuote = "CHARLS 数据明确揭示：在40岁之前就保持每周≥3次有氧运动、每年规律全身体检的中年人，其进入60岁后高血压、冠心病的发病率较无运动习惯者整整降低了42.6%。",
                keyTakeaway = "40岁前对体检与运动的微小投入，是中老年期性价比最高、回报最确定的健康复利。"
            ),
            recommendedCampaignTitle = "心肺活力与体脂达标年度战役",
            recommendedCampaignTarget = "全年累计高质量有氧运动120次"
        ),

        // 4. 高光贡献期 (45 ~ 60 岁)
        EvidenceBasedChapter(
            stageIndex = 4,
            name = "高光贡献 · 智慧复利与代际传承",
            enTag = "MIDDLE ADULTHOOD II",
            ageRange = "45 ~ 60 岁",
            startAge = 45,
            endAge = 60,
            theorySource = "张侃“第三个20年”贡献期 × 埃里克森“繁殖 vs 停滞”危机",
            subtitle = "人生阅历与社会资本的最高光峰值，平稳完成代际托举与养老金储备",
            macroStrategy = "充分释放前半生积淀的知识和人脉复利。减少盲目重资产扩张，把确定性放在第一位，做好子女独立扶持与自身退休金稳健资产池锁定。",
            focusAttributes = "GOLD,BND,SAN",
            developmentalTasks = listOf(
                DevelopmentalTaskItem("t4_1", "代际独立赋能", "协助子女完成社会化独立，做好边界感与家庭情感托底", "关系", false),
                DevelopmentalTaskItem("t4_2", "养老资产蓄水池闭环", "核验养老保险年限，建立抗通胀的稳健被动收益现金流组合", "财务", false),
                DevelopmentalTaskItem("t4_3", "骨骼与肌肉量维持", "加入抗阻力量训练，预防肌少症与骨质疏松", "生理", false),
                DevelopmentalTaskItem("t4_4", "社会经验知识变现与传承", "将实战技能封装为课程、著作或顾问角色，实现社会价值溢价", "职业", false),
                DevelopmentalTaskItem("t4_5", "生活节奏降速转换", "开始培养1~2种可以陪伴后半生的深度精神爱好", "心理", false)
            ),
            empiricalInsight = EmpiricalInsight(
                source = "中国社会科学院人口与劳动经济研究所研究报告",
                dataQuote = "实证调查显示：在50岁前就明确做好退休后社交网络与业余爱好规划的人群，其退休后的抑郁发生率不到骤然脱离职场人群的三分之一。",
                keyTakeaway = "后半生的幸福不仅靠养老金，更靠提前建立的精神自足与社会连接。"
            ),
            recommendedCampaignTitle = "养老保障稳固与肌肉抗衰工程",
            recommendedCampaignTarget = "完成养老金流水梳理与力量训练80次"
        ),

        // 5. 稳步谢幕期 (60 ~ 80+ 岁)
        EvidenceBasedChapter(
            stageIndex = 5,
            name = "稳步谢幕 · 活力晚年与人生归档",
            enTag = "LATE ADULTHOOD",
            ageRange = "60 ~ 80+ 岁",
            startAge = 60,
            endAge = 85,
            theorySource = "张侃“第四个20年”安度期（60-70上半场、70-80下半场）",
            subtitle = "健康慢速巡航，复盘梳理一生档案，安度澄明高质量余生",
            macroStrategy = "张侃指出60~70岁是活力的黄金上半场，抓紧完成游历与心愿；70~80岁进入医养下半场，保持规律作息。内心达成艾里克森所谓的人生整合感，坦然回顾无憾一生。",
            focusAttributes = "VIT,SAN,BND",
            developmentalTasks = listOf(
                DevelopmentalTaskItem("t5_1", "60-70上半场心愿通关", "在身体尚好时完成尚未实现的地理探索与生活心愿", "探索", false),
                DevelopmentalTaskItem("t5_2", "慢病规范化达标管理", "血压、血糖、血脂平稳达标，与社区家庭医生建立定期沟通", "生理", false),
                DevelopmentalTaskItem("t5_3", "一生个人回忆录归档", "梳理平生照片、日记与心得，完成专属个人人生档案归档", "心理", false),
                DevelopmentalTaskItem("t5_4", "深度亲密与日常互动", "与伴侣、老友保持高频良性互动，排解晚年孤独感", "关系", false),
                DevelopmentalTaskItem("t5_5", "安详接纳与生命整合", "达成内心安宁无憾，实现精神上的超然与丰足", "心理", false)
            ),
            empiricalInsight = EmpiricalInsight(
                source = "CHARLS 追踪中国百岁老人健康生活方式研究",
                dataQuote = "CHARLS 调研证明：长寿老人的核心共同特征并非特殊营养品，而是：终身保持体力微劳作、饮食盐油节制、以及拥有稳固温和的家庭支持系统。",
                keyTakeaway = "简单、节制、温和的人际关系，是走完圆满一生的终极秘诀。"
            ),
            recommendedCampaignTitle = "人生回忆录沉淀与慢速巡航战役",
            recommendedCampaignTarget = "完成一生大事纪整理与年度漫步100万步"
        )
    )
}
