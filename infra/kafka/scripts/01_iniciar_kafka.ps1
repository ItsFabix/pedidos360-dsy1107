# Levanta 3 ZooKeeper + 3 brokers, espera que los brokers esten healthy y luego levanta Kafka UI.
$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
  docker compose -f docker-compose.kafka.yml config --quiet
  if ($LASTEXITCODE -ne 0) { throw 'docker-compose.kafka.yml no es valido.' }

  docker compose -f docker-compose.kafka.yml up -d
  if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar ZooKeeper/Kafka.' }

  Write-Host 'Esperando que los 3 brokers queden healthy (puede tardar 1-2 minutos)...'
  $listos = $false
  for ($i = 1; $i -le 30; $i++) {
    $estados = docker inspect --format '{{.State.Health.Status}}' pedidos360-kafka1 pedidos360-kafka2 pedidos360-kafka3
    $sanos = @($estados | Where-Object { $_ -eq 'healthy' }).Count
    Write-Host "  intento $i : $sanos de 3 brokers healthy"
    if ($sanos -eq 3) { $listos = $true; break }
    Start-Sleep -Seconds 6
  }
  if (-not $listos) { throw 'Los brokers no quedaron healthy. Revisar: docker compose -f docker-compose.kafka.yml logs --tail=50 kafka-1' }

  docker compose -f docker-compose.kafka-ui.yml up -d
  if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar Kafka UI.' }

  docker compose -f docker-compose.kafka.yml ps
  Write-Host 'Kafka UI: http://localhost:8090'
} finally { Pop-Location }