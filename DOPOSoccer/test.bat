@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
if exist bin rmdir /s /q bin
mkdir bin
rem Cada ruta va entre comillas y con "/" para que javac acepte carpetas con espacios.
(
    for /r src %%f in (*.java) do (
        set "p=%%f"
        echo "!p:\=/!"
    )
) > sources.tmp
javac -encoding UTF-8 -cp lib\junit-platform-console-standalone-1.10.2.jar -d bin @sources.tmp
if errorlevel 1 goto :error
del sources.tmp
java -jar lib\junit-platform-console-standalone-1.10.2.jar execute --class-path bin --select-package doposoccer.test --details=summary
pause
exit /b 0

:error
del sources.tmp
pause
exit /b 1
