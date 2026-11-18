# PowerShell Run Script
if (-not (Test-Path "out")) {
    & ".\build.ps1"
}

Write-Host "Launching MedCare Hospital Management System..." -ForegroundColor Green
java -cp "out;lib/*" com.hospital.Main
