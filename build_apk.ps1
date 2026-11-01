# Android APK Build and Deploy Script
$env:ANDROID_HOME = "D:\Scoop\apps\android-clt\current"

Write-Host "==============================================" -ForegroundColor Green
Write-Host "/// Life Online - APK Build and Install ///" -ForegroundColor Green
Write-Host "• ANDROID_HOME: $env:ANDROID_HOME" -ForegroundColor Gray
Write-Host "==============================================" -ForegroundColor Green

# 1. Compile APK
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    Remove-Item -Force $apkPath
}

Write-Host "Running Gradle assembleDebug..." -ForegroundColor Cyan
gradle assembleDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Gradle build failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit 1
}

if (Test-Path $apkPath) {
    Write-Host "==============================================" -ForegroundColor Green
    Write-Host "[OK] APK build successful!" -ForegroundColor Green
    Write-Host "APK Path: $apkPath" -ForegroundColor Cyan
    Write-Host "==============================================" -ForegroundColor Green

    # 2. Check connected devices and install
    $devices = adb devices | Select-String "device$"
    if ($devices.Count -gt 0) {
        Write-Host "Connected device found, pushing and installing APK..." -ForegroundColor Yellow
        adb push $apkPath /data/local/tmp/app.apk
        adb shell pm install -r -d -t -g /data/local/tmp/app.apk
        Write-Host "[OK] Installation command sent! Launching app..." -ForegroundColor Green
        adb shell am start -n com.lifeonline.app/.MainActivity
    } else {
        Write-Host "Notice: No connected USB device found." -ForegroundColor Gray
    }
} else {
    Write-Host "[ERROR] Output APK not found." -ForegroundColor Red
}
