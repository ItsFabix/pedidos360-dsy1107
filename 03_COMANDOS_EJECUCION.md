# 03 — Comandos de instalación y ejecución

## A. Abrir Workspace

```text
VS Code → File → Open Workspace from File
→ DSY1107-Semana05.code-workspace
```

## B. Frontend Angular

### Primera vez

```powershell
cd frontend-angular
npm install
```

Si MSAL falta:

```powershell
npm install @azure/msal-angular @azure/msal-browser
```

Ejecutar:

```powershell
npx ng serve
```

Abrir:

```text
http://localhost:4200
```

## C. Backend — compilar en Windows

```powershell
cd backend-springboot\pedidos-api
java -version
.\mvnw.cmd clean package -DskipTests
```

JAR esperado:

```text
target/pedidos-api-0.0.1-SNAPSHOT.jar
```

## D. Backend — EC2

Java 21:

```bash
sudo dnf update -y
sudo dnf install java-21-amazon-corretto-headless -y
java -version
```

Variables:

```bash
export DB_HOST="<ENDPOINT_RDS>"
export DB_PORT="5432"
export DB_NAME="pedidosdb"
export DB_USER="postgres"
export DB_PASSWORD="<PASSWORD_RDS>"
export ENTRA_TENANT_ID="<TENANT_ID>"
export ENTRA_API_CLIENT_ID="<CLIENT_ID_API>"
```

Ejecutar:

```bash
java -jar pedidos-api.jar
```

Segundo plano:

```bash
nohup java -jar pedidos-api.jar > app.log 2>&1 &
tail -f app.log
```

## E. Pruebas rápidas backend

```bash
curl http://localhost:8080/api/publico
curl -i http://localhost:8080/api/pedidos
```

Sin token, `/api/pedidos` debe responder `401`.

## F. Orden completo

```text
Entra ID → RDS → EC2 → Spring Boot → API Gateway → Angular → JWT Authorizer → 200/401/403
```
