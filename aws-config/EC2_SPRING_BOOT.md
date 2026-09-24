# EC2 + Spring Boot

1. Crear EC2 `dsy1107-pedidos-api` en `us-east-1`.
2. Amazon Linux 2023.
3. Instalar Java 21.
4. Subir `pedidos-api-0.0.1-SNAPSHOT.jar`.
5. Definir variables de entorno.
6. Ejecutar JAR.

Para la integración HTTP pública del laboratorio, API Gateway debe poder alcanzar `http://<IP_EC2>:8080`. La guía de Sesión 2 abre temporalmente TCP 8080 para el laboratorio.

En producción se preferiría una integración privada (por ejemplo, VPC Link + recurso compatible) en lugar de exponer directamente EC2.
