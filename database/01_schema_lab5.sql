-- Seleccione ExpresoFastC5C089_II2026 en SSMS antes de ejecutar.
IF OBJECT_ID(N'dbo.EmpresaLogistica', N'U') IS NULL
CREATE TABLE EmpresaLogistica (
    empresa_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    cedula_juridica VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL,
    fecha_registro DATETIME NOT NULL
);


IF OBJECT_ID(N'dbo.Conductor', N'U') IS NULL
CREATE TABLE Conductor (
    conductor_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(50) NOT NULL,
    licencia VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL
);


IF OBJECT_ID(N'dbo.Vehiculo', N'U') IS NULL
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


IF OBJECT_ID(N'dbo.Envio', N'U') IS NULL
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
