@echo off
set MUSE_DB_PASSWORD=123456789
setlocal
cd /d "%~dp0"

call build.bat
if errorlevel 1 exit /b %errorlevel%

java -cp "out;lib\*" gui.DangNhapFrame
