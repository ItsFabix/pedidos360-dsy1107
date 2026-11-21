# Levanta el broker caido, espera a que este healthy y comprueba que el ISR vuelve a 3.
# Uso:   .\08_recuperar_broker.ps1
#        .\08_recuperar_broker.ps1 -Broker 2
param(
    [ValidateSet("1", "2", "3")]
    [string]$Broker = "3"
)

$contenedor = "pedidos360-kafka$Broker"

Write-Host "Iniciando $contenedor ..."
docker start $contenedor | Out-Null

Write-Host "Esperando que quede healthy..."
for ($i = 1; $i -le 30; $i++) {
    $estado = docker inspect --format '{{.State.Health.Status}}' $contenedor
    Write-Host "  intento ${i}: $estado"
    if ($estado -eq "healthy") { break }
    Start-Sleep -Seconds 5
}

Write-Host "Esperando 15 segundos a que las replicas se pongan al dia..."
Start-Sleep -Seconds 15

Write-Host ""
Write-Host "========= Devolviendo el liderazgo a las replicas preferidas ========="
docker exec pedidos360-kafka1 kafka-leader-election --bootstrap-server kafka-1:19092 --election-type PREFERRED --all-topic-partitions

Write-Host ""
Write-Host "========= Estado de los topicos (Isr debe volver a 1,2,3) ========="
docker exec pedidos360-kafka1 kafka-topics --bootstrap-server kafka-1:19092 --describe --topic pedidos360.pedidos.eventos
docker exec pedidos360-kafka1 kafka-topics --bootstrap-server kafka-1:19092 --describe --topic pedidos360.pedidos.alertas

Write-Host ""
Write-Host "========= Particiones sub-replicadas (debe salir vacio) ========="
docker exec pedidos360-kafka1 kafka-topics --bootstrap-server kafka-1:19092 --describe --under-replicated-partitions