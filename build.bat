@echo off
echo ========================================================
echo  Compiling MedCare Hospital Management System...
echo ========================================================

if not exist out mkdir out

dir /s /B src\*.java > sources.txt
javac -cp "lib/*" -d out @sources.txt
del sources.txt

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation completed successfully!
    echo Run 'run.bat' to start the application.
) else (
    echo [ERROR] Compilation failed.
)
pause
