# 02 — Configurar AWS para Semana 05

Región utilizada:

```text
us-east-1 — Estados Unidos (Norte de Virginia)
```

## A. AWS Academy Learner Lab

1. Entrar a `https://awsacademy.instructure.com`.
2. Curso AWS Academy Learner Lab.
3. Módulos → Iniciar el Laboratorio de aprendizaje de AWS Academy.
4. Start Lab.
5. Esperar AWS verde.
6. Abrir AWS Management Console.
7. Confirmar `us-east-1`.

## B. Amazon RDS PostgreSQL

Valores utilizados en la guía de Semana 05:

```text
Motor: PostgreSQL
Plantilla: Capa gratuita
Disponibilidad: 1 instancia
Identificador: dsy1107-pedidos-db
Usuario: postgres
Autenticación: contraseña
Clase: db.t4g.micro
Almacenamiento: gp2 — 20 GiB
Base inicial: pedidosdb
Puerto: 5432
VPC: Default VPC
Acceso público: No
Security Group: dsy1107-rds-sg
Backup: 1 día
Protección contra eliminación: No
```

Security Group RDS:

```text
PostgreSQL / TCP / 5432
Origen: dsy1107-ec2-sg
```

Nunca utilizar `0.0.0.0/0` para el puerto 5432.

## C. EC2

Nombre sugerido:

```text
dsy1107-pedidos-api
```

- Amazon Linux 2023.
- Instancia pequeña permitida por Learner Lab.
- Java 21.
- Security Group: `dsy1107-ec2-sg`.

Durante la Sesión 2, la guía utiliza una integración HTTP pública de API Gateway hacia EC2. Para este laboratorio se abre temporalmente TCP 8080 al endpoint público de EC2. Esta es una simplificación pedagógica; no es el patrón recomendado de producción.

## D. API Gateway

Tipo:

```text
HTTP API
```

Nombre:

```text
dsy1107-pedidos-api-gateway
```

Integraciones:

```text
integ-publico  → http://<IP_EC2>:8080/api/publico
integ-pedidos  → http://<IP_EC2>:8080/api/pedidos
integ-admin    → http://<IP_EC2>:8080/api/admin/resumen   (opcional)
```

Rutas:

```text
GET  /api/publico → integ-publico
GET  /api/pedidos → integ-pedidos
POST /api/pedidos → integ-pedidos
GET  /api/admin/resumen → integ-admin   (opcional)
```

CORS:

```text
Allowed origin:  http://localhost:4200
Allowed methods: GET, POST, OPTIONS
Allowed headers: authorization, content-type
```

## E. JWT Authorizer

```text
Nombre: entra-jwt-authorizer
Identity source: $request.header.Authorization
Issuer: https://login.microsoftonline.com/<TENANT_ID>/v2.0
Audience: <CLIENT_ID_API>
```

Para las rutas protegidas:

```text
Authorization scope: Pedidos.Read
```

`GET /api/publico` debe permanecer sin autorización en API Gateway.
