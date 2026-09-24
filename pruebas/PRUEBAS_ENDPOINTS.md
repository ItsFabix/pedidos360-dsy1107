# Pruebas de endpoints

Sustituye:

```text
<API_URL> = https://<api-id>.execute-api.us-east-1.amazonaws.com
<TOKEN>   = Access Token completo
```

## Público

```bash
curl -i <API_URL>/api/publico
```

Esperado: `200`.

## Pedidos sin token

```bash
curl -i <API_URL>/api/pedidos
```

Esperado: `401`.

## Pedidos con token

```bash
curl -i \
  -H "Authorization: Bearer <TOKEN>" \
  <API_URL>/api/pedidos
```

Esperado: `200` si `scp` contiene `Pedidos.Read`.

## Crear pedido

```bash
curl -i -X POST \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"descripcion":"Monitor","estado":"CREADO","total":159990}' \
  <API_URL>/api/pedidos
```

Esperado: `201`.

## Admin

```bash
curl -i \
  -H "Authorization: Bearer <TOKEN>" \
  <API_URL>/api/admin/resumen
```

Esperado: `200` con role `Admin`; de lo contrario, rechazo por autorización en backend.
