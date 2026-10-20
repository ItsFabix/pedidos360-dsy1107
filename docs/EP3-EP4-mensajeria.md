# Mensajería asíncrona con RabbitMQ — EP3 y EP4

Este documento describe cómo Pedidos360 incorpora RabbitMQ sobre lo construido en EP1 (Angular + Entra ID,
API Gateway, Spring Boot, RDS PostgreSQL). Nada de EP1 se reemplazó: se agregó el productor dentro de
`pedidos-api` y dos microservicios nuevos.

## 1. Arquitectura

```
Angular ─► API Gateway (JWT) ─► pedidos-api ──► RDS PostgreSQL
                                     │  (después del commit)
                                     ▼
                          RabbitMQ (rabbit1 + rabbit2, clúster)
                                     │
        ┌────────────────────────────┼─────────────────────────────┐
        ▼                            ▼                             ▼
 pedidos360.notificaciones     pedidos360.cocina           pedidos360.documentos
        └──────────────┬─────────────┴──────────────┬──────────────┘
                       ▼                            ▼
              pedidos-mensajeria (consumidores)   rabbit-admin (administración, rol Admin)
```

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| `pedidos-api` | 8080 | Productor: publica un evento cuando un pedido cambia de estado |
| `pedidos-mensajeria` | 8081 | Declara la topología, consume por dominio, aplica reintentos y DLQ |
| `rabbit-admin` | 8082 | API REST para crear y eliminar colas, exchanges y bindings |

## 2. Topología

**Exchanges:** `pedidos360.direct.exchange` (direct), `pedidos360.topic.exchange` (topic),
`pedidos360.retry.exchange` (direct), `pedidos360.dlx` (direct, Dead Letter Exchange).

**Por cada dominio** (`notificaciones`, `cocina`, `documentos`):

| Cola | Función | Binding |
|---|---|---|
| `pedidos360.<dominio>.queue` | Cola de trabajo. Si se rechaza sin requeue, va al DLX | direct, key `<dominio>` |
| `pedidos360.<dominio>.retry` | Espera 5 s (TTL) y devuelve el mensaje a la cola principal | retry, key `<dominio>` |
| `pedidos360.<dominio>.dlq` | Mensajes fallidos, se conservan para diagnóstico | dlx, key `<dominio>.failed` |

Además `pedidos360.monitoreo.queue` está ligada al exchange topic con el patrón `pedido.*` y registra todos
los cambios de estado (`pedido.aceptado`, `pedido.cancelado`, ...).

**Quién publica qué:** al pasar un pedido a `ACEPTADO` se publica un mensaje en el exchange direct hacia los
tres dominios. Cualquier cambio de estado se anuncia además por el exchange topic.

## 3. Política de confirmación y manejo de errores

Implementada en `pedidos-mensajeria/.../service/ProcesadorEventos.java`. El modo de confirmación es **manual**
y `prefetch=1`.

| Situación | Acción | Resultado |
|---|---|---|
| Procesamiento correcto | `basicAck` | El broker elimina el mensaje |
| Mensaje ilegible (JSON inválido) | `basicNack(requeue=false)` | Va directo a la DLQ, no se reintenta: repetirlo no lo arreglaría |
| Error de procesamiento, intentos disponibles | Publica copia en `.retry` y `basicAck` | Espera 5 s y vuelve a la cola principal (máx. 2 reintentos) |
| Error de procesamiento, intentos agotados | `basicNack(requeue=false)` | El broker lo envía a la DLQ del dominio |
| No se pudo publicar el reintento | `basicNack(requeue=true)` | El mensaje vuelve a la cola para no perderse |

El número de intentos viaja en el header `x-intentos`. Un mensaje se entrega como máximo 3 veces
(1 entrega inicial + 2 reintentos). Los logs registran dominio, pedido, evento e intento; nunca el contenido
completo del mensaje, contraseñas ni tokens.

**Fallo controlado para demostración:** variable de entorno `SIMULAR_FALLO_DOMINIOS=notificaciones`. Solo el
dominio marcado falla; los otros siguen procesando, lo que demuestra el desacoplamiento.

## 4. Administración (`rabbit-admin`, rol Admin)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/rabbit/queues` | Crea una cola (opcional: `durable`, `ttlMs`, `maxLength`, DLX) |
| GET | `/api/rabbit/queues/{nombre}` | Mensajes y consumidores de la cola |
| DELETE | `/api/rabbit/queues/{nombre}` | Elimina una cola |
| POST | `/api/rabbit/exchanges` | Crea un exchange `DIRECT`, `TOPIC` o `FANOUT` |
| DELETE | `/api/rabbit/exchanges/{nombre}` | Elimina un exchange |
| POST | `/api/rabbit/bindings` | Une una cola a un exchange con una routing key |
| DELETE | `/api/rabbit/bindings?cola=&exchange=&routingKey=` | Elimina el binding |

**Validaciones** (en el DTO con Bean Validation y de nuevo en el servicio): nombres con letras, números,
`. - _` (máx. 100); routing key con `. - _ * #`; TTL entre 1 s y 24 h; `maxLength` entre 1 y 100000; el prefijo
`amq.` está reservado por RabbitMQ; lo que empieza con `pedidos360.` es la topología del sistema y **no se
puede eliminar** (409). Cola inexistente: 404. Datos inválidos: 400. RabbitMQ caído: 503.

## 5. Retención y monitoreo

Política `pedidos360-principales`: TTL 120000 ms y `max-length` 200, **solo** sobre las tres colas principales
(`infra/cluster/03_politica_retencion.sh`). Se excluyen las `.dlq` a propósito: expirarlas borraría los
mensajes fallidos antes de que alguien los diagnostique.

`infra/cluster/monitor/monitor_queues.py` consulta la Management API y muestra Ready, Unacked y Consumers.
Alertas: DLQ con Ready > 0; Ready ≥ 10; cola principal con mensajes y sin consumidores. Los umbrales son de
demostración y deben ajustarse a la carga real.

## 6. Clúster de dos nodos

`infra/cluster/docker-compose.cluster.yml` levanta `rabbit@rabbit1` y `rabbit@rabbit2` con la misma Erlang
Cookie. El segundo nodo se une al primero con `stop_app → reset → join_cluster → start_app`
(`01_inicializar_cluster.sh`, que no repite el `reset` si el nodo ya está unido).

**Límite que se declara explícitamente:** un clúster de dos nodos comparte metadatos (usuarios, exchanges,
colas declaradas), pero las colas clásicas viven en un solo nodo y no se replican solas. Tampoco hay alta
disponibilidad garantizada: un quorum de dos miembros pierde la mayoría si cae uno. Lo que se demuestra es la
agrupación de nodos y el monitoreo, no tolerancia a fallos.

## 7. Seguridad

- `rabbit-admin` exige JWT de Entra ID con rol `Admin`.
- Las credenciales de RabbitMQ y la Erlang Cookie se leen desde variables de entorno / `.env` (excluido de Git).
- La Management UI (15672/15673) no se expone a Internet: el Security Group la limita a la IP del operador.
- Límite conocido del productor: si RabbitMQ no responde justo después del commit, el pedido queda guardado
  y el mensaje se pierde (queda en el log). La solución completa es el patrón outbox.

## 8. Trazabilidad con la rúbrica EP3

| Indicador | Dónde está |
|---|---|
| IE3.1 Nombres centralizados | `pedidos-mensajeria/.../config/RabbitNombres.java` |
| IE3.2 Beans Queue / Exchange / Binding por caso de uso | `ExchangesConfig`, `NotificacionesTopologia`, `CocinaTopologia`, `DocumentosTopologia`, `MonitoreoTopologia` |
| IE3.3 Configuración separada de la lógica | paquete `config/` vs. `service/` y `consumer/` |
| IE3.4 `@RabbitListener` agrupados por dominio | paquete `consumer/`: una clase por dominio |
| IE3.5 ACK y manejo explícito de errores | `service/ProcesadorEventos.java` y sección 3 de este documento |
| IE3.6 Endpoints de administración | `rabbit-admin/.../controller/RabbitAdminController.java` |
| IE3.7 Lógica encapsulada en un servicio | `rabbit-admin/.../service/RabbitAdminService.java` |
| IE3.8 Validación de entradas | DTOs con Bean Validation, reglas del servicio y `ManejadorErrores` |

## 9. Ejecución local

Requisitos: Java 21, Docker Desktop. Primero el clúster (`infra/cluster`, con su `.env`), luego cada servicio
con `RABBIT_USER`, `RABBIT_PASSWORD` y, en `pedidos-api`, las variables de base de datos de EP1.

```bash
cd backend-springboot/pedidos-mensajeria && ./mvnw spring-boot:run   # 8081
cd backend-springboot/rabbit-admin       && ./mvnw spring-boot:run   # 8082
cd backend-springboot/pedidos-api        && ./mvnw spring-boot:run   # 8080
```
