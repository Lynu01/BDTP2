/****** Object:  StoredProcedure [dbo].[sp_CargarDatosDesdeXML]    Script Date: 10/17/2025 12:25:20 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[sp_CargarDatosDesdeXML]
(
    @inXmlData XML,
    @outResultCode INT OUTPUT
)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Limpiar tablas en el orden correcto para evitar conflictos de Foreign Key
        DELETE FROM dbo.Movimiento;
        DELETE FROM dbo.BitacoraEvento;
        DELETE FROM dbo.Empleado;
        DELETE FROM dbo.Puesto;
        DELETE FROM dbo.Usuario;
        DELETE FROM dbo.TipoMovimiento;
        DELETE FROM dbo.TipoEvento;
        DELETE FROM dbo.Feriado;
        DELETE FROM dbo.DBError;
        DELETE FROM dbo.Error;

        -- Resetear el contador de las tablas con columnas IDENTITY
        DBCC CHECKIDENT ('dbo.Puesto', RESEED, 0);
        DBCC CHECKIDENT ('dbo.Empleado', RESEED, 0);
        DBCC CHECKIDENT ('dbo.Movimiento', RESEED, 0);
        DBCC CHECKIDENT ('dbo.BitacoraEvento', RESEED, 0);
        DBCC CHECKIDENT ('dbo.DBError', RESEED, 0);
        DBCC CHECKIDENT ('dbo.Error', RESEED, 0);
        DBCC CHECKIDENT ('dbo.Feriado', RESEED, 0);

        -- Tabla temporal para almacenar datos del XML
        DECLARE @DatosTabla TABLE (
            TipoDato NVARCHAR(50), Campo1 NVARCHAR(255), Campo2 NVARCHAR(255),
            Campo3 NVARCHAR(255),   Campo4 NVARCHAR(255), Campo5 NVARCHAR(255),
            Campo6 NVARCHAR(255),   Campo7 NVARCHAR(255), Campo8 NVARCHAR(255),
            Campo9 NVARCHAR(255),   Campo10 NVARCHAR(255), Campo11 NVARCHAR(255),
            Campo12 NVARCHAR(255),  Campo13 NVARCHAR(255), Campo14 NVARCHAR(255),
            Campo15 NVARCHAR(255),  Campo18 NVARCHAR(255)
        );

        -- Cargar datos del XML a la tabla temporal
        INSERT INTO @DatosTabla (TipoDato, Campo1, Campo2, Campo3, Campo4, Campo5, Campo6, Campo7, Campo8, Campo9, Campo10, Campo11, Campo12, Campo13, Campo14, Campo15, Campo18)
        SELECT 
            LOWER(T.X.value('local-name(.)', 'NVARCHAR(50)')),
            T.X.value('@Nombre', 'NVARCHAR(255)'), T.X.value('@SalarioxHora', 'NVARCHAR(255)'),
            T.X.value('@Id', 'NVARCHAR(255)'), T.X.value('@TipoAccion', 'NVARCHAR(255)'),
            COALESCE(T.X.value('@ValorDocumentoIdentidad', 'NVARCHAR(255)'), T.X.value('@ValorDocId', 'NVARCHAR(255)')),
            T.X.value('@FechaContratacion', 'NVARCHAR(255)'), T.X.value('@Pass', 'NVARCHAR(255)'),
            T.X.value('@IdTipoMovimiento', 'NVARCHAR(255)'), T.X.value('@Monto', 'NVARCHAR(255)'),
            T.X.value('@PostByUser', 'NVARCHAR(255)'), COALESCE(T.X.value('@IdPuesto', 'NVARCHAR(255)'), T.X.value('@Puesto', 'NVARCHAR(255)')),
            T.X.value('@PostInIP', 'NVARCHAR(255)'), T.X.value('@PostTime', 'NVARCHAR(255)'),
            T.X.value('@Fecha', 'NVARCHAR(255)'), T.X.value('@Descripcion', 'NVARCHAR(255)'),
            T.X.value('@Codigo', 'NVARCHAR(255)')
        FROM @inXmlData.nodes('/Datos/*/*') AS T(X);

        -- Insertar datos en Puesto (No necesita IDENTITY_INSERT)
        INSERT INTO dbo.Puesto (Nombre, SalarioxHora)
        SELECT Campo1, CAST(Campo2 AS DECIMAL(10,2)) FROM @DatosTabla WHERE TipoDato = 'puesto';

        -- Insertar datos en TipoEvento, Usuario, TipoMovimiento (No tienen IDENTITY, se inserta directo)
        INSERT INTO dbo.TipoEvento (Id, Nombre)
        SELECT CAST(Campo3 AS INT), Campo1 FROM @DatosTabla WHERE TipoDato = 'tipoevento';

        INSERT INTO dbo.TipoMovimiento (Id, Nombre, TipoAccion)
        SELECT CAST(Campo3 AS INT), Campo1, Campo4 FROM @DatosTabla WHERE TipoDato = 'tipomovimiento';
        
        INSERT INTO dbo.Usuario (Id, Username, Password)
        SELECT CAST(Campo3 AS INT), Campo1, Campo7 FROM @DatosTabla WHERE TipoDato = 'usuario';

        -- Insertar datos en Empleado
        INSERT INTO dbo.Empleado (IdPuesto, ValorDocumentoIdentidad, Nombre, FechaContratacion, SaldoVacaciones, EsActivo)
        SELECT p.Id, DT.Campo5, DT.Campo1, CAST(DT.Campo6 AS DATE), 0, 1 -- Saldo de vacaciones se inicializa en 0
        FROM @DatosTabla DT
        INNER JOIN dbo.Puesto p ON p.Nombre = DT.Campo11 WHERE DT.TipoDato = 'empleado';

        /*
        -- --- SECCIÓN CORREGIDA ---
        -- La carga de movimientos en lote se deshabilita para cumplir con el requisito
        -- de que los saldos se calculen con cada inserción individual desde la aplicación.
        --
        -- Insertar datos en Movimiento
        INSERT INTO dbo.Movimiento (IdEmpleado, IdTipoMovimiento, Fecha, Monto, NuevoSaldo, IdPostByUser, PostInIP, PostTime)
        SELECT e.Id, CAST(DT.Campo8 AS INT), CAST(DT.Campo14 AS DATE), CAST(DT.Campo9 AS DECIMAL(10,2)), 0, u.Id, DT.Campo12, CAST(DT.Campo13 AS DATETIME)
        FROM @DatosTabla DT
        INNER JOIN dbo.Empleado e ON e.ValorDocumentoIdentidad = DT.Campo5
        INNER JOIN dbo.Usuario u ON u.Username = DT.Campo10 WHERE DT.TipoDato = 'movimiento';
        */

        -- Insertar Errores
        INSERT INTO dbo.Error (Codigo, Descripcion)
        SELECT CAST(Campo18 AS INT), Campo15 FROM @DatosTabla WHERE TipoDato = 'errorcodigo';

        COMMIT TRANSACTION;
        SET @outResultCode = 0;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        INSERT INTO dbo.DBError (UserName, Number, State, Severity, Line, ProcedureName, Message, DateTime)
        SELECT SUSER_SNAME(), ERROR_NUMBER(), ERROR_STATE(), ERROR_SEVERITY(), ERROR_LINE(), ERROR_PROCEDURE(), ERROR_MESSAGE(), GETDATE();
        SET @outResultCode = 50008;
    END CATCH;

    SET NOCOUNT OFF;
END;
