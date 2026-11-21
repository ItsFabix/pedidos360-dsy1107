# Detiene Kafka, ZooKeeper y Kafka UI SIN borrar contenedores ni datos.
# Los topicos y mensajes se conservan; para volver a levantar todo usa 01_iniciar_kafka.ps1.
# (No uses "docker compose down -v": eso si borra los datos.)

Write-Host "Deteniendo Kafka UI, brokers y ZooKeeper (sin borrar datos)..."
docker stop pedidos360-kafka-ui pedidos360-kafka1 pedidos360-kafka2 pedidos360-kafka3 pedidos360-zk1 pedidos360-zk2 pedidos360-zk3

Write-Host ""
Write-Host "Listo. Para volver a levantar: .\01_iniciar_kafka.ps1"