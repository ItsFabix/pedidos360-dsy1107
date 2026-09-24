# Amazon RDS PostgreSQL

Configuración validada para el laboratorio:

```text
us-east-1
PostgreSQL
Capa gratuita
Single-AZ / 1 instancia
db.t4g.micro
gp2 20 GiB
Identificador: dsy1107-pedidos-db
Base inicial: pedidosdb
Usuario: postgres
Puerto: 5432
Acceso público: No
Security Group: dsy1107-rds-sg
Backup: 1 día
```

Regla de entrada RDS:

```text
PostgreSQL TCP 5432
Origen: dsy1107-ec2-sg
```
