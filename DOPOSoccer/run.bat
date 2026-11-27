@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
if exist bin rmdir /s /q bin
mkdir bin
rem Cada ruta va entre comillas y con "/" para que javac acepte carpetas con espacios.
rem (no se compila src\doposoccer\test: el juego no necesita JUnit)
(
    for /r src\doposoccer\domain %%f in (*.java) do (
        set "p=%%f"
        echo "!p:\=/!"
    )
    for /r src\doposoccer\application %%f in (*.java) do (
        set "p=%%f"
        echo "!p:\=/!"
    )
    for /r src\doposoccer\infrastructure %%f in (*.java) do (
        set "p=%%f"
        echo "!p:\=/!"
    )
    for /r src\doposoccer\presentation %%f in (*.java) do (
        set "p=%%f"
        echo "!p:\=/!"
    )
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
