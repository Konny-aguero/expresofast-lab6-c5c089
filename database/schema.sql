IF COL_LENGTH('dbo.Envio', 'destinatario') IS NULL
    ALTER TABLE dbo.Envio ADD destinatario VARCHAR(100) NULL;
GO
CREATE OR ALTER PROCEDURE dbo.SP_OBTENER_ENVIOS_POR_ESTADO
    @pEstado VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT * FROM dbo.Envio
    WHERE estado_envio = @pEstado
    ORDER BY fecha_creacion DESC, envio_id DESC;
END;
GO
