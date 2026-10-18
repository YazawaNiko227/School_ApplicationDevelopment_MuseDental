@echo off
setlocal
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    echo Error: Java JDK was not found. Install a JDK and add it to PATH.
    exit /b 1
)

if not exist out mkdir out
set "SOURCE_LIST=%TEMP%\MuseDental_sources_%RANDOM%.txt"
(for /r "src\main\java" %%F in (*.java) do @echo %%F) > "%SOURCE_LIST%"
javac -encoding UTF-8 -sourcepath src\main\java -d out @"%SOURCE_LIST%"
set "BUILD_RESULT=%ERRORLEVEL%"
del "%SOURCE_LIST%" >nul 2>nul
if not "%BUILD_RESULT%"=="0" exit /b %BUILD_RESULT%

xcopy /E /I /Y "src\main\resources\*" "out\" >nul
if errorlevel 1 (
    echo Error: Failed to copy application resources into the build folder.
    exit /b 1
)

echo Build completed successfully.
