#!/usr/bin/env bash
# ==============================================================================
# 人生 Online (Life Online) - Linux 云服务器一键自动化部署与更新脚本
# 支持: GitHub 直装 (curl 管道) / 本地运行 / 后续一键更新
# 仓库: https://github.com/Merlynr/helloman
# ==============================================================================
set -e

REPO_URL="https://github.com/Merlynr/helloman.git"
INSTALL_ROOT="/opt/lifeonline"
SERVER_DIR="${INSTALL_ROOT}/server"
SERVICE_NAME="lifeonline"

# 1. 检查 root 权限
if [ "$EUID" -ne 0 ]; then
  echo -e "\033[31m[!] 请使用 root 权限运行此脚本: sudo bash $0\033[0m"
  exit 1
fi

echo -e "\033[32m========================================================\033[0m"
echo -e "\033[32m  《人生 Online》Agent 服务端自动化安装与部署\033[0m"
echo -e "\033[32m========================================================\033[0m"

# 2. 安装系统基础依赖 (免交互静默安装)
echo -e "\033[34m[1/6] 检查并安装系统依赖 (Git, Python3, venv, curl)...\033[0m"
export DEBIAN_FRONTEND=noninteractive
export NEEDRESTART_MODE=a
if command -v apt-get >/dev/null 2>&1; then
    apt-get update -y
    apt-get install -y -o Dpkg::Options::="--force-confdef" -o Dpkg::Options::="--force-confold" git python3 python3-pip python3-venv curl
elif command -v dnf >/dev/null 2>&1; then
    dnf install -y git python3 python3-pip curl
elif command -v yum >/dev/null 2>&1; then
    yum install -y git python3 python3-pip curl
else
    echo -e "\033[33m[!] 未检测到标准包管理器，请确保已安装 git, python3 及 curl。\033[0m"
fi

# 3. 从 GitHub 下载或更新项目源码
echo -e "\033[34m[2/6] 同步 GitHub 项目源码至 ${INSTALL_ROOT}...\033[0m"
if [ -d "${INSTALL_ROOT}/.git" ]; then
    echo -e "--> 检测到已有仓库，执行 git pull 获取最新代码..."
    git config --global --add safe.directory "${INSTALL_ROOT}"
    cd "${INSTALL_ROOT}"
    git pull origin master || true
else
    echo -e "--> 首次安装，克隆仓库: ${REPO_URL}..."
    mkdir -p "${INSTALL_ROOT}"
    # 如果当前目录就在已克隆的仓库中，优先就地迁移
    if [ -f "main.py" ] && [ -f "scale_engine.py" ] && [ -d "../../.git" ]; then
        cd ../..
        cp -r ./* "${INSTALL_ROOT}/" 2>/dev/null || true
    else
        git clone "${REPO_URL}" "${INSTALL_ROOT}"
    fi
fi

# 4. 配置 Python 独立虚拟环境与安装依赖
echo -e "\033[34m[3/6] 配置 Python 虚拟环境与依赖包...\033[0m"
cd "${SERVER_DIR}"
if [ ! -d "venv" ]; then
    python3 -m venv venv
fi
./venv/bin/pip install --upgrade pip -i https://pypi.tuna.tsinghua.edu.cn/simple || ./venv/bin/pip install --upgrade pip
./venv/bin/pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple || ./venv/bin/pip install -r requirements.txt

# 5. 准备持久化数据目录
echo -e "\033[34m[4/6] 初始化持久化第二大脑知识库目录...\033[0m"
mkdir -p "${SERVER_DIR}/data"

# 6. 配置并注册 Systemd 系统后台常驻服务
echo -e "\033[34m[5/6] 注册 Systemd 系统后台守护服务 (${SERVICE_NAME})...\033[0m"
cat <<EOF > /etc/systemd/system/${SERVICE_NAME}.service
[Unit]
Description=LifeOnline Agent Daemon Service
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=${SERVER_DIR}
ExecStart=${SERVER_DIR}/venv/bin/python -m uvicorn main:app --host 0.0.0.0 --port 8080
Restart=always
RestartSec=5
Environment=PYTHONUNBUFFERED=1

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable ${SERVICE_NAME}
systemctl restart ${SERVICE_NAME}

# 7. 连通性测试与完成报告
echo -e "\033[34m[6/6] 验证本地运行状态...\033[0m"
sleep 2

if systemctl is-active --quiet ${SERVICE_NAME}; then
    echo -e "\033[32m========================================================\033[0m"
    echo -e "\033[32m  [√] 人生 Online 服务端部署成功并已常驻后台！\033[0m"
    echo -e "========================================================"
    echo -e "• 本地测试: curl http://127.0.0.1:8080/"
    echo -e "• 查看状态: systemctl status ${SERVICE_NAME}"
    echo -e "• 实时日志: journalctl -u ${SERVICE_NAME} -f"
    echo -e "• 知识档案: ${SERVER_DIR}/data/life_archive.md"
    echo -e "--------------------------------------------------------"
    echo -e "\033[33m【下一步：配置 Cloudflare 穿透，让手机随时随地直连】\033[0m"
    echo -e "1. 安装 cloudflared (若未安装):"
    echo -e "   curl -L --output /tmp/cloudflared.deb https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb && dpkg -i /tmp/cloudflared.deb"
    echo -e "2. 启动穿透（免域名、免公网IP、10秒生成公网 HTTPS）:"
    echo -e "   \033[36mcloudflared tunnel --url http://127.0.0.1:8080\033[0m"
    echo -e "3. 复制终端中显示的 https://xxxx.trycloudflare.com 到手机 App 配置中即可！"
    echo -e "========================================================"
else
    echo -e "\033[31m[X] 服务启动异常，请检查日志: journalctl -u ${SERVICE_NAME} -n 30\033[0m"
    exit 1
fi
