# iniciar.ps1
# Abre um terminal separado para cada um dos tres servicos do projeto:
#  - User Service (Spring Boot, porta 8081)
#  - Email Service (Spring Boot, porta 8082)
#  - Frontend (Node.js/Express, porta 3000)
#
# Execute este script a partir da raiz do repositorio:
#   .\iniciar.ps1

$root = $PSScriptRoot

Write-Host "Iniciando User Service (porta 8081)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "cd '$root\user-service'; .\mvnw.cmd spring-boot:run"
)

Write-Host "Iniciando Email Service (porta 8082)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "cd '$root\email-service'; .\mvnw.cmd spring-boot:run"
)

Write-Host "Aguardando os servicos Java subirem antes de iniciar o frontend..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

Write-Host "Iniciando Frontend (porta 3000)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "cd '$root\frontend'; if (-not (Test-Path 'node_modules')) { npm install }; npm start"
)

Write-Host ""
Write-Host "Os tres servicos estao sendo iniciados em terminais separados." -ForegroundColor Green
Write-Host "Acesse http://localhost:3000 quando todos estiverem prontos." -ForegroundColor Green
