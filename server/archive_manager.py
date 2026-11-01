# -*- coding: utf-8 -*-
"""
电脑端个人成长数字档案与第二大脑持久化管理模块
"""
import json
import os
from datetime import datetime
from typing import Dict, Any, List, Optional

DATA_DIR = os.path.join(os.path.dirname(__file__), "data")
PROFILE_FILE = os.path.join(DATA_DIR, "profile.json")
LOGS_DIR = os.path.join(DATA_DIR, "daily_logs")
SUMMARY_MD = os.path.join(DATA_DIR, "life_archive.md")

os.makedirs(LOGS_DIR, exist_ok=True)

class ArchiveManager:

    @classmethod
    def get_profile(cls) -> Dict[str, Any]:
        if os.path.exists(PROFILE_FILE):
            try:
                with open(PROFILE_FILE, "r", encoding="utf-8") as f:
                    return json.load(f)
            except Exception:
                pass
        
        default_profile = {
            "name": "冒险者",
            "title": "初窥门径的探索者",
            "level": 1,
            "exp": 0,
            "maxExp": 100,
            "tec": 20,
            "vit": 20,
            "san": 20,
            "gold": 20,
            "rel": 20,
            "hp": 100,
            "maxHp": 100,
            "mp": 100,
            "maxMp": 100,
            "isInitialized": False,
            "backgroundStory": "",
            "traits": []
        }
        cls.save_profile(default_profile)
        return default_profile

    @classmethod
    def save_profile(cls, profile: Dict[str, Any]):
        profile["updatedAt"] = datetime.now().isoformat()
        with open(PROFILE_FILE, "w", encoding="utf-8") as f:
            json.dump(profile, f, ensure_ascii=False, indent=2)

    @classmethod
    def save_daily_settlement(
        cls,
        date_str: str,
        raw_input: str,
        step_count: int,
        settlement: Dict[str, Any]
    ) -> Dict[str, Any]:
        """
        更新玩家核心属性并将战报归档到电脑本地
        """
        profile = cls.get_profile()

        # 1. 经验累加与等级判定
        exp_gained = settlement.get("expGained", 0)
        new_exp = profile.get("exp", 0) + exp_gained
        level = profile.get("level", 1)
        max_exp = profile.get("maxExp", 100)

        while new_exp >= max_exp:
            new_exp -= max_exp
            level += 1
            max_exp = int(max_exp * 1.25)

        # 2. 五维属性更新
        deltas = settlement.get("attributeDeltas", {})
        profile["level"] = level
        profile["exp"] = max(0, new_exp)
        profile["maxExp"] = max_exp
        profile["tec"] = max(1, min(999, profile.get("tec", 20) + deltas.get("tec", 0)))
        profile["vit"] = max(1, min(999, profile.get("vit", 20) + deltas.get("vit", 0)))
        profile["san"] = max(1, min(999, profile.get("san", 20) + deltas.get("san", 0)))
        profile["gold"] = max(1, min(999, profile.get("gold", 20) + deltas.get("gold", 0)))
        profile["rel"] = max(1, min(999, profile.get("rel", 20) + deltas.get("rel", 0)))

        cls.save_profile(profile)

        # 3. 归档单日详细数据
        log_entry = {
            "date": date_str,
            "timestamp": datetime.now().isoformat(),
            "rawInput": raw_input,
            "stepCount": step_count,
            "settlement": settlement,
            "profileSnapshot": profile
        }

        log_path = os.path.join(LOGS_DIR, f"{date_str}_{datetime.now().strftime('%H%M%S')}.json")
        with open(log_path, "w", encoding="utf-8") as f:
            json.dump(log_entry, f, ensure_ascii=False, indent=2)

        # 4. 追加写入 Human-readable Markdown 历程日志
        cls._append_to_summary_markdown(date_str, raw_input, step_count, settlement, profile)

        return profile

    @classmethod
    def _append_to_summary_markdown(cls, date_str: str, raw: str, steps: int, s: Dict[str, Any], p: Dict[str, Any]):
        deltas = s.get("attributeDeltas", {})
        scale = s.get("scaleEvaluation", {})
        entry_md = f"""
### 📅 {date_str} 日常战报与成长复盘
- **传感器步数**：{steps} 步 | **获得经验**：+{s.get('expGained', 0)} EXP (当前等级: Lv.{p.get('level')} | {p.get('title')})
- **原始流水**：{raw}
- **属性变动**：智力({deltas.get('tec',0):+d}) | 气血({deltas.get('vit',0):+d}) | 心力({deltas.get('san',0):+d}) | 财力({deltas.get('gold',0):+d}) | 人脉({deltas.get('rel',0):+d})
- **Agent 战报**：{s.get('dialogue', '')}
- **《高性价比人生指南》点评**：
  - *近期微习惯*：{scale.get('shortTerm', '')}
  - *阶段性防线*：{scale.get('midTerm', '')}
  - *长期复利底线*：{scale.get('longTerm', '')}

---
"""
        with open(SUMMARY_MD, "a", encoding="utf-8") as f:
            f.write(entry_md)

    @classmethod
    def get_recent_history(cls, limit: int = 15) -> List[Dict[str, Any]]:
        files = sorted(os.listdir(LOGS_DIR), reverse=True)[:limit]
        history = []
        for file in files:
            if file.endswith(".json"):
                with open(os.path.join(LOGS_DIR, file), "r", encoding="utf-8") as f:
                    history.append(json.load(f))
        return history
