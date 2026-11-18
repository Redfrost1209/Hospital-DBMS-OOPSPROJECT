# PowerShell Build Script
Write-Host "Compiling MedCare Hospital Management System..." -ForegroundColor Cyan

if (-not (Test-Path "out")) {
    New-Item -ItemType Directory -Path "out" | Out-Null
}

$sources = (Get-ChildItem -Path "src" -Filter *.java -Recurse).FullName
javac -cp "lib/*" -d "out" $sources

if ($LASTEXITCODE -eq 0) {
    Write-Host "[SUCCESS] Compilation succeeded! Run .\run.ps1 to start." -ForegroundColor Green
} else {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
}
