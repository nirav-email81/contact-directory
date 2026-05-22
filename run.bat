@echo off
cd /d "%~dp0"
if not exist out\ContactDirectoryApp.class (
    call compile.bat
    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
)
if not exist lib\sqlite-jdbc.jar (
    call compile.bat
    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
)
java -cp "out;lib\sqlite-jdbc.jar;out\sqlite-jdbc.jar" ContactDirectoryApp %*
