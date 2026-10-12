@echo off
setlocal
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    echo Error: Java JDK was not found. Install a JDK and add it to PATH.
    exit /b 1
)

if not exist out mkdir out
javac -encoding UTF-8 -d out src\main\java\gui\LoginFrame.java
if errorlevel 1 exit /b %errorlevel%

echo Build completed successfully.
