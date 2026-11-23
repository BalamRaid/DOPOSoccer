@echo off
setlocal
cd /d "%~dp0"
if exist bin rmdir /s /q bin
mkdir bin
(
    dir /s /b src\doposoccer\domain\*.java
    dir /s /b src\doposoccer\application\*.java
    dir /s /b src\doposoccer\infrastructure\*.java
    dir /s /b src\doposoccer\presentation\*.java
) > sources.tmp
javac -encoding UTF-8 -d bin @sources.tmp
if errorlevel 1 goto :error
del sources.tmp
java -cp bin doposoccer.presentation.DOPOSoccerGUI
exit /b 0

:error
del sources.tmp
pause
exit /b 1
