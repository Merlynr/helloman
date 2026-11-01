# 《人生 Online》服务端云端部署与 Cloudflare 代理指南

本文档介绍如何将《人生 Online》的 Python Agent 守护进程部署到云服务器（Linux VPS），并通过 **Cloudflare Tunnel (穿透代理)** 实现全球 24 小时随时随地免端口映射、全链路 HTTPS 安全直连。

---

## 🏗️ 架构拓扑图

```
 +------------------------+
 |   安卓手机客户端        |
 | (Life Online Android)  |
 +-----------+------------+
             | (1) 随时随地安全 HTTPS 请求
             v
 +------------------------+
 | Cloudflare Edge 边缘网络 |
 | (免费 HTTPS / DDoS防御)|
 +-----------+------------+
             | (2) Cloudflare Tunnel 穿透隧道（无需开放公网端口）
             v
 +------------------------+
 | 云服务器 (Linux VPS)   |
 |  • cloudflared 守护进程 |
 |  • FastAPI Agent 服务  |  <--> [ 个人 Markdown 智库: life_archive.md ]
 |    (127.0.0.1:8080)    |
 +-----------+------------+
             | (3) 智能推演与经验结算
             v
 +------------------------+
 | 硅基流动 / 商业大模型   |
 | (DeepSeek / Qwen 2.5)  |
 +------------------------+
```

### 为什么采用 Cloudflare Tunnel？
1. **无需公网 IP**：即使服务器处于 NAT 内网、家用宽带或云厂商轻量服务器，也能完美穿透；
2. **无需开放端口**：不需要在云服务器安全组放行 8080 端口，杜绝扫描与黑客攻击风险；
3. **原生 HTTPS 证书**：Cloudflare 自动颁发受信任的 SSL 证书，解决安卓高版本系统限制 HTTP 明文传输的问题；
4. **全球加速与高可用**：借助 Cloudflare 全球 CDN 边缘节点，延迟低且全天 24 小时常驻稳定。

---

## 🚀 方式一：云服务器 (Linux VPS) + Cloudflare Tunnel 部署（推荐）

### 第一步：通过 GitHub 在服务器上一键安装并启动

#### 选项 A：GitHub 一键远程直装（极简，直接在 VPS 上粘贴单行命令）
登录你的 Linux 服务器后，直接运行以下命令：
```bash
curl -fsSL https://raw.githubusercontent.com/Merlynr/helloman/master/server/deploy.sh | sudo bash
```

#### 选项 B：Git Clone 仓库后安装
如果你习惯先拉取完整仓库：
```bash
# 1. 克隆 GitHub 仓库
git clone https://github.com/Merlynr/helloman.git /opt/lifeonline

# 2. 进入 server 目录并执行一键安装脚本
cd /opt/lifeonline/server
sudo ./deploy.sh
```

> **自动化脚本已为你处理所有细节**：
> - 自动安装系统环境（Git, Python3, pip, venv）；
> - 自动拉取或更新 GitHub 仓库到 `/opt/lifeonline`；
> - 自动创建 Python 独立虚拟环境并安装 FastAPI 等全部依赖；
> - 自动初始化 Markdown 第二大脑智库持久化目录 `/opt/lifeonline/server/data`；
> - 自动注册 Systemd 系统级守护服务 `lifeonline.service`，实现开机自启、崩溃自动重启与常驻后台。

#### 验证本地服务运行情况
```bash
curl http://127.0.0.1:8080/
# 正常应返回: {"status":"running","service":"LifeOnline Agent Daemon",...}
```

#### 后续如何从 GitHub 更新？
当你在开发机推送了新代码后，在服务器上一键更新即可：
```bash
cd /opt/lifeonline && git pull && sudo systemctl restart lifeonline
```
或者再次执行 `sudo ./deploy.sh` 即可自动拉取更新并热重启。

---

### 第二步：安装并配置 Cloudflare Tunnel (`cloudflared`)

Cloudflare 官方提供了轻量穿透客户端 `cloudflared`。

#### 1. 安装 `cloudflared`（以 Ubuntu / Debian 为例）
```bash
# 下载官方 Debian 包
curl -L --output cloudflared.deb https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb

# 安装
sudo dpkg -i cloudflared.deb
```
*(如果是 CentOS / RHEL，可下载 rpm 包：`sudo rpm -ivh https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-x86_64.rpm`)*

---

#### 2. 方案 A：快速试用隧道（免注册、免域名、10秒启动）
如果你没有在 Cloudflare 绑定自定义域名，可直接使用 Quick Tunnel：
```bash
cloudflared tunnel --url http://127.0.0.1:8080
```
终端会输出如下临时 HTTPS 地址：
```
+--------------------------------------------------------------------------------------------+
| Your quick Tunnel has been created! Visit it at (it may take some time to be reachable):  |
| https://random-subdomain-1234.trycloudflare.com                                            |
+--------------------------------------------------------------------------------------------+
```
> 💡 将该 `https://random-subdomain-1234.trycloudflare.com` 地址直接复制到手机 App 中即可直连！

---

#### 3. 方案 B：专属固定域名常驻隧道（推荐长久使用）
如果你在 Cloudflare 托管了域名（例如 `yourdomain.com`）：

1. **登录 Cloudflare 授权**：
   ```bash
   cloudflared tunnel login
   ```
   *终端会生成一个 URL，在浏览器中打开并授权你的域名。*

2. **创建专属隧道（命名为 `lifeonline`）**：
   ```bash
   cloudflared tunnel create lifeonline
   ```
   *会生成一个隧道 UUID，并提示凭据保存在 `~/.cloudflared/<UUID>.json`。*

3. **创建配置文件 `~/.cloudflared/config.yml`**：
   ```yaml
   tunnel: <你的隧道UUID>
   credentials-file: /root/.cloudflared/<你的隧道UUID>.json

   ingress:
     - hostname: life.yourdomain.com
       service: http://127.0.0.1:8080
     - service: http_status:404
   ```

4. **将二级域名绑定到 DNS 解析**：
   ```bash
   cloudflared tunnel route dns lifeonline life.yourdomain.com
   ```

5. **安装为系统服务，实现开机后台常驻运行**：
   ```bash
   sudo cloudflared service install
   sudo systemctl enable cloudflared
   sudo systemctl start cloudflared
   ```
   现在，你的后端将通过 `https://life.yourdomain.com` 全天候 24 小时受保护访问！

---

### 第三步：手机端 App 一键连接

1. 打开手机上的 **人生 Online** App；
2. 点击底部导航栏最右侧的 **【配置】** 图标；
3. 在第一张卡片 **【C/S 模式 · 电脑/云端 Agent 守护进程】** 的输入框中输入：
   - 临时隧道模式：`https://random-subdomain-1234.trycloudflare.com`
   - 固定域名模式：`https://life.yourdomain.com`
4. 点击绿色的 **`[ 测试 Agent 服务连通性 (TEST) ]`** 按钮；
5. 看到提示 **`✓ 成功连通 Agent 服务端！`** 即代表全链路部署打通。

---

## ⚡ 方式二：纯手机直连大模型（零服务器成本）

如果你手头没有闲置的云服务器，App 原生支持**纯手机端直连云端大模型模式**：

1. 前往 **[硅基流动 (SiliconFlow)](https://cloud.siliconflow.cn/)** 或 **智谱开放平台** 注册并获取 API Key；
2. 打开手机 App 的 **【配置】** 页面；
3. 在下方 **【AI 模型服务配置（国内直连 · 全天候）】** 卡片中：
   - 点击快捷预设标签（如 `硅基流动` 或 `智谱 GLM-4-Flash`）；
   - 在 **API Key** 输入框粘贴你的 Key；
   - 点击 **`[ 测试 API 连通性 ]`** 按钮验证；
4. 提示 **`✓ 成功连通大模型接口！`** 后，手机端每次结算将直接调用云端 AI，无需启动任何服务器守护进程！

---

## 🛠️ 方式三：本地开发/内网调试（USB 调试或局域网）

### 1. USB 调试直连 (最稳妥、延迟最低)
电脑启动服务后，使用 ADB 将手机的 8080 端口转发到电脑：
```powershell
adb reverse tcp:8080 tcp:8080
```
手机端地址填写：`http://127.0.0.1:8080`

### 2. 局域网 WiFi 直连
电脑与手机连接在同一个路由器 Wi-Fi 下：
- 查询电脑内网 IP（例如 `192.168.2.114`）；
- 手机端地址填写：`http://192.168.2.114:8080`。

---

## 📋 常用运维与管理指令

在云服务器上，你可以随时使用以下指令管理后端：

| 操作 | 命令 |
| :--- | :--- |
| **查看后端运行状态** | `sudo systemctl status lifeonline` |
| **查看实时推演日志** | `sudo journalctl -u lifeonline -f` |
| **重启服务端** | `sudo systemctl restart lifeonline` |
| **停止服务** | `sudo systemctl stop lifeonline` |
| **查看 Cloudflare Tunnel 状态** | `sudo systemctl status cloudflared` |
| **查看 Cloudflare 穿透日志** | `sudo journalctl -u cloudflared -f` |

### 数据备份与个人知识库
服务端的所有结算记录与 Markdown 人生档案均保存在：
- `/opt/lifeonline/data/life_archive.md`（人类可读的 Markdown 个人智库，支持 Obsidian / Logseq 打开）
- `/opt/lifeonline/data/profile.json`（持久化角色五维属性与称号数据）

你可以定期备份 `/opt/lifeonline/data/` 目录，保障人生数据永久留存。
