@echo off
setlocal
cd /d "%~dp0"

call build.bat
if errorlevel 1 exit /b %errorlevel%

java -cp "out;lib/*" gui.LoginFrame
