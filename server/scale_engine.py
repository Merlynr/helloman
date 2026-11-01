# -*- coding: utf-8 -*-
"""
基于 GitHub 开源项目《高性价比人生指南》(HowToLiveBetter) 的云端 Agent 推演与评价标尺引擎
内置硅基流动 (SiliconFlow) 大模型云端直连 + 离线智能确定性规则双引擎
"""
import os
import re
import json
import urllib.request
import urllib.error
from typing import Dict, Any, List

API_KEY = os.environ.get("SILICONFLOW_API_KEY", "sk-imwzguqatcmhibwwgzsldifjtqoraxbylmveicuibgkabnwz")
BASE_URL = os.environ.get("SILICONFLOW_BASE_URL", "https://api.siliconflow.cn/v1")
MODEL_NAME = os.environ.get("SILICONFLOW_MODEL", "Qwen/Qwen2.5-7B-Instruct")

class HowToLiveBetterEngine:

    SYSTEM_BENCHMARK_PROMPT = """
你是一款硬核现实 RPG 游戏《人生 Online》的云端 AI 裁决导师。
你的经验结算与评价严格遵循开源项目《高性价比人生指南》(HowToLiveBetter) 的循证与高性价比哲学：
1. 【循证与证据等级】：拒绝鸡汤与虚妄玄学，只认可具备真实复利的行动。
2. 【成本与收益比】：衡量用户行为所消耗的时间、金钱和意志力成本，与其带来的短期即时收益与长期复利。
3. 【底线防御大于盲目进攻】：保护睡眠、防病防猝死、防金融债务陷阱、防法律风险的权重，远高于短期的盲目亢奋。
4. 【多周期三层标尺】：
   - 近期 (Daily 即时反馈)：关注睡眠时长与作息、低成本微运动、健康饮食、心流工作、防久坐与防蓝光；
   - 阶段性 (Weekly/Monthly 习惯成型)：关注打卡连续性、月度储蓄率与现金流结余、系统性知识输出、体能进阶；
   - 长期 (Yearly 人生蓝图)：关注健康安全底线(年度体检/疫苗)、社会保障合规(医保社保防致贫)、核心职业技能护城河。

五维属性说明：
- TEC (智力/脑力): 专业技能、深度思考、认知提升、知识吸收
- VIT (气血/体魄): 睡眠质量、有氧与抗阻运动、心肺机能、饮食健康
- SAN (心力/精神): 情绪稳定、专注力、意志力韧性、自省与冥想
- GOLD (财力/资产): 储蓄、记账、避免非必要消费、长期投资
- REL (人脉/社交): 沟通协作、亲密关系维系、家庭支持、高质量社交圈

请以幽默但极富洞察力的 RPG 旁白语气给出结算与战报。输出必须严格为 JSON 格式。
"""

    @classmethod
    def evaluate_genesis(cls, background_text: str) -> Dict[str, Any]:
        """
        根据用户过往经历推演初始角色面板（优先云端大模型，兜底规则）
        """
        # 尝试调用大模型
        if API_KEY:
            try:
                user_prompt = f"""
用户录入了其现实生活中的真实经历、教育成就与生活习惯：
【用户背景】
{background_text}

请根据《高性价比人生指南》的现实标尺，为该用户初始化其《人生 Online》角色面板。
计算初始等级 (建议 1 - 5 级)、五维初始属性点 (每项基准值 10~50 之间，总点数与其实际积累成正比)、专属头衔、初始评价与 2~3 个天生特质 (Trait)。

请严格输出以下 JSON 结构，不要包含多余文字：
{{
  "title": "例如：初窥门径的工程师 / 稳健求索的学者",
  "level": 2,
  "tec": 35,
  "vit": 25,
  "san": 30,
  "gold": 20,
  "rel": 20,
  "commentary": "对用户过往经历的硬核点评，指出其优势维度与当前亟需防守的短板",
  "initialTraits": [
    {{"name": "夜间灵感", "description": "深夜思考经验+10%，但气血消耗增加", "isDebuff": false}},
    {{"name": "亚健康预警", "description": "久坐运动不足，体能恢复速度-10%", "isDebuff": true}}
  ]
}}
"""
                resp = cls._call_llm(cls.SYSTEM_BENCHMARK_PROMPT, user_prompt)
                clean_json = cls._extract_json(resp)
                data = json.loads(clean_json)
                if "title" in data and "tec" in data:
                    return data
            except Exception as e:
                print(f"[Engine] 大模型推演 Genesis 异常，降级到本地规则: {e}")

        # 本地确定性规则兜底
        return cls._local_genesis_fallback(background_text)

    @classmethod
    def evaluate_settlement(
        cls,
        today_input: str,
        step_count: int,
        completed_tasks: List[str]
    ) -> Dict[str, Any]:
        """
        基于多周期标尺裁决每日流水与步数（优先云端大模型，兜底规则）
        """
        if API_KEY:
            try:
                user_prompt = f"""
今日安卓端自动采集与完成情况：
- 传感器步数：{step_count} 步
- 已完成预定任务：{", ".join(completed_tasks) if completed_tasks else "无"}

用户提交的今日极简流水记录：
【流水内容】
{today_input}

请根据《高性价比人生指南》三层标尺裁决：
1. 计算经验掉落 (expGained, 通常在 15 - 80 之间，若有严重摆烂或熬夜可扣减惩罚)；
2. 计算五维增减 (attributeDeltas: tec, vit, san, gold, rel，整数，可正可负)；
3. 判定是否附带 BUFF 或 DEBUFF (各提供 0~2 个)；
4. 给出简短幽默且富含人生哲理的战报旁白 (dialogue)；
5. 分别从 近期、阶段性、长期 三个层次给出基于指南的精辟建议 (scaleEvaluation)。

请严格输出以下 JSON 结构：
{{
  "dialogue": "今日战报短评...",
  "expGained": 40,
  "attributeDeltas": {{
    "tec": 10,
    "vit": 15,
    "san": 5,
    "gold": -20,
    "rel": 0
  }},
  "buffsApplied": ["体能充沛 (明日气血增长加速)"],
  "debuffsApplied": [],
  "scaleEvaluation": {{
    "shortTerm": "近期微习惯点评...",
    "midTerm": "阶段性习惯与现金流点评...",
    "longTerm": "长期复利与防御底线点评..."
  }}
}}
"""
                resp = cls._call_llm(cls.SYSTEM_BENCHMARK_PROMPT, user_prompt)
                clean_json = cls._extract_json(resp)
                data = json.loads(clean_json)
                if "expGained" in data and "attributeDeltas" in data:
                    return data
            except Exception as e:
                print(f"[Engine] 大模型推演 Settlement 异常，降级到本地规则: {e}")

        # 本地规则兜底
        return cls._local_settlement_fallback(today_input, step_count, completed_tasks)

    @classmethod
    def _call_llm(cls, sys_prompt: str, user_prompt: str) -> str:
        endpoint = f"{BASE_URL.rstrip('/')}/chat/completions"
        payload = {
            "model": MODEL_NAME,
            "messages": [
                {"role": "system", "content": sys_prompt},
                {"role": "user", "content": user_prompt}
            ],
            "temperature": 0.7,
            "max_tokens": 1200
        }
        data_bytes = json.dumps(payload).encode("utf-8")
        req = urllib.request.Request(
            endpoint,
            data=data_bytes,
            headers={
                "Authorization": f"Bearer {API_KEY}",
                "Content-Type": "application/json"
            }
        )
        with urllib.request.urlopen(req, timeout=30) as response:
            result = json.loads(response.read().decode("utf-8"))
            return result["choices"][0]["message"]["content"]

    @classmethod
    def _extract_json(cls, text: str) -> str:
        text = text.strip()
        match = re.search(r'```(?:json)?\s*([\s\S]*?)\s*```', text)
        if match:
            return match.group(1).strip()
        first_brace = text.find('{')
        last_brace = text.rfind('}')
        if first_brace != -1 and last_brace != -1 and last_brace > first_brace:
            return text[first_brace:last_brace + 1]
        return text

    @classmethod
    def _local_genesis_fallback(cls, background_text: str) -> Dict[str, Any]:
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

        title = "初窥门径的工程师" if tec > 35 else "稳健探索的旅人"
        commentary = "根据《高性价比人生指南》循证量化：你的初始五维已均衡建立。长线复利的关键在于先筑牢健康防线，以低成本微习惯避免意志力早衰。"

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
    def _local_settlement_fallback(
        cls,
        today_input: str,
        step_count: int,
        completed_tasks: List[str]
    ) -> Dict[str, Any]:
        tec_delta = 0
        vit_delta = 0
        san_delta = 0
        gold_delta = 0
        rel_delta = 0
        exp = 20

        if step_count >= 10000:
            vit_delta += 15
            exp += 25
        elif step_count >= 6000:
            vit_delta += 10
            exp += 15
        elif step_count > 0:
            vit_delta += 5
            exp += 5

        exp += len(completed_tasks) * 15
        lower = today_input.lower()

        if any(w in lower for w in ["代码", "学习", "读书", "写作", "论文", "研发", "研究", "思考"]):
            tec_delta += 15
            san_delta += 5
            exp += 20

        if any(w in lower for w in ["跑", "健身", "游泳", "骑行", "散步", "锻炼", "拉伸"]):
            vit_delta += 15
            san_delta += 8
            exp += 20

        if any(w in lower for w in ["早睡", "午休", "睡够", "冥想", "放松"]):
            vit_delta += 8
            san_delta += 10
            exp += 10

        if any(w in lower for w in ["熬夜", "通宵", "失眠", "暴饮暴食"]):
            vit_delta -= 12
            san_delta -= 8
            exp = max(5, exp - 15)

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

        buffs = []
        debuffs = []
        if vit_delta >= 15:
            buffs.append("气血充盈 (明日恢复速度提升)")
        if san_delta >= 10:
            buffs.append("心如止水 (专注心流加成)")
        if any(w in lower for w in ["熬夜", "通宵"]):
            debuffs.append("睡眠债务 (心智疲倦)")

        return {
            "dialogue": f"【今日 Agent 裁决】步数达到 {step_count} 步，产出扎实成长经验 +{exp} EXP。保持这种低心智内耗的高性价比节奏！",
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
                "shortTerm": "微习惯有效推进，传感器步数与心流专注提供了稳健的确定性收益。",
                "midTerm": "坚持阶段性打卡与现金流管控，遵循高性价比复利法则，防止大起大落。",
                "longTerm": "根据《高性价比人生指南》，持续筑牢健康与储蓄安全底线，是抵抗外部不确定性的最高收益投资。"
            }
        }
