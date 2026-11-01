# 启动人生 Online 电脑端 Agent 守护服务 (LifeOnline Daemon)
$pythonExe = "D:\Scoop\apps\python\current\python.exe"
if (-not (Test-Path $pythonExe)) {
    $pythonExe = "python"
}

Write-Host "==============================================" -ForegroundColor Green
Write-Host "/// 启动 人生 Online Agent 守护进程 ///" -ForegroundColor Green
Write-Host "• 标尺体系: 《高性价比人生指南》(HowToLiveBetter)" -ForegroundColor Gray
Write-Host "• 监听端口: http://127.0.0.1:8080" -ForegroundColor Cyan
Write-Host "• 档案目录: server/data/ (Master Brain Archive)" -ForegroundColor Gray
Write-Host "==============================================" -ForegroundColor Green

& $pythonExe -m uvicorn main:app --host 0.0.0.0 --port 8080 --app-dir server --reload
