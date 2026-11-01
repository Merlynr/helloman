#!/usr/bin/env bash
# ==============================================================================
# 人生 Online - Linux 云服务器一键自动化部署脚本 (Ubuntu / Debian)
# ==============================================================================
set -e

APP_DIR="/opt/lifeonline"
SERVICE_NAME="lifeonline"

echo -e "\033[32m[1/5] 安装系统依赖 (Python3, venv, curl)...\033[0m"
sudo apt-get update -y
sudo apt-get install -y python3 python3-pip python3-venv curl

echo -e "\033[32m[2/5] 准备应用目录: ${APP_DIR}...\033[0m"
sudo mkdir -p ${APP_DIR}
sudo cp -r ./* ${APP_DIR}/ || true
cd ${APP_DIR}

echo -e "\033[32m[3/5] 构建 Python 独立虚拟环境与安装依赖...\033[0m"
if [ ! -d "venv" ]; then
    python3 -m venv venv
fi
./venv/bin/pip install --upgrade pip
./venv/bin/pip install -r requirements.txt

echo -e "\033[32m[4/5] 注册 Systemd 系统级后台常驻守护服务...\033[0m"
cat <<EOF | sudo tee /etc/systemd/system/${SERVICE_NAME}.service
[Unit]
Description=LifeOnline Agent Daemon Service
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=${APP_DIR}
ExecStart=${APP_DIR}/venv/bin/python -m uvicorn main:app --host 0.0.0.0 --port 8080
Restart=always
RestartSec=5
Environment=PYTHONUNBUFFERED=1

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable ${SERVICE_NAME}
sudo systemctl restart ${SERVICE_NAME}

echo -e "\033[32m[5/5] 验证服务状态...\033[0m"
sleep 2
if sudo systemctl is-active --quiet ${SERVICE_NAME}; then
    echo -e "\033[32m========================================================\033[0m"
    echo -e "\033[32m[√] 人生 Online 服务端部署成功并已常驻后台！\033[0m"
    echo -e "• 本地测试: curl http://127.0.0.1:8080/"
    echo -e "• 查看状态: sudo systemctl status ${SERVICE_NAME}"
    echo -e "• 查看日志: sudo journalctl -u ${SERVICE_NAME} -f"
    echo -e "\033[32m========================================================\033[0m"
else
    echo -e "\033[31m[X] 服务启动异常，请检查日志: sudo journalctl -u ${SERVICE_NAME} -n 30\033[0m"
    exit 1
fi
