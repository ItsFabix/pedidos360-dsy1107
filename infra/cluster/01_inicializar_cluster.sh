#!/usr/bin/env bash
# Levanta los dos nodos y une rabbit2 a rabbit1. Ejecutar desde infra/cluster (donde está el .env).
# Es seguro repetirlo: si rabbit2 ya pertenece al clúster, no hace reset.
set -euo pipefail
cd "$(dirname "$0")"

docker compose -f docker-compose.cluster.yml up -d
docker exec pedidos360-rabbit1 rabbitmqctl await_startup
docker exec pedidos360-rabbit2 rabbitmqctl await_startup

if docker exec pedidos360-rabbit2 rabbitmqctl cluster_status 2>/dev/null | grep -q "rabbit@rabbit1"; then
  echo "rabbit2 ya está unido al clúster. No se ejecuta reset."
else
  echo "Uniendo rabbit2 a rabbit1 (primera vez)..."
  docker exec pedidos360-rabbit2 rabbitmqctl stop_app
  docker exec pedidos360-rabbit2 rabbitmqctl reset
  docker exec pedidos360-rabbit2 rabbitmqctl join_cluster rabbit@rabbit1
  docker exec pedidos360-rabbit2 rabbitmqctl start_app
fi

docker exec pedidos360-rabbit1 rabbitmqctl cluster_status