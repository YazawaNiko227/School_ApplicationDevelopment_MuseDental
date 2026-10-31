@echo off
setlocal
cd /d "%~dp0"

if not defined MUSE_DB_USER (
    echo Error: Set the MUSE_DB_USER environment variable before running the application.
    exit /b 1
)
if not defined MUSE_DB_PASSWORD (
    echo Error: Set the MUSE_DB_PASSWORD environment variable before running the application.
    exit /b 1
)

call build.bat
if errorlevel 1 exit /b %errorlevel%

java -cp "out;lib\*" gui.DangNhapFrame
