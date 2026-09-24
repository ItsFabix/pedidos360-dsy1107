# 01 — Configurar Microsoft Entra ID

## 1. App Registration FRONTEND

1. Entrar a `https://portal.azure.com`.
2. Microsoft Entra ID → App registrations → New registration.
3. Crear una aplicación para Angular.
4. Authentication → Add a platform → **Single-page application (SPA)**.
5. Redirect URI:

```text
http://localhost:4200
```

6. Overview → copiar **Application (client) ID**.
7. Overview → copiar **Directory (tenant) ID**.

## 2. App Registration API

1. App registrations → New registration.
2. Crear una segunda aplicación para la API.
3. Overview → copiar **Application (client) ID**.
4. Manifest → confirmar que el Access Token utilizado sea v2 cuando corresponda a la configuración realizada en clases.
5. Expose an API → definir Application ID URI.
6. Crear scope:

```text
Pedidos.Read
```

El scope completo normalmente quedará:

```text
api://<CLIENT_ID_API>/Pedidos.Read
```

## 3. Dar permiso al FRONTEND

Frontend App Registration → API permissions → Add a permission → My APIs → seleccionar API → Delegated permissions → `Pedidos.Read`.

## 4. App Role opcional

En la App Registration de la API:

```text
App roles → Create app role
Display name: Admin
Value: Admin
Allowed member types: Users/Groups
```

La asignación del rol puede requerir permisos de administración del tenant. Si el tenant de Azure for Students no permite completarla, no bloquear el flujo principal: `Pedidos.Read` sigue siendo obligatorio.

## 5. Valores para environment.ts

```text
CLIENT_ID_FRONTEND
TENANT_ID
CLIENT_ID_API
api://CLIENT_ID_API/Pedidos.Read
```

## 6. Backend

En EC2 también se utilizan:

```text
ENTRA_TENANT_ID=<TENANT_ID>
ENTRA_API_CLIENT_ID=<CLIENT_ID_API>
```

## 7. No usar Client Secret en Angular

La SPA usa Authorization Code + PKCE mediante MSAL. No agregues un Client Secret al código frontend.
