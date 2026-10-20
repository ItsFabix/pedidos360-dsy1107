#!/usr/bin/env python3
"""Monitor de colas de Pedidos360 usando la Management API de RabbitMQ (solo biblioteca estándar).

Variables de entorno:
  RABBIT_API_URL       (opcional) por defecto http://localhost:15672/api/queues/%2F
  RABBIT_API_USER      usuario del broker
  RABBIT_API_PASSWORD  clave del broker

Alertas (umbrales de demostración, deben ajustarse a la operación real):
  - una DLQ con mensajes Ready > 0
  - una cola con Ready >= umbral
  - una cola principal con Ready > 0 y sin consumidores
"""
import argparse
import base64
import json
import os
import sys
import time
import urllib.error
import urllib.request

parser = argparse.ArgumentParser(description="Monitor de colas de Pedidos360")
parser.add_argument("--una-vez", action="store_true", help="Consulta una sola vez y termina")
parser.add_argument("--intervalo", type=int, default=15, help="Segundos entre consultas")
parser.add_argument("--umbral-ready", type=int, default=10, help="Alerta si Ready alcanza este valor")
args = parser.parse_args()

url = os.getenv("RABBIT_API_URL", "http://localhost:15672/api/queues/%2F")
usuario = os.getenv("RABBIT_API_USER")
clave = os.getenv("RABBIT_API_PASSWORD")
if not usuario or not clave:
    sys.exit("Defina RABBIT_API_USER y RABBIT_API_PASSWORD")
token = base64.b64encode(f"{usuario}:{clave}".encode()).decode()


def consultar():
    peticion = urllib.request.Request(url, headers={"Authorization": f"Basic {token}"})
    try:
        with urllib.request.urlopen(peticion, timeout=8) as respuesta:
            colas = json.load(respuesta)
    except (urllib.error.URLError, ValueError) as error:
        print("ERROR consultando la Management API:", error)
        return

    print(time.strftime("--- %Y-%m-%d %H:%M:%S ---"))
    print(f"{'COLA':42} {'READY':>6} {'UNACKED':>8} {'CONSUMERS':>10}")
    for cola in sorted(colas, key=lambda c: c.get("name", "")):
        nombre = cola.get("name", "")
        if not nombre.startswith("pedidos360."):
            continue
        ready = int(cola.get("messages_ready", 0) or 0)
        unacked = int(cola.get("messages_unacknowledged", 0) or 0)
        consumidores = int(cola.get("consumers", 0) or 0)
        print(f"{nombre:42} {ready:>6} {unacked:>8} {consumidores:>10}")
        if nombre.endswith(".dlq") and ready > 0:
            print("   ALERTA: la DLQ tiene mensajes fallidos que revisar")
        if ready >= args.umbral_ready:
            print("   ALERTA: acumulación de mensajes Ready")
        if nombre.endswith(".queue") and ready > 0 and consumidores == 0:
            print("   ALERTA: cola principal con mensajes y sin consumidores")


if args.una_vez:
    consultar()
else:
    try:
        while True:
            consultar()
            time.sleep(max(1, args.intervalo))
    except KeyboardInterrupt:
        print("Monitor detenido.")