# Android Studio Fix Script
# This script helps fix common Android Studio import errors
# Run this script from the project root directory

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Android Studio Import Errors Fix Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if we're in the right directory
if (-not (Test-Path "build.gradle.kts")) {
    Write-Host "ERROR: build.gradle.kts not found. Please run this script from the project root directory." -ForegroundColor Red
    exit 1
}

Write-Host "Step 1: Cleaning Gradle cache..." -ForegroundColor Yellow
if (Test-Path ".gradle") {
    Remove-Item -Recurse -Force ".gradle" -ErrorAction SilentlyContinue
    Write-Host "  ✓ Deleted .gradle folder" -ForegroundColor Green
} else {
    Write-Host "  ℹ .gradle folder not found (this is okay)" -ForegroundColor Gray
}

if (Test-Path "app\.gradle") {
    Remove-Item -Recurse -Force "app\.gradle" -ErrorAction SilentlyContinue
    Write-Host "  ✓ Deleted app/.gradle folder" -ForegroundColor Green
}

Write-Host ""
Write-Host "Step 2: Cleaning build directories..." -ForegroundColor Yellow
if (Test-Path "app\build") {
    Remove-Item -Recurse -Force "app\build" -ErrorAction SilentlyContinue
    Write-Host "  ✓ Deleted app/build folder" -ForegroundColor Green
}

if (Test-Path "build") {
    Remove-Item -Recurse -Force "build" -ErrorAction SilentlyContinue
    Write-Host "  ✓ Deleted build folder" -ForegroundColor Green
}

Write-Host ""
Write-Host "Step 3: Running Gradle clean..." -ForegroundColor Yellow
if (Test-Path "gradlew.bat") {
    & .\gradlew.bat clean
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ Gradle clean completed successfully" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ Gradle clean had some issues (this might be okay)" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ⚠ gradlew.bat not found. Skipping Gradle clean." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Script completed!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps in Android Studio:" -ForegroundColor Yellow
Write-Host "1. Open Android Studio" -ForegroundColor White
Write-Host "2. Go to File → Invalidate Caches... → Invalidate and Restart" -ForegroundColor White
Write-Host "3. After restart, go to File → Sync Project with Gradle Files" -ForegroundColor White
Write-Host "4. Go to Build → Rebuild Project" -ForegroundColor White
Write-Host ""
Write-Host "This should resolve most import errors!" -ForegroundColor Green

