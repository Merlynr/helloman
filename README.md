# 人生 Online (Life Online) - Android 原生应用

一款基于 **Kotlin + Jetpack Compose** 构建的 RPG 风格个人成长与自律管理安卓应用。
将真实人生的琐事、习惯与成长数字化，为现实行为赋予即时正反馈、五维属性面板与科学的成长阶梯。

成长与经验裁决标尺严格参考 GitHub 开源项目 **《高性价比人生指南》([HowToLiveBetter](https://github.com/eternity4719/HowToLiveBetter))** 构建。

---

## 🌟 核心功能特色

1. **角色觉醒与属性初始化 (Character Genesis)**
   - 首次进入录入学历专业、过往里程碑成就、生活习惯现状；
   - AI 结合循证标尺自动生成初始角色头衔、初始等级、五维属性点 (TEC, VIT, SAN, GOLD, REL) 与天生特质 (Traits)。

2. **五维属性面板与复古终端 UI (Dashboard)**
   - 自定义 Canvas 绘制五维动态成长雷达图；
   - 包含 HP (体魄活力)、MP (心智专注)、EXP (经验条) 与活跃 Buff/Debuff 徽章。

3. **极简流水随手记 + 安卓端自动采集 (Journal & Auto Sensor)**
   - **自动采集**：集成安卓硬件计步传感器 (`Sensor.TYPE_STEP_COUNTER`)，后台低功耗自动累计步数，提供步数折算经验；
   - **极简记录**：支持一句话随手记日常流水（如：*“上午编程2小时，跑步4km，午餐轻食花费28元”*）；
   - **每日习惯打卡**：预置高性价比微习惯（7.5h 充足睡眠、6000 步运动、心流专注、收支记账）。

4. **《高性价比人生指南》三层多周期标尺 (Scale Benchmark)**
   - **近期 (Daily 即时反馈)**：关注微习惯投入产出比、精力和意志力保护，惩罚报复性熬夜；
   - **阶段性 (Weekly/Monthly 习惯成型)**：关注打卡连续性、月度储蓄率与现金流安全垫；
   - **长期 (Yearly 人生蓝图)**：构筑防重疾（疫苗与体检）、防意外致贫（医保社保）、防法律合同坑的底线防线，沉淀核心技能护城河。

5. **隐私优先与双引擎架构**
   - 数据完全保存在本地 SQLite / Room 数据库中；
   - 支持任意兼容 OpenAI 的模型 API（DeepSeek / 通义千问 / Kimi / GLM）；
   - **内置完全离线规则引擎**：即使未配置 API Key 或断网，也能基于标尺规则完成精准经验结算与属性演化！

---

## 🚀 快速上手与运行

1. 在 **Android Studio** 中点击 `Open` 打开本项目文件夹；
2. 保持网络通畅，等待 Gradle 依赖自动同步完成；
3. 连接安卓真机（开启 USB 调试）或启动 Android 模拟器；
4. 点击顶部的绿色 **Run (Shift + F10)** 按钮，即可在手机上启动体验！

---

## 🌐 云端 24 小时部署与 Cloudflare 穿透代理 (Server & Cloudflare Tunnel)

本项目支持 **C/S 协同模式**：你可以将 Python 守护进程常驻在 Linux 云服务器上，并通过 **Cloudflare Tunnel** 穿透暴露为受原生 SSL 保护的 HTTPS 服务，手机端无论处于 5G 还是任何 Wi-Fi 下均可随时直连推演，并自动生成个人 Markdown 知识库。

> 📖 **完整详尽的一键部署手册请查看**：[**服务端与 Cloudflare 部署文档 (DEPLOYMENT.md)**](file:///e:/project/helloman/DEPLOYMENT.md)

### 极速部署三步法：
1. **Linux 服务器一键启动**：执行 [`server/deploy.sh`](file:///e:/project/helloman/server/deploy.sh)，自动创建 Python 虚拟环境并注册为 `systemd` 系统守护服务。
2. **Cloudflare Tunnel 穿透**：安装 `cloudflared`，执行 `cloudflared tunnel --url http://127.0.0.1:8080` 生成安全的公网 HTTPS 访问域名（免公网 IP、免开防火墙端口）。
3. **手机 App 配置**：在 App【配置】页填入获得的 Cloudflare HTTPS 地址，一键测试连通！

*(注：如果你不想租用云服务器，也可以直接在手机 App 内填入国内大模型 API Key（如硅基流动 / 智谱），享受零服务器成本的手机端直连云端 AI 结算)*

---

## 📁 目录结构

- `app/src/main/java/com/lifeonline/app/`
  - `data/model/`: 实体模型（`UserStats`, `DailyLog`, `TaskItem`, `BuffItem`）
  - `data/local/`: 本地 Room 数据库持久化
  - `data/sensor/`: 硬件计步器与传感器数据管理
  - `data/scale/`: 《高性价比人生指南》知识库、Prompt 模版与离线引擎
  - `data/remote/`: 大模型 API 客户端 (支持 DeepSeek / 通义等) 与 DaemonClient
  - `data/repository/`: 统一数据流与经验升级算法仓库
  - `ui/components/`: 复古像素卡片、五维雷达图、进度条、战报结算弹窗
  - `ui/screens/`: 角色创建、主面板、流水账与打卡、历史档案、配置
  - `MainActivity.kt`: 应用主入口与四栏底部导航
- `server/`: Python FastAPI 云端/本地常驻 Agent 守护服务
  - `main.py`: FastAPI API 路由服务 (`/api/genesis`, `/api/settle`, `/api/status`)
  - `scale_engine.py`: 《高性价比人生指南》裁决推演引擎 (集成硅基流动大模型 + 规则兜底)
  - `archive_manager.py`: 个人第二大脑知识库持久化管理器 (`life_archive.md`)
  - `deploy.sh`: Linux (Ubuntu/Debian) 一键自动化 Systemd 安装脚本
- `DEPLOYMENT.md`: 云服务器部署与 Cloudflare Tunnel 穿透全攻略指南
