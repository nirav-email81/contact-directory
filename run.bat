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
if "%CONTACT_ENV%"=="" set CONTACT_ENV=dev
echo Starting environment: %CONTACT_ENV%
java -Dcontact.env=%CONTACT_ENV% -cp "out;lib\sqlite-jdbc.jar;out\sqlite-jdbc.jar" ContactDirectoryApp %*

