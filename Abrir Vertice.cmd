@echo off
cd /d "%~dp0"
java -jar "Vertice.jar" %*
if errorlevel 1 pause
