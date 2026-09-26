# Laboratorio 10 — ExpresoFast

Universidad de Costa Rica  
IF0009 — Desarrollo de Software IV  
Carné: C5C089

Aplicación de gestión de envíos con Angular Standalone, Spring Boot y SQL Server.

## Funcionalidades

- Listado de envíos y actualización de estados.
- Registro de nuevas guías.
- Rastreo de envíos con barra de progreso.

## Estructura

- `expresofast-backend/`: API REST por capas.
- `expresofast-frontend/`: aplicación Angular.
- `database/`: scripts SQL.

## Ejecución

Requisitos: Java 21+, Node.js 22 y SQL Server.

Ejecutar los scripts de `database/` y configurar la conexión y JWT en el backend usando `application.properties.template`.

Backend:

    cd expresofast-backend
    .\mvnw.cmd spring-boot:run

Frontend:

    cd expresofast-frontend
    npm ci
    npm start

Frontend: http://localhost:4200  
API: http://localhost:8080/api/v1/envios

## Compilación y pruebas

Backend, desde la raíz:

    mvn clean verify

Frontend, desde su carpeta:

    npm run build