$ErrorActionPreference = "Stop"
Set-Location "$PSScriptRoot\..\frontend-angular"
if (-not (Test-Path "node_modules")) {
    Write-Host "node_modules no existe. Ejecutando npm install..."
    npm install
}
npx ng serve
