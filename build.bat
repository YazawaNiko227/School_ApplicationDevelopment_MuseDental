@echo off
setlocal
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    echo Error: Java JDK was not found. Install a JDK and add it to PATH.
    exit /b 1
)

if not exist out mkdir out
<<<<<<< HEAD

javac -encoding UTF-8 -cp "lib/*" -sourcepath src\main\java -d out src\main\java\gui\LoginFrame.java src\main\java\gui\MainFrame.java src\main\java\gui\PatientSearchPanel.java src\main\java\gui\EmployeeSearchPanel.java src\main\java\gui\AppointmentPanel.java src\main\java\gui\EMRSearchPanel.java

if errorlevel 1 exit /b %errorlevel%
=======
set "SOURCE_LIST=%TEMP%\MuseDental_sources_%RANDOM%.txt"
(for /r "src\main\java" %%F in (*.java) do @echo %%F) > "%SOURCE_LIST%"
javac -encoding UTF-8 -sourcepath src\main\java -d out @"%SOURCE_LIST%"
set "BUILD_RESULT=%ERRORLEVEL%"
del "%SOURCE_LIST%" >nul 2>nul
if not "%BUILD_RESULT%"=="0" exit /b %BUILD_RESULT%
>>>>>>> main

xcopy /E /I /Y "src\main\resources\*" "out\" >nul
if errorlevel 1 (
    echo Error: Failed to copy application resources into the build folder.
    exit /b 1
)

echo Build completed successfully.