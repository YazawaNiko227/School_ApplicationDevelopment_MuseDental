@echo off
setlocal
cd /d "%~dp0"

call build.bat
if errorlevel 1 exit /b %errorlevel%

<<<<<<< HEAD
java -cp "out;lib/*" gui.LoginFrame
=======
java -cp "out;lib\*" gui.DangNhapFrame
>>>>>>> main
