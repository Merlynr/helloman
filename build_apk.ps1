# 纯命令行 Android APK 编译与部署脚本
$env:ANDROID_HOME = "D:\Scoop\apps\android-clt\current"
$sdkManager = "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat"

Write-Host "==============================================" -ForegroundColor Green
Write-Host "/// 人生 Online · 纯命令行 APK 构建部署 ///" -ForegroundColor Green
Write-Host "• ANDROID_HOME: $env:ANDROID_HOME" -ForegroundColor Gray
Write-Host "==============================================" -ForegroundColor Green

# 1. 检查必要的 SDK 组件 (android-35 与 build-tools)
if (-not (Test-Path "$env:ANDROID_HOME\platforms\android-35")) {
    Write-Host "正在下载安装 Android 35 编译平台..." -ForegroundColor Yellow
    & $sdkManager "platforms;android-35" "build-tools;35.0.0"
}

# 2. 编译 APK
Write-Host "开始执行 Gradle 构建 assembleDebug..." -ForegroundColor Cyan
gradle assembleDebug

$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    Write-Host "==============================================" -ForegroundColor Green
    Write-Host "[√] APK 编译成功！" -ForegroundColor Green
    Write-Host "APK 路径: $apkPath" -ForegroundColor Cyan
    Write-Host "==============================================" -ForegroundColor Green

    # 3. 检查是否有设备连接，若有则自动安装
    $devices = adb devices | Select-String "device$"
    if ($devices.Count -gt 0) {
        Write-Host "检测到已连接手机，正在自动安装 APK 到手机..." -ForegroundColor Yellow
        adb install -r $apkPath
        Write-Host "[√] 手机安装完成！正在启动应用..." -ForegroundColor Green
        adb shell am start -n com.lifeonline.app/.MainActivity
    } else {
        Write-Host "提示：当前无 USB 调试设备连接。插上手机后执行 .\sync_phone.ps1 即可反代端口，或通过 adb install 安装该 APK。" -ForegroundColor Gray
    }
} else {
    Write-Host "[X] 未找到编译输出的 APK，请查看上方 Gradle 日志。" -ForegroundColor Red
}
