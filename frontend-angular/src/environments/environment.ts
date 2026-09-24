export const environment = {
  production: false,

  msal: {
    // Microsoft Entra ID → App registrations → FRONTEND → Overview
    clientId: '9d8bb75e-7ff8-412f-bdd5-c2e0c2be82f2',

    // Microsoft Entra ID → Directory (tenant) ID
    tenantId: '84e517e6-6e52-43b3-82da-162220153ea3',

    // Debe existir como Redirect URI de tipo SPA en la App Registration FRONTEND.
    redirectUri: 'http://localhost:4200',

    // App Registration API → Expose an API → Pedidos.Read
    apiScope: 'api://d7a99a6a-b387-494e-a239-5ea101bb3856/Pedidos.Read'
  },

  // AWS → API Gateway → HTTP API → Invoke URL.
  // Ejemplo: https://abc123.execute-api.us-east-1.amazonaws.com
  apiBaseUrl: 'https://tjkn3r8p13.execute-api.us-east-1.amazonaws.com'
};
