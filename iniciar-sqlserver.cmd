@echo off
cd /d "%~dp0"
echo ExpresoFast SQL Server - Abrir http://localhost:8080
call mvn -f backend/pom.xml spring-boot:run
pause
