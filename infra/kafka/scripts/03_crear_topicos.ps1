# Crea los topicos de Pedidos360 con 3 particiones, RF=3 y min.insync.replicas=2.
# La retencion cambia segun el uso de cada flujo. --if-not-exists NO modifica un topico que ya existe.
$ErrorActionPreference = 'Stop'
$bootstrap = 'kafka-1:19092'
$topicos = @(
  @{ nombre = 'pedidos360.pedidos.eventos';     retencionMs = '604800000'  },  # 7 dias: historial para auditoria y reprocesamiento
  @{ nombre = 'pedidos360.pedidos.alertas';     retencionMs = '259200000'  },  # 3 dias: avisos operativos de corta vigencia
  @{ nombre = 'pedidos360.pedidos.eventos.DLT'; retencionMs = '1209600000' },  # 14 dias: tiempo para revisar eventos fallidos
  @{ nombre = 'pedidos360.pedidos.alertas.DLT'; retencionMs = '1209600000' }
)
foreach ($t in $topicos) {
  Write-Host "Creando/verificando $($t.nombre) ..."
  docker exec pedidos360-kafka1 kafka-topics --bootstrap-server $bootstrap --create --if-not-exists `
    --topic $t.nombre --partitions 3 --replication-factor 3 `
    --config min.insync.replicas=2 --config cleanup.policy=delete --config "retention.ms=$($t.retencionMs)"
  if ($LASTEXITCODE -ne 0) { throw "No se pudo crear $($t.nombre)" }
}
docker exec pedidos360-kafka1 kafka-topics --bootstrap-server $bootstrap --list