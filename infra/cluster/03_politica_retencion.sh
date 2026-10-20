#!/usr/bin/env bash
# Aplica TTL de 120 s y largo máximo de 200 mensajes SOLO a las tres colas principales.
# No se aplica a .retry ni a .dlq: expirar una DLQ borraría los mensajes fallidos antes de diagnosticarlos.
set -euo pipefail

docker exec pedidos360-rabbit1 rabbitmqctl set_policy \
  --apply-to queues pedidos360-principales \
  '^pedidos360\.(notificaciones|cocina|documentos)\.queue$' \
  '{"message-ttl":120000,"max-length":200}'

docker exec pedidos360-rabbit1 rabbitmqctl list_policies