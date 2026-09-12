CREATE TABLE EmpresaLogistica (
    empresa_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    cedula_juridica VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL,
    fecha_registro DATETIME NOT NULL
);


CREATE TABLE Conductor (
    conductor_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(50) NOT NULL,
    licencia VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL
);


CREATE TABLE Vehiculo (
    vehiculo_id INT IDENTITY(1,1) PRIMARY KEY,
    placa VARCHAR(15) NOT NULL UNIQUE,
    capacidad_kg DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    empresa_id INT NOT NULL,

    FOREIGN KEY (empresa_id)
        REFERENCES EmpresaLogistica(empresa_id),

    CHECK (
        estado = 'DISPONIBLE'
        OR estado = 'EN_RUTA'
        OR estado = 'MANTENIMIENTO'
    )
);


CREATE TABLE Envio (
    envio_id INT IDENTITY(1,1) PRIMARY KEY,
    codigo_rastreo VARCHAR(30) NOT NULL UNIQUE,
    direccion_destino VARCHAR(200) NOT NULL,
    peso_kg DECIMAL(10,2) NOT NULL,
    costo DECIMAL(10,2) NOT NULL,
    estado_envio VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    vehiculo_id INT NOT NULL,
    conductor_id INT NOT NULL,
    fecha_creacion DATETIME NULL,
    fecha_modificacion DATETIME NULL,

    FOREIGN KEY (vehiculo_id)
        REFERENCES Vehiculo(vehiculo_id),

    FOREIGN KEY (conductor_id)
        REFERENCES Conductor(conductor_id),

    CHECK (
        estado_envio = 'PENDIENTE'
        OR estado_envio = 'EN_TRANSITO'
        OR estado_envio = 'ENTREGADO'
        OR estado_envio = 'CANCELADO'
    )
);

CREATE TABLE Usuario (
    usuario_id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    activo BIT NOT NULL DEFAULT 1
);

CREATE TABLE Rol (
    rol_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre_rol VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE UsuarioRol (
    usuario_id INT NOT NULL,
    rol_id INT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id) REFERENCES Rol(rol_id) ON DELETE CASCADE
);

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