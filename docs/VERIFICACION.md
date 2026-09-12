# Verificación del proyecto

- El backend se compila y valida desde la raíz con `mvn clean verify`.
- Las pruebas de integración usan H2 exclusivamente en alcance `test`, configurado en modo de compatibilidad con SQL Server.
- Las pruebas cubren autenticación JWT, RBAC, CORS, DTOs, reglas de negocio, auditoría, bitácora y gestión de flota.
- El cliente HTTP se valida con `node --test tests/frontend-api.test.cjs`.
- Los archivos JavaScript se validan con `node --check`.
- El JAR generado incluye el frontend y se produce en `backend/target/`.

## Verificación pendiente del entorno

La conexión al servidor SQL Server debe comprobarse cuando el servicio esté disponible. El modelo JPA y los scripts DDL se validan entre sí mediante el esquema automatizado de pruebas, pero esta verificación local no sustituye la prueba final contra la instancia real.
