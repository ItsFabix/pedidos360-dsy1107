# Muestra los grupos de consumo: offsets, lag y como se reparten las particiones.
# El LAG es cuantos mensajes le faltan por leer al grupo (0 = al dia).

$bootstrap = "kafka-1:19092"

Write-Host "========= Grupos de consumo ========="
docker exec pedidos360-kafka1 kafka-consumer-groups --bootstrap-server $bootstrap --list

foreach ($grupo in @("pedidos360-auditoria", "pedidos360-auditoria-alertas", "pedidos360-analitica")) {
    Write-Host ""
    Write-Host "========= $grupo (offsets y lag) ========="
    docker exec pedidos360-kafka1 kafka-consumer-groups --bootstrap-server $bootstrap --describe --group $grupo
    Write-Host ""
    Write-Host "--- estado ---"
    docker exec pedidos360-kafka1 kafka-consumer-groups --bootstrap-server $bootstrap --describe --group $grupo --state
}