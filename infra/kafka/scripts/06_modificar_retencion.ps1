# Cambia la retencion de un topico en caliente (sin recrearlo ni perder mensajes).
# Uso:   .\06_modificar_retencion.ps1                              (alertas a 5 dias)
#        .\06_modificar_retencion.ps1 -RetencionMs 259200000        (vuelve a 3 dias)
param(
    [string]$Topico = "pedidos360.pedidos.alertas",
    [long]$RetencionMs = 432000000
)

$bootstrap = "kafka-1:19092"

Write-Host "========= ANTES: $Topico ========="
docker exec pedidos360-kafka1 kafka-configs --bootstrap-server $bootstrap --describe --entity-type topics --entity-name $Topico

Write-Host ""
Write-Host "Cambiando retention.ms a $RetencionMs ..."
docker exec pedidos360-kafka1 kafka-configs --bootstrap-server $bootstrap --alter --entity-type topics --entity-name $Topico --add-config "retention.ms=$RetencionMs"

Write-Host ""
Write-Host "========= DESPUES: $Topico ========="
docker exec pedidos360-kafka1 kafka-configs --bootstrap-server $bootstrap --describe --entity-type topics --entity-name $Topico