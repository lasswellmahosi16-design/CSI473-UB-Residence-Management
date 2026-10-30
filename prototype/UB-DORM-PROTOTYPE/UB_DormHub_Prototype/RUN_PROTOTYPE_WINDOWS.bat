@echo off
setlocal
cd /d "%~dp0"
echo ========================================
echo      UB-DormHub - CSI473 Prototype
 echo ========================================
echo.
echo Starting Java 21 application in a new PowerShell window...
start "UB-DormHub Server" powershell.exe -NoProfile -ExecutionPolicy Bypass -NoExit -File "%~dp0scripts\run.ps1"
echo Opening http://localhost:8765 shortly...
timeout /t 5 /nobreak >nul
start "" "http://localhost:8765"
echo.
echo If the page does not load, check the Java server window.
echo Close the Java server window to stop the prototype.
endlocal
