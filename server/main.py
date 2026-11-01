# -*- coding: utf-8 -*-
"""
人生 Online - 电脑端 Agent 守护进程服务 (LifeOnline Agent Daemon)
"""
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
from datetime import datetime

from scale_engine import HowToLiveBetterEngine
from archive_manager import ArchiveManager

app = FastAPI(
    title="LifeOnline Agent Daemon",
    description="人生 Online 电脑端 Agent 守护进程与数字记忆大脑",
    version="1.0.0"
)

# 允许跨域与局域网访问
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class GenesisRequest(BaseModel):
    background: str

class SettleRequest(BaseModel):
    date: Optional[str] = None
    rawInput: str
    stepCount: int = 0
    completedTasks: List[str] = []

class SyncLogItem(BaseModel):
    date: str
    rawInput: str
    stepCount: int = 0
    completedTasks: List[str] = []

class SyncRequest(BaseModel):
    pendingLogs: List[SyncLogItem]

@app.get("/")
def root():
    profile = ArchiveManager.get_profile()
    return {
        "status": "online",
        "service": "LifeOnline Agent Daemon",
        "principles": "HowToLiveBetter Benchmark Scale",
        "character": {
            "title": profile.get("title"),
            "level": profile.get("level"),
            "exp": f"{profile.get('exp')}/{profile.get('maxExp')}"
        }
    }

@app.get("/api/status")
def get_status():
    return ArchiveManager.get_profile()

@app.post("/api/genesis")
def character_genesis(req: GenesisRequest):
    """
    根据现实经历录入，推演生成初始角色
    """
    if not req.background.strip():
        raise HTTPException(status_code=400, detail="经历输入不能为空")

    result = HowToLiveBetterEngine.evaluate_genesis(req.background)

    profile = ArchiveManager.get_profile()
    profile["name"] = "玩家"
    profile["title"] = result["title"]
    profile["level"] = result["level"]
    profile["exp"] = 0
    profile["maxExp"] = 100 * result["level"]
    profile["tec"] = result["tec"]
    profile["vit"] = result["vit"]
    profile["san"] = result["san"]
    profile["gold"] = result["gold"]
    profile["rel"] = result["rel"]
    profile["isInitialized"] = True
    profile["backgroundStory"] = req.background
    profile["traits"] = result["initialTraits"]

    ArchiveManager.save_profile(profile)

    return {
        "genesisResult": result,
        "profile": profile
    }

@app.post("/api/settle")
def settle_journal(req: SettleRequest):
    """
    每日战报裁决与经验结算
    """
    date_str = req.date or datetime.now().strftime("%Y-%m-%d")

    # 1. 运行《高性价比人生指南》Agent 标尺引擎
    settlement = HowToLiveBetterEngine.evaluate_settlement(
        today_input=req.rawInput,
        step_count=req.stepCount,
        completed_tasks=req.completedTasks
    )

    # 2. 持久化到电脑主档案并演进角色属性
    updated_profile = ArchiveManager.save_daily_settlement(
        date_str=date_str,
        raw_input=req.rawInput,
        step_count=req.stepCount,
        settlement=settlement
    )

    return {
        "settlement": settlement,
        "profile": updated_profile
    }

@app.post("/api/sync")
def sync_offline_logs(req: SyncRequest):
    """
    离线批量同步：接收手机端暂存的离线流水，逐条结算归档
    """
    results = []
    latest_profile = ArchiveManager.get_profile()

    for item in req.pendingLogs:
        settlement = HowToLiveBetterEngine.evaluate_settlement(
            today_input=item.rawInput,
            step_count=item.stepCount,
            completed_tasks=item.completedTasks
        )
        latest_profile = ArchiveManager.save_daily_settlement(
            date_str=item.date,
            raw_input=item.rawInput,
            step_count=item.stepCount,
            settlement=settlement
        )
        results.append({
            "date": item.date,
            "settlement": settlement
        })

    return {
        "syncedCount": len(results),
        "results": results,
        "profile": latest_profile
    }

@app.get("/api/history")
def get_history(limit: int = 15):
    return ArchiveManager.get_recent_history(limit)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8080, reload=True)
