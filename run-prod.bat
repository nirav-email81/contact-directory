@echo off
cd /d "%~dp0"
set CONTACT_ENV=prod
call run.bat %*
