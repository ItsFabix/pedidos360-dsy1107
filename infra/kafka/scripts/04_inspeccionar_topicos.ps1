# Muestra particiones, lider, replicas e ISR de cada topico y su configuracion efectiva.
$ErrorActionPreference = 'Stop'
$bootstrap = 'kafka-1:19092'
foreach ($topico in @('pedidos360.pedidos.eventos', 'pedidos360.pedidos.alertas')) {
  Write-Host "========= $topico ========="
  docker exec pedidos360-kafka1 kafka-topics --bootstrap-server $bootstrap --describe --topic $topico
  docker exec pedidos360-kafka1 kafka-configs --bootstrap-server $bootstrap --describe --entity-type topics --entity-name $topico
}