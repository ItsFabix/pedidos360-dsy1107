#!/usr/bin/env bash
# Elimina la política de retención (para repetir la demostración).
set -euo pipefail
docker exec pedidos360-rabbit1 rabbitmqctl clear_policy pedidos360-principales
docker exec pedidos360-rabbit1 rabbitmqctl list_policies