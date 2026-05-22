@echo off
cd /d "%~dp0"
setlocal
set LIB=lib
set JAR=%LIB%\sqlite-jdbc.jar
set URL=https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.34.0/sqlite-jdbc-3.34.0.jar

if not exist %LIB% mkdir %LIB%

if not exist %JAR% (
    echo Downloading SQLite JDBC driver...
    powershell -NoProfile -Command ^
        "Invoke-WebRequest -Uri '%URL%' -OutFile '%JAR%' -UseBasicParsing"
    if %ERRORLEVEL% NEQ 0 (
        echo Download failed. Install Maven and run: mvn compile
        exit /b 1
    )
)

if not exist out mkdir out
copy /Y "%JAR%" "out\sqlite-jdbc.jar" >nul
javac -encoding UTF-8 -cp "%JAR%" -d out src\*.java
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
if not exist out\config mkdir out\config
xcopy /E /I /Y config\*.properties out\config\ >nul
echo Compiled successfully. Run: run-dev.bat or run-prod.bat
