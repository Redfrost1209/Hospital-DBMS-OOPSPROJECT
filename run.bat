@echo off
if not exist out (
    echo Building project first...
    call build.bat
)

echo Starting MedCare Hospital Management System...
java -cp "out;lib/*" com.hospital.Main
pause
