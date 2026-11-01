# 一键反向代理与手机连接脚本 (ADB Reverse Proxy)
Write-Host "==============================================" -ForegroundColor Green
Write-Host "/// 人生 Online · 安卓手机联调助手 ///" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green

Write-Host "1. 检测已连接的安卓设备..." -ForegroundColor Yellow
$devices = adb devices | Select-String "device$"

if ($devices.Count -eq 0) {
    Write-Host "[!] 未检测到连接的安卓真机！" -ForegroundColor Red
    Write-Host "请按以下步骤操作：" -ForegroundColor Gray
    Write-Host "  a. 手机打开「设置 -> 开发者选项 -> 开启 USB 调试」" -ForegroundColor Gray
    Write-Host "  b. 用 USB 数据线将手机连接到电脑" -ForegroundColor Gray
    Write-Host "  c. 手机弹出提示时勾选「一律允许此计算机进行调试」" -ForegroundColor Gray
} else {
    Write-Host "[√] 成功检测到设备: $devices" -ForegroundColor Green
    Write-Host "2. 建立端口反向代理 (8080 -> 8080)..." -ForegroundColor Yellow
    adb reverse tcp:8080 tcp:8080
    Write-Host "[√] 反向代理建立成功！" -ForegroundColor Green
    Write-Host "手机端可以直接访问 http://127.0.0.1:8080 无缝连接电脑 Agent！" -ForegroundColor Cyan
}
