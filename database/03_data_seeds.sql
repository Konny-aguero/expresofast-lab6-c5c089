-- Ejecutar en ExpresoFastC5C089_II2026 después de los esquemas.
-- Solo inserta registros faltantes; no restablece contraseñas existentes.
SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- Usuario de prueba nuevo: admin / admin123
IF NOT EXISTS (SELECT 1 FROM Rol WHERE nombre_rol='ROLE_ADMIN')
    INSERT INTO Rol(nombre_rol) VALUES('ROLE_ADMIN');
IF NOT EXISTS (SELECT 1 FROM Usuario WHERE username='admin')
    INSERT INTO Usuario(username,password_hash,nombre_completo,email,activo)
    VALUES('admin','$2a$10$IZrYKSB1qYEfC/gehKYWgOcCdrSoeM25prPBu2l1hhbHdjdhFJp9W','Administrador General','admin@expresofast.local',1);
IF NOT EXISTS (SELECT 1 FROM UsuarioRol ur JOIN Usuario u ON u.usuario_id=ur.usuario_id
               JOIN Rol r ON r.rol_id=ur.rol_id WHERE u.username='admin' AND r.nombre_rol='ROLE_ADMIN')
    INSERT INTO UsuarioRol(usuario_id,rol_id)
    SELECT u.usuario_id,r.rol_id FROM Usuario u CROSS JOIN Rol r
    WHERE u.username='admin' AND r.nombre_rol='ROLE_ADMIN';

-- Usuario de prueba nuevo: operador / operador123
IF NOT EXISTS (SELECT 1 FROM Rol WHERE nombre_rol='ROLE_OPERADOR')
    INSERT INTO Rol(nombre_rol) VALUES('ROLE_OPERADOR');
IF NOT EXISTS (SELECT 1 FROM Usuario WHERE username='operador')
    INSERT INTO Usuario(username,password_hash,nombre_completo,email,activo)
    VALUES('operador','$2a$10$ckoo4KrM7Fa1DqFOBXqcMu77f/df1dot6v.GRT16kAeCYm.ANrQtu','Operador Logístico','operador@expresofast.local',1);
IF NOT EXISTS (SELECT 1 FROM UsuarioRol ur JOIN Usuario u ON u.usuario_id=ur.usuario_id
               JOIN Rol r ON r.rol_id=ur.rol_id WHERE u.username='operador' AND r.nombre_rol='ROLE_OPERADOR')
    INSERT INTO UsuarioRol(usuario_id,rol_id)
    SELECT u.usuario_id,r.rol_id FROM Usuario u CROSS JOIN Rol r
    WHERE u.username='operador' AND r.nombre_rol='ROLE_OPERADOR';

-- Usuario de prueba nuevo: conductor1 / cond123
IF NOT EXISTS (SELECT 1 FROM Rol WHERE nombre_rol='ROLE_CONDUCTOR')
    INSERT INTO Rol(nombre_rol) VALUES('ROLE_CONDUCTOR');
IF NOT EXISTS (SELECT 1 FROM Usuario WHERE username='conductor1')
    INSERT INTO Usuario(username,password_hash,nombre_completo,email,activo)
    VALUES('conductor1','$2a$10$RnVPe4OuFIXiBYLVPKNjQ.eMOdaud.UdgQRO510C1Bibqsxp6eTCu','Carlos Conductor','conductor1@expresofast.local',1);
IF NOT EXISTS (SELECT 1 FROM UsuarioRol ur JOIN Usuario u ON u.usuario_id=ur.usuario_id
               JOIN Rol r ON r.rol_id=ur.rol_id WHERE u.username='conductor1' AND r.nombre_rol='ROLE_CONDUCTOR')
    INSERT INTO UsuarioRol(usuario_id,rol_id)
    SELECT u.usuario_id,r.rol_id FROM Usuario u CROSS JOIN Rol r
    WHERE u.username='conductor1' AND r.nombre_rol='ROLE_CONDUCTOR';

IF NOT EXISTS (SELECT 1 FROM EmpresaLogistica WHERE cedula_juridica='3-101-000001')
    INSERT INTO EmpresaLogistica(nombre,cedula_juridica,telefono,fecha_registro)
    VALUES('ExpresoFast Logística','3-101-000001','2222-0000',GETDATE());
IF NOT EXISTS (SELECT 1 FROM Vehiculo WHERE placa='EF-0001')
    INSERT INTO Vehiculo(placa,capacidad_kg,estado,empresa_id)
    SELECT 'EF-0001',1200,'DISPONIBLE',empresa_id FROM EmpresaLogistica WHERE cedula_juridica='3-101-000001';
IF NOT EXISTS (SELECT 1 FROM Conductor WHERE licencia='LIC-EF-0001')
    INSERT INTO Conductor(nombre,apellidos,licencia,telefono) VALUES('Carlos','Conductor','LIC-EF-0001','8888-0000');
COMMIT;
