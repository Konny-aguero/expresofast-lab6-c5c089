SET XACT_ABORT ON;
BEGIN TRANSACTION;
DECLARE @vehiculo INT = (SELECT vehiculo_id FROM dbo.Vehiculo WHERE placa = 'EF-0001');
DECLARE @conductor INT = (SELECT conductor_id FROM dbo.Conductor WHERE licencia = 'LIC-EF-0001');
IF @vehiculo IS NULL OR @conductor IS NULL
    THROW 50001, 'Ejecute primero database/03_data_seeds.sql', 1;
DECLARE @i INT = 1;
WHILE @i <= 15
BEGIN
    DECLARE @codigo VARCHAR(30) = CONCAT('LAB9-', RIGHT(CONCAT('000', @i), 3));
    IF NOT EXISTS (SELECT 1 FROM dbo.Envio WHERE codigo_rastreo = @codigo)
        INSERT INTO dbo.Envio (codigo_rastreo, destinatario, direccion_destino, peso_kg,
            costo, estado_envio, vehiculo_id, conductor_id, fecha_creacion, fecha_modificacion)
        VALUES (@codigo, CONCAT('Destinatario ', @i), CONCAT('Cartago, avenida ', @i),
            2 + @i, 1500 + @i * 100,
            CASE @i % 4 WHEN 0 THEN 'CANCELADO' WHEN 1 THEN 'PENDIENTE'
                WHEN 2 THEN 'EN_TRANSITO' ELSE 'ENTREGADO' END,
            @vehiculo, @conductor, DATEADD(MINUTE, -@i, GETDATE()), GETDATE());
    SET @i = @i + 1;
END;
COMMIT;
