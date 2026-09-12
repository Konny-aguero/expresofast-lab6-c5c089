-- Seleccione ExpresoFastC5C089_II2026 en SSMS antes de ejecutar.
IF OBJECT_ID(N'dbo.Usuario', N'U') IS NULL
CREATE TABLE Usuario (
    usuario_id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    activo BIT NOT NULL DEFAULT 1
);

IF OBJECT_ID(N'dbo.Rol', N'U') IS NULL
CREATE TABLE Rol (
    rol_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre_rol VARCHAR(30) NOT NULL UNIQUE
);

IF OBJECT_ID(N'dbo.UsuarioRol', N'U') IS NULL
CREATE TABLE UsuarioRol (
    usuario_id INT NOT NULL,
    rol_id INT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id) REFERENCES Rol(rol_id) ON DELETE CASCADE
);

IF OBJECT_ID(N'dbo.BitacoraEnvio', N'U') IS NULL
CREATE TABLE BitacoraEnvio (
    bitacora_id INT IDENTITY(1,1) PRIMARY KEY,
    envio_id INT NOT NULL,
    estado_anterior VARCHAR(20) NOT NULL,
    estado_nuevo VARCHAR(20) NOT NULL,
    fecha_cambio DATETIME NOT NULL DEFAULT GETDATE(),
    usuario_id INT NOT NULL,
    observaciones VARCHAR(250) NULL,
    FOREIGN KEY (envio_id) REFERENCES Envio(envio_id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);