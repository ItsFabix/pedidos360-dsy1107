#!/usr/bin/env bash
# Muestra el estado de los contenedores y del clúster desde ambos nodos.
set -euo pipefail
cd "$(dirname "$0")"

docker compose -f docker-compose.cluster.yml ps
echo; echo "=== cluster_status desde rabbit1 ==="
docker exec pedidos360-rabbit1 rabbitmqctl cluster_status
echo; echo "=== cluster_status desde rabbit2 ==="
docker exec pedidos360-rabbit2 rabbitmqctl cluster_status