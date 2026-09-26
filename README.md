# ExpresoFast - Laboratorio 9

Universidad de Costa Rica · IF0009 Desarrollo de Software IV · II ciclo 2026
Carné: C5C089

Consola de operación logística con Spring Boot, SQL Server, HTML, CSS y JavaScript.

## Laboratorio 9

Después de los scripts 01 a 04, ejecutar `database/schema.sql` y luego `database/data.sql`
en la misma base SQL Server. El primero agrega el destinatario y el procedimiento
`SP_OBTENER_ENVIOS_POR_ESTADO`; el segundo inserta 15 envíos con los cuatro estados.
Se pueden volver a ejecutar sin duplicar datos. Los envíos anteriores sin destinatario
aparecen como "Sin registrar".

Iniciar sesión y abrir el enlace **Laboratorio 9** del panel, o visitar
`http://localhost:8080/dashboard_paginado.html`.
La vista permite buscar por rastreo, destinatario y dirección, filtrar por estado,
elegir 5/10/20 registros y navegar entre páginas. El selector Stored Procedure consulta
el procedimiento por estado y muestra su lista completa; en ese modo no se pagina.

- `GET /api/v1/envios?page=0&size=5&sortBy=fechaCreacion&direction=desc&busqueda=&estado=`
- `GET /api/v1/envios/procedimiento/PENDIENTE`

Ambas rutas requieren JWT. Los conductores solo ven sus envíos asignados.
`sortBy` admite id, codigoRastreo, destinatario, direccionDestino, montoFlete, estado
y fechaCreacion; `direction` admite asc/desc. La paginación se ejecuta en la base de datos.
El procedimiento opcional de métricas no se incluye.

## Ejecutar la API

Requisitos: Java 21 o superior, Maven y SQL Server.

1. Seleccionar la base `ExpresoFastC5C089_II2026` en SQL Server.
2. Ejecutar en orden los scripts `database/01_schema_lab5.sql`, `02_schema_lab6_extension.sql`, `03_data_seeds.sql` y `04_lab8_usuario_conductor.sql`. Si los laboratorios anteriores ya están instalados, ejecutar únicamente el cuarto script.
3. Copiar `backend/src/main/resources/application.properties.template` como `application.properties` en la misma carpeta si todavía no existe.
4. Configurar `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` en el entorno. La clave JWT debe tener al menos 32 bytes. Mantener `spring.jpa.hibernate.ddl-auto=none`.
5. Desde la raíz del proyecto:

```powershell
mvn -f backend/pom.xml spring-boot:run
```

La API queda en `http://localhost:8080/api`. También se puede abrir el cliente en `http://localhost:8080/index.html`.

## Ejecutar el cliente por separado

Abrir `frontend/index.html` con Live Server en el puerto 5500. Como alternativa, desde la raíz:

Abrir `http://localhost:5500/index.html`. El cliente utiliza la API del puerto 8080 y el backend permite CORS para los orígenes locales. El login guarda el JWT en `sessionStorage` y redirige a `dashboard.html`.

## Usuarios iniciales

| Usuario | Contraseña | Acciones |
| --- | --- | --- |
| admin | admin123 | Envíos, vehículos y bitácora lateral |
| operador | operador123 | Crear envíos, asignar vehículos e iniciar tránsito |
| conductor1 | cond123 | Consultar sus envíos y marcar entregas |

El script 04 vincula `conductor1` con el conductor de licencia `LIC-EF-0001`. Para otras cuentas de conductor, asignar su `Usuario.conductor_id` correspondiente. Los envíos se filtran por ese conductor, conservando el vehículo asignado a cada envío.

## API utilizada

- `POST /api/auth/login`: autenticación y JWT con roles.
- `GET /api/envios`: envíos visibles para el usuario.
- `PUT /api/envios/{id}/estado`: actualizar estado según el rol.
- `PUT /api/envios/{id}/vehiculo`: asignar vehículo a un envío pendiente.
- `GET /api/envios/{id}/bitacora`: auditoría para ADMIN.
- `/api/vehiculos`: gestión de flota para ADMIN.
- `/api/catalogos/*`: opciones de los formularios; vehículos también alimenta el indicador de vehículos activos.

Se conservan `GET /api/envios/optimizados` y `PATCH /api/envios/{id}/estado` de los laboratorios anteriores. Los errores 400 y 404 muestran el detalle de la respuesta; 401 y 403 cierran la sesión y regresan al login.

## Verificación

```powershell
mvn clean verify
node --test tests/frontend-api.test.cjs
```

Las pruebas de backend utilizan H2 en memoria. El reporte de cobertura queda en `backend/target/site/jacoco/index.html`.
