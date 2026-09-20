IF COL_LENGTH('dbo.Usuario', 'conductor_id') IS NULL
    ALTER TABLE Usuario ADD conductor_id INT NULL REFERENCES Conductor(conductor_id);
GO
UPDATE Usuario SET conductor_id = (SELECT conductor_id FROM Conductor WHERE licencia = 'LIC-EF-0001')
WHERE username = 'conductor1' AND conductor_id IS NULL;
GO
