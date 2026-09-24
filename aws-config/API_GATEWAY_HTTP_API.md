# API Gateway — HTTP API

Nombre:

```text
dsy1107-pedidos-api-gateway
```

Integraciones:

```text
integ-publico → http://<IP_EC2>:8080/api/publico
integ-pedidos → http://<IP_EC2>:8080/api/pedidos
integ-admin   → http://<IP_EC2>:8080/api/admin/resumen   (opcional)
```

Rutas:

```text
GET  /api/publico
GET  /api/pedidos
POST /api/pedidos
GET  /api/admin/resumen   (opcional)
```

CORS:

```text
Origin: http://localhost:4200
Methods: GET, POST, OPTIONS
Headers: authorization, content-type
```

Copia el Invoke URL y pégalo en:

```text
frontend-angular/src/environments/environment.ts
```
