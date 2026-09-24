# Frontend Angular — Semana 05

## Instalar

```powershell
npm install
```

Si faltan las dependencias MSAL:

```powershell
npm install @azure/msal-angular @azure/msal-browser
```

## Configurar

Editar:

```text
src/environments/environment.ts
```

## Ejecutar

```powershell
npx ng serve
```

Abrir `http://localhost:4200`.

El frontend espera que `apiBaseUrl` sea el **Invoke URL de AWS API Gateway**.
