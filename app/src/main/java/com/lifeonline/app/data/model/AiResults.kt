package com.lifeonline.app.data.model

data class TraitItem(
    val name: String,
    val description: String,
    val isDebuff: Boolean = false
)

data class CharacterGenesisResult(
    val title: String,
    val level: Int,
    val tec: Int,
    val vit: Int,
    val san: Int,
    val gold: Int,
    val rel: Int,
    val commentary: String,
    val initialTraits: List<TraitItem>
)

data class AttributeDeltas(
    val tec: Int = 0,
    val vit: Int = 0,
    val san: Int = 0,
    val gold: Int = 0,
    val rel: Int = 0
)

data class ScaleEvaluation(
    val shortTerm: String,
    val midTerm: String,
    val longTerm: String
)

data class SettlementResult(
    val dialogue: String,
    val expGained: Int,
    val attributeDeltas: AttributeDeltas,
    val buffsApplied: List<String> = emptyList(),
    val debuffsApplied: List<String> = emptyList(),
    val scaleEvaluation: ScaleEvaluation
)

data class CampaignPlanResult(
    val title: String,
    val targetDate: String = "2026-12-01",
    val currentProgress: Int = 0,
    val targetTotal: Int = 100,
    val unit: String = "次",
    val stageName: String = "第一阶段：基础夯实与习惯固化",
    val description: String = ""
)
