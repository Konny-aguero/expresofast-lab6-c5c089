# Laboratorio 11 — ExpresoFast

Universidad de Costa Rica  
IF0009 — Desarrollo de Software IV  
Carné: C5C089

Aplicación de gestión de envíos con Angular Standalone, Spring Boot 3 y SQL Server.

## Funcionalidades

- Listado de envíos y actualización de estados.
- Registro de envíos con múltiples paquetes y fechas operativas.
- Validación de fechas y de números de rastreo duplicados.
- Rastreo de envíos con barra de progreso.

## Estructura

- `expresofast-backend/`: API REST por capas.
- `expresofast-frontend/`: aplicación Angular.
- `database/`: scripts SQL.

## Ejecución

Requisitos: Java 21+, Node.js 22 y SQL Server.

Para una base existente del laboratorio 10, ejecutar `database/06_lab11_paquetes.sql`.
El script adapta la referencia `ENVIOS(id BIGINT)` del enunciado a `dbo.Envio(envio_id INT)`,
que es la tabla existente, conservando la relación 1:N y `ON DELETE CASCADE`.
Las fechas permiten NULL para conservar los envíos anteriores; son obligatorias en el nuevo registro.

Para una instalación nueva, ejecutar `database/01_schema_lab5.sql`,
`02_schema_lab6_extension.sql`, `03_data_seeds.sql`, `04_lab8_usuario_conductor.sql`,
`05_lab10_envios_sin_asignacion.sql` y `06_lab11_paquetes.sql`, en ese orden.
Configurar la conexión y JWT en el backend usando `application.properties.template`.

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
## UX y escalabilidad

Un FormArray representa una colección de controles cuyo tamaño cambia con los paquetes
que el operador agrega o elimina. Cada paquete tiene un FormGroup con descripción y peso:
sus valores, errores y estados touched, dirty y valid pertenecen al modelo del formulario.
Angular propaga la validez hacia el formulario principal, por lo que basta consultar su
estado para bloquear el registro. El operador ve solamente los paquetes que necesita y
puede corregir el error en el bloque correspondiente, sin un límite artificial de diez paquetes.

Diez grupos estáticos ocultos requieren manejar por separado su visibilidad, si sus valores
se envían y si deben validarse. Ocultar un campo no elimina su control ni su valor; mantener
esas condiciones duplica lógica y facilita errores. Con FormArray, push y removeAt actualizan
la colección que se muestra y se envía. La función nuevoPaquete centraliza las reglas y los
controles tipados hacen que pesoKg sea FormControl<number>: asignarle un string produce
un error de TypeScript. Esto simplifica ampliar el formulario y probar las reglas sin depender
de la estructura del DOM.

## Ciclo de eventos

El validador cruzado fechasValidas se ejecuta sincrónicamente durante la actualización del
FormGroup, en la pila de llamadas de JavaScript. Compara las fechas con Date.parse y devuelve
inmediatamente null o el error fechasInvalidas; dos fechas iguales también son inválidas.
No realiza I/O ni necesita esperar otra tarea del Event Loop.

El validador trackingDisponible inicia un Observable: timer espera 300 ms y switchMap realiza
la petición HTTP. La espera y la red no bloquean la pila, de modo que el navegador puede
seguir atendiendo al usuario. Al recibir el resultado, el callback convierte el booleano en
null o en trackingTomado. Una Promise resuelta programa su continuación como microtarea;
un Observable no es por sí mismo una microtarea: su planificación depende de la fuente,
en este caso el temporizador y HttpClient. El Event Loop procesa las tareas y vacía las
microtareas cuando la pila queda libre.

Angular exige un Observable o Promise porque la respuesta no está disponible cuando termina
la invocación inicial. Así puede mantener el control en PENDING y actualizar los errores al
recibir el resultado. Angular cancela la suscripción anterior si cambia el valor, evitando
aplicar una respuesta de un tracking anterior. El botón se bloquea tanto en INVALID como
en PENDING; si la consulta falla, trackingNoVerificado también impide enviar. La validación
asíncrona mejora la experiencia, pero la restricción UNIQUE de SQL Server sigue siendo
necesaria para impedir duplicados cuando dos usuarios registran el mismo tracking a la vez.
