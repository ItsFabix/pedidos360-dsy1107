# Muestra el rol de cada nodo ZooKeeper (leader/follower) y los brokers registrados en el ensemble.
$ErrorActionPreference = 'Stop'
foreach ($zk in @('pedidos360-zk1', 'pedidos360-zk2', 'pedidos360-zk3')) {
  Write-Host "========= $zk ========="
  docker exec $zk bash -c 'exec 3<>/dev/tcp/localhost/2181; echo srvr >&3; cat <&3' | Select-String 'Mode|Zookeeper version'
}
Write-Host '========= Brokers registrados en ZooKeeper (/brokers/ids) ========='
docker exec pedidos360-kafka1 zookeeper-shell zookeeper-1:2181 ls /brokers/ids
Write-Host '========= Broker controlador del cluster (/controller) ========='
docker exec pedidos360-kafka1 zookeeper-shell zookeeper-1:2181 get /controller