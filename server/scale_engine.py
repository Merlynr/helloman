# -*- coding: utf-8 -*-
"""
基于 GitHub 开源项目《高性价比人生指南》(HowToLiveBetter) 的本地 Agent 推演与评价标尺引擎
"""
import re
from typing import Dict, Any, List

class HowToLiveBetterEngine:

    SYSTEM_PRINCIPLES = """
    【循证与证据等级】：拒绝鸡汤与虚妄自律，只认可具备真实复利的行动。
    【成本与收益比】：衡量用户行为所消耗的时间、金钱和意志力成本，与其带来的短期即时收益与长期复利。
    【底线防御大于盲目进攻】：保护睡眠、防病防猝死、防金融债务陷阱、防法律风险的权重，远高于短期的盲目亢奋。
    【多周期三层标尺】：
       - 近期 (Daily 即时反馈)：关注睡眠时长与作息、低成本微运动、健康饮食、心流工作、防久坐与防蓝光；
       - 阶段性 (Weekly/Monthly 习惯成型)：关注打卡连续性、月度储蓄率与现金流结余、系统性知识输出、体能进阶；
       - 长期 (Yearly 人生蓝图)：关注健康安全底线(年度体检/疫苗)、社会保障合规(医保社保防致贫)、核心职业技能护城河。
    """

    @classmethod
    def evaluate_genesis(cls, background_text: str) -> Dict[str, Any]:
        """
        根据用户过往经历推演初始角色面板
        """
        lower = background_text.lower()
        level = 1
        tec = 25
        vit = 25
        san = 25
        gold = 20
        rel = 20
        traits = []

        if any(w in lower for w in ["计算机", "代码", "开发", "工程", "软件", "算法"]):
            tec += 15
            level += 1
            traits.append({"name": "逻辑思维", "description": "系统性思考与问题拆解能力，TEC获取效率+10%", "isDebuff": False})

        if any(w in lower for w in ["跑", "健身", "马拉松", "运动", "体能"]):
            vit += 15
            traits.append({"name": "强健体魄", "description": "具备基础运动耐受力，每日基础气血恢复速度提升", "isDebuff": False})

        if any(w in lower for w in ["熬夜", "晚睡", "失眠", "久坐"]):
            vit -= 5
            traits.append({"name": "作息透支", "description": "长期存在节律债务，VIT上限受到潜在抑制，需尽快修复", "isDebuff": True})

        if any(w in lower for w in ["记账", "储蓄", "理财", "定投"]):
            gold += 15
            traits.append({"name": "防御储备", "description": "具备良好的财务安全垫意识，抗金融风险韧性增强", "isDebuff": False})

        if any(w in lower for w in ["社交", "合作", "团队", "沟通"]):
            rel += 10

        title = "初窥门径的工程师" if tec > 35 else "稳健探索的旅人"
        if vit > 35 and tec > 35:
            title = "体脑双修的求索者"

        commentary = (
            f"根据《高性价比人生指南》对你过往履历的循证量化：你当前在核心技能与认知积累上处于优势阶梯（TEC={tec}），"
            f"而在底线健康防守（VIT={vit}）与资产安全垫（GOLD={gold}）上具备良好的可塑空间。"
            f"长线复利的关键在于先筑牢健康防线，以低成本微习惯避免意志力早衰。"
        )

        return {
            "title": title,
            "level": level,
            "tec": max(10, min(100, tec)),
            "vit": max(10, min(100, vit)),
            "san": max(10, min(100, san)),
            "gold": max(10, min(100, gold)),
            "rel": max(10, min(100, rel)),
            "commentary": commentary,
            "initialTraits": traits
        }

    @classmethod
    def evaluate_settlement(
        cls,
        today_input: str,
        step_count: int,
        completed_tasks: List[str]
    ) -> Dict[str, Any]:
        """
        基于多周期标尺裁决每日流水与步数
        """
        tec_delta = 0
        vit_delta = 0
        san_delta = 0
        gold_delta = 0
        rel_delta = 0
        exp = 20

        # 1. 计步器客观传感器评估
        if step_count >= 10000:
            vit_delta += 15
            exp += 25
        elif step_count >= 6000:
            vit_delta += 10
            exp += 15
        elif step_count > 0:
            vit_delta += 5
            exp += 5

        # 2. 预定任务加权
        exp += len(completed_tasks) * 15

        lower = today_input.lower()

        # 3. 智力/心流深度工作
        if any(w in lower for w in ["代码", "学习", "读书", "写作", "论文", "研发", "研究", "思考"]):
            tec_delta += 15
            san_delta += 5
            exp += 20

        # 4. 运动与身体磨砺
        if any(w in lower for w in ["跑", "健身", "游泳", "骑行", "散步", "锻炼", "拉伸"]):
            vit_delta += 15
            san_delta += 8
            exp += 20

        # 5. 睡眠与心力庇护
        if any(w in lower for w in ["早睡", "午休", "睡够", "冥想", "放松"]):
            vit_delta += 8
            san_delta += 10
            exp += 10

        # 6. 熬夜与透支惩罚 (负反馈)
        if any(w in lower for w in ["熬夜", "通宵", "失眠", "暴饮暴食", "宿醉"]):
            vit_delta -= 12
            san_delta -= 8
            exp = max(5, exp - 15)

        # 7. 消费与财务意识
        money_match = re.search(r'(?:花了|支出|消费|买|外卖|餐|饭).*?(\d+)', today_input)
        if money_match:
            try:
                amt = int(money_match.group(1))
                gold_delta -= min(amt, 100)
            except Exception:
                gold_delta -= 20
        if any(w in lower for w in ["记账", "存", "理财", "结余", "定投"]):
            gold_delta += 12
            exp += 10

        # 8. 社交与协同
        if any(w in lower for w in ["聚", "朋友", "家人", "交流", "沟通", "合作"]):
            rel_delta += 10
            san_delta += 5
            exp += 10

        buffs = []
        debuffs = []
        if vit_delta >= 15:
            buffs.append("气血充盈 (明日恢复速度提升)")
        if san_delta >= 10:
            buffs.append("心如止水 (专注心流加成)")
        if any(w in lower for w in ["熬夜", "通宵"]):
            debuffs.append("睡眠债务 (心智疲倦)")

        # 三层标尺精辟点评
        short_term = "微习惯有效推进，传感器步数与心流专注提供了稳健的确定性收益。"
        mid_term = "坚持阶段性打卡与现金流管控，遵循高性价比复利法则，防止大起大落。"
        long_term = "根据《高性价比人生指南》，持续筑牢健康与储蓄安全底线，是抵抗外部不确定性的最高收益投资。"

        dialogue = (
            f"【今日 Agent 裁决】检测到你的有效生活实践！"
            f"步数达到 {step_count} 步，产出扎实成长经验 +{exp} EXP。"
            f"保持这种低心智内耗的高性价比节奏！"
        )

        return {
            "dialogue": dialogue,
            "expGained": exp,
            "attributeDeltas": {
                "tec": tec_delta,
                "vit": vit_delta,
                "san": san_delta,
                "gold": gold_delta,
                "rel": rel_delta
            },
            "buffsApplied": buffs,
            "debuffsApplied": debuffs,
            "scaleEvaluation": {
                "shortTerm": short_term,
                "midTerm": mid_term,
                "longTerm": long_term
            }
        }
