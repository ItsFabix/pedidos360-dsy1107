# Simula la caida de un broker (por defecto el 3) y muestra como reacciona el cluster.
# Con 2 de 3 brokers vivos y min.insync.replicas=2, los topicos siguen funcionando.
# Uso:   .\07_simular_caida_broker.ps1
#        .\07_simular_caida_broker.ps1 -Broker 2
param(
    [ValidateSet("1", "2", "3")]
    [string]$Broker = "3"
)

$caido = "pedidos360-kafka$Broker"
# Para consultar usamos un broker que siga vivo.
if ($Broker -eq "1") { $vivo = "2" } else { $vivo = "1" }
$contenedorVivo = "pedidos360-kafka$vivo"
$bootstrapVivo = "kafka-${vivo}:19092"

Write-Host "Deteniendo $caido ..."
docker stop $caido | Out-Null
Write-Host "Esperando 20 segundos a que el cluster detecte la caida..."
Start-Sleep -Seconds 20

Write-Host ""
Write-Host "========= Brokers vivos en ZooKeeper ========="
docker exec pedidos360-zk1 zookeeper-shell zookeeper-1:2181 ls /brokers/ids 2>$null | Select-String "\["

Write-Host ""
Write-Host "========= Topicos con un broker caido (mira Leader e Isr) ========="
docker exec $contenedorVivo kafka-topics --bootstrap-server $bootstrapVivo --describe --topic pedidos360.pedidos.eventos
docker exec $contenedorVivo kafka-topics --bootstrap-server $bootstrapVivo --describe --topic pedidos360.pedidos.alertas

Write-Host ""
Write-Host "========= Particiones sub-replicadas (falta una replica en ISR) ========="
docker exec $contenedorVivo kafka-topics --bootstrap-server $bootstrapVivo --describe --under-replicated-partitions

Write-Host ""
Write-Host "========= Particiones sin lider (debe salir vacio) ========="
docker exec $contenedorVivo kafka-topics --bootstrap-server $bootstrapVivo --describe --unavailable-partitions

Write-Host ""
Write-Host "Para recuperar el broker: .\08_recuperar_broker.ps1 -Broker $Broker"