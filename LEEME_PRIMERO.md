# DSY1107 — Semana 05 — Solución Completa FullStack Cloud

Proyecto acumulativo de referencia para **Semana 05**.

Arquitectura objetivo:

```text
Angular 21
   ↓
MSAL + Microsoft Entra ID
   ↓  Access Token JWT
AWS API Gateway — HTTP API
   ↓  JWT Authorizer (issuer + audience + scope)
Spring Boot / EC2
   ↓  Spring Security
Spring Data JPA
   ↓
Amazon RDS PostgreSQL
   ↓
pedidosdb
```

> **Este ZIP NO contiene credenciales reales.** Antes de ejecutar debes reemplazar tus propios identificadores y configurar variables de entorno.

---

## 1. Abrir el proyecto en VS Code

1. Descomprime el ZIP en una carpeta de trabajo.
2. Abre Visual Studio Code.
3. Selecciona **File → Open Workspace from File...**.
4. Abre:

```text
DSY1107-Semana05.code-workspace
```

El explorador mostrará simultáneamente:

```text
frontend-angular/
backend-springboot/pedidos-api/
```

Extensiones recomendadas de VS Code:

- Extension Pack for Java.
- Spring Boot Extension Pack.
- Angular Language Service.

---

## 2. Archivos / valores que DEBES configurar

### A. Frontend Angular

Archivo:

```text
frontend-angular/src/environments/environment.ts
```

Debes completar:

| Valor | Dónde se obtiene |
|---|---|
| `clientId` | Microsoft Entra ID → App registrations → aplicación del FRONTEND → Overview → **Application (client) ID** |
| `tenantId` | Microsoft Entra ID → Overview/App registration → **Directory (tenant) ID** |
| `apiScope` | App Registration de la API → **Expose an API** → scope `Pedidos.Read` |
| `apiBaseUrl` | AWS → API Gateway → HTTP API → **Invoke URL** |

Plantilla incluida:

```ts
export const environment = {
  production: false,
  msal: {
    clientId: 'PEGAR_AQUI_CLIENT_ID_FRONTEND',
    tenantId: 'PEGAR_AQUI_TENANT_ID',
    redirectUri: 'http://localhost:4200',
    apiScope: 'api://PEGAR_AQUI_CLIENT_ID_API/Pedidos.Read'
  },
  apiBaseUrl: 'PEGAR_AQUI_URL_API_GATEWAY'
};
```

### B. Backend Spring Boot

El backend NO guarda la contraseña de RDS ni los IDs de Entra en el código. Utiliza variables de entorno:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USER
DB_PASSWORD
ENTRA_TENANT_ID
ENTRA_API_CLIENT_ID
```

Origen de los valores:

| Variable | Dónde se obtiene |
|---|---|
| `DB_HOST` | AWS → RDS → `dsy1107-pedidos-db` → Connectivity → **Endpoint** |
| `DB_PORT` | PostgreSQL: `5432` |
| `DB_NAME` | Base inicial creada en RDS: `pedidosdb` |
| `DB_USER` | Usuario maestro RDS: `postgres` |
| `DB_PASSWORD` | Contraseña definida por ti al crear RDS |
| `ENTRA_TENANT_ID` | Microsoft Entra → **Directory (tenant) ID** |
| `ENTRA_API_CLIENT_ID` | App Registration de la API → **Application (client) ID** |

Archivo que consume estas variables:

```text
backend-springboot/pedidos-api/src/main/resources/application.properties
```

> No escribas `DB_PASSWORD` en GitHub, `environment.ts`, `application.properties`, capturas públicas ni documentos compartidos.

---

## 3. Microsoft Entra ID requerido

Debes tener dos App Registrations:

```text
App FRONTEND Angular
└── Redirect URI SPA: http://localhost:4200

App API / Backend
└── Expose an API
    └── Scope: Pedidos.Read
```

La aplicación frontend debe tener permiso delegado sobre `Pedidos.Read`.

No se usa **Client Secret** en Angular. Es una SPA y utiliza Authorization Code + PKCE mediante MSAL.

Si el tenant institucional no permite crear App Registrations, utiliza el tenant/suscripción de **Azure for Students** indicado por el docente. Evita realizar la configuración en una ventana de incógnito si aparecen errores de sesión/cookies.

Guía detallada:

```text
01_CONFIGURAR_ENTRA_ID.md
```

---

## 4. AWS requerido

Configuración utilizada en Semana 05:

```text
Región: us-east-1 — Norte de Virginia

RDS
├── PostgreSQL
├── Capa gratuita
├── db.t4g.micro
├── gp2 20 GiB
├── pedidosdb
├── usuario postgres
├── puerto 5432
└── acceso público: NO

EC2
└── Spring Boot / Java 21

API Gateway
└── HTTP API
    ├── GET  /api/publico
    ├── GET  /api/pedidos
    ├── POST /api/pedidos
    └── GET  /api/admin/resumen   (opcional — role Admin)
```

Guía:

```text
02_CONFIGURAR_AWS.md
aws-config/
```

---

## 5. Instalar dependencias Angular

El ZIP **NO incluye `node_modules`**. Esto es intencional.

La carpeta correcta generada por NPM se llama:

```text
node_modules
```

Desde la terminal de VS Code:

```powershell
cd frontend-angular
npm install
```

Si por alguna razón MSAL no quedó instalado:

```powershell
npm install @azure/msal-angular @azure/msal-browser
```

Si necesitas Angular CLI sin instalarlo globalmente:

```powershell
npx ng serve
```

Opcional, instalación global:

```powershell
npm install -g @angular/cli
ng serve
```

---

## 6. Compilar / ejecutar Spring Boot

Requisito:

```powershell
java -version
```

Debe mostrar **Java 21**.

### Windows

```powershell
cd backend-springboot\pedidos-api
.\mvnw.cmd clean package -DskipTests
```

### Linux / EC2

```bash
cd backend-springboot/pedidos-api
chmod +x mvnw
./mvnw clean package -DskipTests
```

El proyecto incluye **Maven Wrapper**. No es necesario instalar Maven globalmente. La primera ejecución de `mvnw` / `mvnw.cmd` descargará automáticamente la versión de Maven definida en `.mvn/wrapper/maven-wrapper.properties` (actualmente Apache Maven 3.9.16); por ello se requiere conexión a Internet la primera vez.

---

## 7. Orden recomendado para levantar la solución COMPLETA

```text
1. Microsoft Entra ID
   ├── App Frontend
   ├── App API
   └── Pedidos.Read

2. Amazon RDS PostgreSQL
   └── esperar estado Disponible

3. Amazon EC2
   ├── Java 21
   ├── desplegar JAR
   └── variables DB_* + ENTRA_*

4. Spring Boot / EC2
   └── comprobar /api/publico

5. API Gateway HTTP API
   ├── rutas
   ├── integraciones
   └── CORS

6. Angular
   ├── npm install
   ├── environment.ts
   └── npx ng serve

7. JWT Authorizer
   ├── issuer
   ├── audience
   └── scope Pedidos.Read

8. Pruebas
   ├── 200
   ├── 401
   └── 403
```

Comandos detallados:

```text
03_COMANDOS_EJECUCION.md
```

---

## 8. Ejecutar frontend

```powershell
cd frontend-angular
npm install
npx ng serve
```

Abrir:

```text
http://localhost:4200
```

Funciones incluidas:

- Login / logout con Microsoft Entra ID.
- MsalGuard.
- MsalInterceptor.
- Vista parcial del Access Token.
- Consultar endpoint público.
- Consultar pedidos protegidos.
- Crear un pedido y persistirlo.
- Consultar endpoint Admin opcional.

---

## 9. Ejecutar backend en EC2

En EC2:

```bash
export DB_HOST="<ENDPOINT_RDS>"
export DB_PORT="5432"
export DB_NAME="pedidosdb"
export DB_USER="postgres"
export DB_PASSWORD="<PASSWORD_RDS>"
export ENTRA_TENANT_ID="<TENANT_ID>"
export ENTRA_API_CLIENT_ID="<CLIENT_ID_API>"

java -jar pedidos-api.jar
```

Para segundo plano:

```bash
nohup java -jar pedidos-api.jar > app.log 2>&1 &
tail -f app.log
```

Ejemplo preparado:

```text
scripts/env-example-ec2.sh
```

---

## 10. Prueba local opcional del backend

La arquitectura oficial usa **RDS privado + EC2**. Si solo deseas comprobar JPA desde tu PC, se incluye un PostgreSQL local opcional mediante Docker Compose:

```powershell
cd local-dev
docker compose -f docker-compose.postgres.yml up -d
```

Después, desde PowerShell:

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="pedidosdb"
$env:DB_USER="postgres"
$env:DB_PASSWORD="dsy1107-local"
$env:ENTRA_TENANT_ID="TU_TENANT_ID"
$env:ENTRA_API_CLIENT_ID="TU_CLIENT_ID_API"

cd ..\backend-springboot\pedidos-api
.\mvnw.cmd spring-boot:run
```

> Este PostgreSQL local es solo para desarrollo. **No reemplaza Amazon RDS** en la solución de Semana 05.

---

## 11. Pruebas esperadas

```text
GET /api/publico
→ 200 sin token

GET /api/pedidos
→ 401 sin token
→ 200 con Access Token válido + Pedidos.Read

GET /api/admin/resumen
→ 403 si el token es válido pero el usuario no posee role Admin
→ 200 si posee role Admin
```

Para demostrar el `403` de API Gateway por scope insuficiente, sigue:

```text
04_PRUEBAS_200_401_403.md
```

---

## 12. Archivos que NO debes subir a GitHub

El `.gitignore` ya contempla:

```text
node_modules/
dist/
.angular/cache/
target/
.env
*.pem
*.key
```

También debes evitar cualquier archivo que contenga:

- contraseña RDS;
- Access Tokens;
- Client Secrets;
- claves SSH.

---

## 13. Relación con Semana 05

Este proyecto cubre:

### Sesión 1

```text
Spring Boot + EC2 + Spring Data JPA + RDS PostgreSQL
```

### Sesión 2

```text
API Gateway HTTP API + rutas + CORS + integración EC2
```

### Sesión 3

```text
Microsoft Entra ID + JWT Authorizer + scopes/roles + 200/401/403
```

El código se entrega como **solución de referencia y comparación**, no como sustituto del desarrollo individual/grupal realizado durante las sesiones.
