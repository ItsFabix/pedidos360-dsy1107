#!/usr/bin/env python3
"""Aplica la política de retención mediante la Management API (evita problemas de comillas en PowerShell).

TTL 120000 ms y largo máximo 200, SOLO en las colas principales. No toca .retry ni .dlq.
Uso:  RABBIT_API_USER=... RABBIT_API_PASSWORD=... python aplicar_politica.py [--quitar]
"""
import base64
import json
import os
import sys
import urllib.request

usuario = os.getenv("RABBIT_API_USER")
clave = os.getenv("RABBIT_API_PASSWORD")
if not usuario or not clave:
    sys.exit("Defina RABBIT_API_USER y RABBIT_API_PASSWORD")

base = os.getenv("RABBIT_API_BASE", "http://localhost:15672/api")
url = f"{base}/policies/%2F/pedidos360-principales"
token = base64.b64encode(f"{usuario}:{clave}".encode()).decode()
cabeceras = {"Content-Type": "application/json", "Authorization": f"Basic {token}"}

if "--quitar" in sys.argv:
    peticion = urllib.request.Request(url, method="DELETE", headers=cabeceras)
    accion = "Política eliminada"
else:
    cuerpo = {
        "pattern": r"^pedidos360\.(notificaciones|cocina|documentos)\.queue$",
        "definition": {"message-ttl": 120000, "max-length": 200},
        "priority": 0,
        "apply-to": "queues",
    }
    peticion = urllib.request.Request(url, data=json.dumps(cuerpo).encode(), method="PUT", headers=cabeceras)
    accion = "Política aplicada solo a las colas principales"

with urllib.request.urlopen(peticion, timeout=10) as respuesta:
    print(f"{accion}. HTTP {respuesta.status}")