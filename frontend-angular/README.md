# Frontend Angular — Pedidos360

Aplicación Angular 21 de Pedidos360 (DSY1107). Inicia sesión con Microsoft Entra ID (MSAL, OAuth 2.0 / OpenID Connect con PKCE) y consume la API a través de AWS API Gateway enviando el JWT en cada llamada.

## Pantallas

Inicio (resumen según rol), Pedidos, Catálogo y Diagnóstico. El rol sale del claim `roles` del token: el rol **Admin** gestiona el catálogo y cambia el estado de cualquier pedido.

## Requisitos

- Node.js 20.19 o superior (22 LTS recomendado) y npm.
- Un usuario del tenant de Entra ID con acceso a la aplicación.

## Configuración

Se edita `src/environments/environment.ts`:

| Campo                             | Qué es                                                                                 |
| --------------------------------- | -------------------------------------------------------------------------------------- |
| `msal.clientId` y `msal.tenantId` | Identificadores públicos de la app registrada en Entra ID (no son secretos)            |
| `msal.redirectUri`                | `http://localhost:4200` (debe estar registrada como URI de redirección tipo SPA)       |
| `msal.apiScope`                   | Scope de la API protegida (`api://<id-api>/Pedidos.Read`)                              |
| `apiBaseUrl`                      | URL de invocación de API Gateway, **sin** `/api` (el front agrega `/api/orders`, etc.) |

Para probar contra un backend local, cambia temporalmente `apiBaseUrl` a `http://localhost:8080`. **No subir ese cambio al repositorio.**

## Ejecutar

```powershell
npm install
npm start
```

Abre `http://localhost:4200`.

## Compilar y probar

```powershell
npm run build
npm test
```

## Seguridad

No hay secretos en el front: la SPA usa PKCE, sin _client secret_. Los tokens los gestiona MSAL y no se guardan en el código.

Despliegue y resto del sistema: ver el [README principal](../README.md).
