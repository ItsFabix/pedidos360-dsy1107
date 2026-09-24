# pedidos-api — Semana 05

Backend Spring Boot 3 + Java 21.

Incluye:

- Spring Web.
- Spring Security.
- OAuth2 Resource Server / JWT.
- Spring Data JPA.
- PostgreSQL.
- `Pedido` + `PedidoRepository` + `PedidoService`.
- `GET /api/publico`.
- `GET /api/pedidos`.
- `POST /api/pedidos`.
- `GET /api/admin/resumen` (ROLE_Admin).

Variables obligatorias:

```text
DB_HOST
DB_PASSWORD
ENTRA_TENANT_ID
ENTRA_API_CLIENT_ID
```

Variables con valor predeterminado:

```text
DB_PORT=5432
DB_NAME=pedidosdb
DB_USER=postgres
```
