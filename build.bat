@echo off
cd /d "%~dp0"

if not exist out mkdir out

javac -encoding UTF-8 -cp "lib\*" -sourcepath src\main\java -d out src\main\java\gui\DangNhapFrame.java
if errorlevel 1 exit /b 1

xcopy /E /I /Y "src\main\resources\*" "out\" >nul

echo Build completed successfully.