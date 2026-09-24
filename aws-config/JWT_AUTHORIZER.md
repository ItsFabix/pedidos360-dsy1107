# JWT Authorizer — Microsoft Entra ID

```text
Nombre: entra-jwt-authorizer
Identity source: $request.header.Authorization
Issuer: https://login.microsoftonline.com/<TENANT_ID>/v2.0
Audience: <CLIENT_ID_API>
```

Rutas protegidas:

```text
GET  /api/pedidos → Pedidos.Read
POST /api/pedidos → Pedidos.Read
GET  /api/admin/resumen → JWT Authorizer + Pedidos.Read (opcional); Spring Security exige ROLE_Admin
```

Ruta pública:

```text
GET /api/publico → sin Authorizer
```

Para 403 controlado por scope, crear temporalmente una ruta que exija `Pedidos.Write` y usar un token que solo tenga `Pedidos.Read`.
