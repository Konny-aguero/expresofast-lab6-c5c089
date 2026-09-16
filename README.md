# ExpresoFast - Laboratorios 5 y 6

Universidad de Costa Rica - Sede del Atlántico, Recinto de Paraíso  
IF0009 Desarrollo de Software IV - II ciclo 2026  
Carné: C5C089  
Estudiante: completar nombre completo antes de la entrega.

Plataforma full-stack para administrar flota y envíos express. Incluye Spring Boot, SQL Server, JPA, JWT, RBAC, DTOs validados, bitácora transaccional y un frontend responsivo en HTML, CSS y JavaScript.

## Requisitos

- JDK 21 o superior.
- Maven 3.9.x.
- Microsoft SQL Server y SSMS.
- Navegador moderno.

## Configuración de SQL Server

1. Crear o seleccionar la base `ExpresoFastC5C089_II2026`.
2. Ejecutar, en orden, `database/01_schema_lab5.sql`, `database/02_schema_lab6_extension.sql` y `database/03_data_seeds.sql`.
3. Usar `backend/src/main/resources/application.properties.template` como referencia de configuración.
4. Para despliegue, definir `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` en el entorno. La configuración local incluye valores predeterminados para poder iniciar desde VS Code; la clave JWT de producción debe tener al menos 32 bytes.
5. Mantener `spring.jpa.hibernate.ddl-auto=none`; el esquema se administra mediante los scripts SQL.

Los scripts son idempotentes para registros y tablas faltantes: no reemplazan contraseñas ni datos ya existentes.

## Usuarios iniciales

| Usuario | Contraseña | Rol |
| --- | --- | --- |
| admin | admin123 | ROLE_ADMIN |
| operador | operador123 | ROLE_OPERADOR |
| conductor1 | cond123 | ROLE_CONDUCTOR |

Las contraseñas se almacenan como hashes BCrypt en `03_data_seeds.sql`.

## Compilar y ejecutar

Desde la raíz:

```powershell
mvn clean verify
.\iniciar-sqlserver.cmd
```

También se puede iniciar directamente:

```powershell
mvn -f backend/pom.xml spring-boot:run
```

Abrir `http://localhost:8080`. Spring Boot sirve tanto la API como el frontend, por lo que no se requiere Node para ejecutar la aplicación.

El JAR se genera en `backend/target/lab5-0.0.1-SNAPSHOT.jar`:

```powershell
java -jar backend/target/lab5-0.0.1-SNAPSHOT.jar
```

## Funcionalidad y permisos

- `POST /api/auth/login`: autenticación pública y emisión de JWT.
- `GET /api/envios/optimizados`: consulta con `JOIN FETCH`, disponible para los tres roles.
- `POST /api/envios`: creación para ADMIN y OPERADOR.
- `PATCH /api/envios/{id}/estado`: cambio transaccional para ADMIN y CONDUCTOR.
- `GET /api/envios/{id}/bitacora`: historial para ADMIN y OPERADOR.
- `/api/vehiculos/**`: gestión de flota exclusiva de ADMIN.

Los envíos validan formato, valores positivos, referencias, capacidad del vehículo y transiciones `PENDIENTE -> EN_TRANSITO -> ENTREGADO`, con cancelación desde estados no finales. Cada cambio guarda usuario, fecha, estados y observaciones en la bitácora dentro de la misma transacción.

El frontend adjunta el JWT, diferencia respuestas 401 y 403, adapta las acciones al rol, usa DTOs planos y permite filtrar la bitácora por fechas. CORS permite clientes locales y solicitudes preflight `OPTIONS`.

## Pruebas

```powershell
mvn test
node --test tests/frontend-api.test.cjs
```

Las pruebas del backend utilizan una base H2 efímera únicamente como dependencia de pruebas, en modo de compatibilidad SQL Server. Esto permite verificar contratos y reglas sin sustituir la conexión productiva a SQL Server.

La colección `docs/ExpresoFast_Postman_Collection.json` usa `http://localhost:8080/api`, almacena el JWT después del login e incluye solicitudes de los flujos principales.

## Estructura

```text
backend/    Backend Maven y configuración de Spring Boot
database/   DDL de SQL Server y datos iniciales BCrypt
frontend/   Login, tablero, estilos y cliente Fetch API
docs/       Colección Postman y notas de verificación
tests/      Pruebas del cliente JavaScript
```
# ExpresoFast - Laboratorio 7 (Suite de Pruebas)

## Ejecución de Pruebas
Para ejecutar la suite de pruebas unitarias y de integración de este laboratorio, utilice el siguiente comando en la raíz del proyecto (o dentro de la carpeta `backend`):

`mvn clean test`

Para ejecutar las pruebas y además generar la validación estricta de cobertura, utilice:

`mvn clean verify`

## Reporte de Cobertura (JaCoCo)
Una vez ejecutado el comando `verify`, el reporte de cobertura de código se generará automáticamente. Para visualizarlo, abra el siguiente archivo en cualquier navegador web:
`target/site/jacoco/index.html`
