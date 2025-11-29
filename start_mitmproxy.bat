@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Nebula mitmproxy One-Click Launcher
echo ========================================
echo.

:: Get local IP address (prioritize physical network adapters)
set LOCAL_IP=
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /R /C:"IPv4.*Address" ^| findstr /V "172\. 169\. 127\."') do (
    set LOCAL_IP=%%a
    set LOCAL_IP=!LOCAL_IP: =!
    if not "!LOCAL_IP!"=="" goto :ip_found
)

:ip_found
:: If no suitable IP found, try to get the first non-loopback IP
if "!LOCAL_IP!"=="" (
    for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /R /C:"IPv4.*Address" ^| findstr /V "127\."') do (
        set LOCAL_IP=%%a
        set LOCAL_IP=!LOCAL_IP: =!
        if not "!LOCAL_IP!"=="" goto :ip_found2
    )
)

:ip_found2
:: Use localhost if no IP found
if "!LOCAL_IP!"=="" set LOCAL_IP=localhost

echo Local IP address: !LOCAL_IP!
echo.

:: Kill any existing mitmproxy processes
echo Stopping any existing mitmproxy processes...
taskkill /f /im mitmdump.exe 2>nul
taskkill /f /im mitmproxy.exe 2>nul
taskkill /f /im mitmweb.exe 2>nul
timeout /t 2 /nobreak >nul

:: Enable system proxy with simpler method
echo Enabling system proxy...
reg add "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings" /v ProxyEnable /t REG_DWORD /d 1 /f >nul 2>&1
reg add "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings" /v ProxyServer /t REG_SZ /d "!LOCAL_IP!:8080" /f >nul 2>&1

:: Force proxy settings refresh using multiple methods
echo Refreshing system proxy settings...
rundll32.exe user32.dll,UpdatePerUserSystemParameters
powershell -Command "& {try { $shell = New-Object -ComObject Shell.Application; $shell.SetSystemProxy '!LOCAL_IP!', '8080'; Write-Host 'Proxy updated via Shell API' } catch { Write-Host 'Shell API failed, using registry method' } }" 2>nul

echo Proxy set to: !LOCAL_IP!:8080
echo.

:: Check if Nebula server is running
echo Checking Nebula server status...
curl -s http://localhost:80/common/config >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Nebula server is running
) else (
    echo [WARNING] Cannot connect to Nebula server, please ensure the server is started
    echo Press any key to continue starting mitmproxy, or press Ctrl+C to cancel...
    pause >nul
)

echo.
echo Starting mitmproxy...
echo ========================================
echo Press Ctrl+C to stop mitmproxy and auto-disable proxy
echo ========================================
echo.

:: Start mitmdump with alternative port if 8080 is occupied
echo Trying to start mitmproxy on port 8080...
mitmweb -s scripts/nebula_mitmproxy.py --set confdir=~/.mitmproxy --mode regular@8080 2>nul
if %errorlevel% neq 0 (
    echo Port 8080 is occupied, trying alternative port 8081...
    echo Updating proxy settings to use port 8081...
    reg add "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings" /v ProxyServer /t REG_SZ /d "!LOCAL_IP!:8081" /f >nul 2>&1
    rundll32.exe user32.dll,UpdatePerUserSystemParameters
    
    echo Starting mitmproxy on port 8081...
    mitmweb -s scripts/nebula_mitmproxy.py --set confdir=~/.mitmproxy --mode regular@8081
)

:: After user presses Ctrl+C, automatically execute the following
echo.
echo Disabling system proxy...
reg add "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings" /v ProxyEnable /t REG_DWORD /d 0 /f >nul 2>&1
rundll32.exe user32.dll,UpdatePerUserSystemParameters
echo System proxy disabled
echo.
echo Thank you for using Nebula mitmproxy tool!
pause