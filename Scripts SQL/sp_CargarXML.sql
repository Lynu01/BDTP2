
-- Creamos el SP nuevo
CREATE OR ALTER PROCEDURE dbo.sp_CargarDatosDesdeXML
(
    @inXmlData XML,
    @outResultCode INT OUTPUT
)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY

        BEGIN TRANSACTION;

		-- Limpiar tablas
		DELETE FROM dbo.Movimiento;
		DELETE FROM dbo.Empleado;
		DELETE FROM dbo.Usuario;
		DELETE FROM dbo.TipoMovimiento;
		DELETE FROM dbo.TipoEvento;
		DELETE FROM dbo.Puesto;
		DELETE FROM dbo.Feriado;
		DELETE FROM dbo.BitacoraEvento;
		DELETE FROM dbo.DBError;
		DELETE FROM dbo.Error;

		-- (Opcional) Resetear IDENTITY
		DBCC CHECKIDENT ('dbo.Empleado', RESEED, 0);
		DBCC CHECKIDENT ('dbo.Movimiento', RESEED, 0);
		DBCC CHECKIDENT ('dbo.Feriado', RESEED, 0);
		DBCC CHECKIDENT ('dbo.Puesto', RESEED, 0);
		--DBCC CHECKIDENT ('dbo.Usuario', RESEED, 0);

        -- Declaración de tabla variable para almacenar los datos del XML
        DECLARE @DatosTabla TABLE (
             TipoDato NVARCHAR(50)
            ,Campo1 NVARCHAR(255) -- Nombre
            ,Campo2 NVARCHAR(255) -- SalarioxHora
            ,Campo3 NVARCHAR(255) -- Id
            ,Campo4 NVARCHAR(255) -- TipoAccion
            ,Campo5 NVARCHAR(255) -- ValorDocumentoIdentidad
            ,Campo6 NVARCHAR(255) -- FechaContratacion
            ,Campo7 NVARCHAR(255) -- Password o SaldoVacaciones
            ,Campo8 NVARCHAR(255) -- IdTipoMovimiento o EsActivo
            ,Campo9 NVARCHAR(255) -- Monto
            ,Campo10 NVARCHAR(255) -- PostByUser
            ,Campo11 NVARCHAR(255) -- IdPuesto
            ,Campo12 NVARCHAR(255) -- PostInIP
            ,Campo13 NVARCHAR(255) -- PostTime
            ,Campo14 NVARCHAR(255) -- Fecha (para movimientos)
            ,Campo15 NVARCHAR(255)  -- Descripcion (opcional para eventos)
			,Campo16 NVARCHAR(255) -- SaldoVacaciones
			,Campo17 NVARCHAR(255) -- EsActivo
			,Campo18 NVARCHAR(255) -- Codigo

        );

        -- Cargar los datos del XML a la tabla variable
        INSERT INTO @DatosTabla (
             TipoDato
            ,Campo1
            ,Campo2
            ,Campo3
            ,Campo4
            ,Campo5
            ,Campo6
            ,Campo7
            ,Campo8
            ,Campo9
            ,Campo10
            ,Campo11
            ,Campo12
			,Campo13
			,Campo14
			,Campo15
			,Campo16
			,Campo17
			,Campo18
        )
        SELECT 
             LOWER(T.X.value('local-name(.)', 'NVARCHAR(50)')) AS TipoDato
            ,T.X.value('@Nombre', 'NVARCHAR(255)')						--Campo1
            ,T.X.value('@SalarioxHora', 'NVARCHAR(255)')				--Campo2
            ,T.X.value('@Id', 'NVARCHAR(255)')							--Campo3
            ,T.X.value('@TipoAccion', 'NVARCHAR(255)')					--Campo4

            ,COALESCE(
				T.X.value('@ValorDocumentoIdentidad', 'NVARCHAR(255)'),
				T.X.value('@ValorDocId', 'NVARCHAR(255)')
			)															--Campo5
            ,T.X.value('@FechaContratacion', 'NVARCHAR(255)')			--Campo6
            ,T.X.value('@Pass', 'NVARCHAR(255)')						--Campo7
            ,T.X.value('@IdTipoMovimiento', 'NVARCHAR(255)')			--Campo8
            ,T.X.value('@Monto', 'NVARCHAR(255)')						--Campo9
            ,T.X.value('@PostByUser', 'NVARCHAR(255)')					--Campo10
			,T.X.value('@IdPuesto', 'NVARCHAR(255)')					--Campo11
            ,T.X.value('@PostInIP', 'NVARCHAR(255)')					--Campo12
            ,T.X.value('@PostTime', 'NVARCHAR(255)')					--Campo13
			,T.X.value('@Fecha', 'NVARCHAR(255)')						--Campo14
			,T.X.value('@Descripcion', 'NVARCHAR(255)')					--Campo15
			,T.X.value('@SaldoVacaciones', 'NVARCHAR(255)')				--Campo16
			,T.X.value('@EsActivo', 'NVARCHAR(255)')					--Campo17
			,T.X.value('@Codigo', 'NVARCHAR(255)')						--Campo18
        FROM @inXmlData.nodes('/Datos/*/*') AS T(X);

		-- Activar IDENTITY_INSERT para respetar los Id del XML
		SET IDENTITY_INSERT dbo.Puesto ON;
        -- Insertar datos en Puesto
        INSERT INTO dbo.Puesto (
		Id
		,Nombre
		,SalarioxHora
		,PostInIP
		,PostBy
		,PostTime
		)
        SELECT 
			CAST(DT.Campo3 AS INT)
            ,DT.Campo1
            ,CAST(DT.Campo2 AS DECIMAL(10,2))
			,DT.Campo12             
			,DT.Campo10             
			,CAST(DT.Campo13 AS DATETIME) 
        FROM @DatosTabla DT
        WHERE DT.TipoDato = 'puesto';

		SET IDENTITY_INSERT dbo.Puesto OFF;

        -- Insertar datos en TipoEvento
        INSERT INTO dbo.TipoEvento (
		Id
		,Nombre
		,PostInIP
		,PostBy
		,PostTime
		)
        SELECT 
             CAST(DT.Campo3 AS INT)
            ,DT.Campo1
			,DT.Campo12             
			,DT.Campo10             
			,CAST(DT.Campo13 AS DATETIME)
        FROM @DatosTabla DT
        WHERE DT.TipoDato = 'tipoevento';


        -- Insertar datos en TipoMovimiento
        INSERT INTO dbo.TipoMovimiento (
		Id
		,Nombre
		,TipoAccion
		,PostInIP
		,PostBy
		,PostTime
		)
        SELECT 
             CAST(DT.campo3 AS INT)
            ,DT.Campo1
            ,DT.Campo4
			,DT.Campo12
			,DT.Campo10
			,CAST(DT.Campo13 AS DATETIME)
        FROM @DatosTabla DT
        WHERE DT.TipoDato = 'tipomovimiento';

        -- Insertar datos en Usuario
        INSERT INTO dbo.Usuario (
		Id
		,Username
		,Password
		,PostInIP
		,PostBy
		,PostTime
		)
        SELECT 
             CAST(DT.Campo3 AS INT)
            ,DT.Campo1
            ,DT.Campo7
			,DT.Campo12
			,DT.Campo10
			,CAST(DT.Campo13 AS DATETIME)
        FROM @DatosTabla DT
        WHERE DT.TipoDato = 'usuario';

        -- Insertar datos en Empleado
		INSERT INTO dbo.Empleado (
			 IdPuesto
			,ValorDocumentoIdentidad
			,Nombre
			,FechaContratacion
			,SaldoVacaciones
			,EsActivo
			,PostInIP
			,PostBy
			,PostTime
		)
		SELECT 
			 CAST(DT.Campo11 AS INT)
			,DT.Campo5
			,DT.Campo1
			,CAST(DT.Campo6 AS DATE)
			,CAST(DT.Campo16 AS INT)
			,CAST(DT.Campo17 AS BIT)
			,DT.Campo12
			,DT.Campo10
			,CAST(DT.Campo13 AS DATETIME)
		FROM @DatosTabla DT
		WHERE DT.TipoDato = 'empleado';

		-- Crear una tabla de depuración para revisar los movimientos extraídos
		IF OBJECT_ID('dbo.Debug_DatosMovimientos', 'U') IS NOT NULL
			DROP TABLE dbo.Debug_DatosMovimientos;

		SELECT *
		INTO dbo.Debug_DatosMovimientos
		FROM @DatosTabla
		WHERE TipoDato = 'movimiento';


        -- Insertar datos en Movimiento
        INSERT INTO dbo.Movimiento (
             IdEmpleado
            ,IdTipoMovimiento
            ,Fecha
            ,Monto
            ,NuevoSaldo
            ,IdPostByUser
            ,PostInIP
            ,PostTime
        )
        SELECT 
			e.Id
             ,CAST(DT.Campo8 AS INT)
            ,CAST(DT.Campo14 AS DATE)
			,CAST(DT.Campo9 AS DECIMAL(10,2))
			,CAST(DT.Campo9 AS DECIMAL(10,2))
			,(SELECT u.Id FROM dbo.Usuario u WHERE u.Username = DT.Campo10)
			,DT.Campo12
			,CAST(DT.Campo13 AS DATETIME)
		FROM @DatosTabla DT
		INNER JOIN dbo.Empleado e ON e.ValorDocumentoIdentidad = DT.Campo5
        WHERE DT.tipoDato = 'movimiento';

        -- Insertar errores
        INSERT INTO dbo.Error (
		Codigo
		,Descripcion
		)
        SELECT 
             CAST(DT.campo18 AS INT)
            ,DT.campo15
        FROM @DatosTabla DT
        WHERE DT.tipoDato = 'error';

		-- Insertar datos en Feriado
		INSERT INTO dbo.Feriado (
			 Fecha
			,Descripcion
			,PostBy
            ,PostInIP
            ,PostTime
		)
		SELECT 
			 CAST(DT.Campo14 AS DATE) -- La fecha viene en Campo6
			,DT.Campo15              -- La descripción viene en Campo15
			,DT.Campo10
			,DT.Campo12
			,CAST(DT.Campo13 AS DATETIME)
		FROM @DatosTabla DT
		WHERE DT.TipoDato = 'feriado';

        COMMIT TRANSACTION;

        SET @outResultCode = 0;

    END TRY
    BEGIN CATCH

        ROLLBACK TRANSACTION;

        -- Manejo de errores
        INSERT INTO dbo.DBError (
             UserName
            ,Number
            ,State
            ,Severity
            ,Line
            ,ProcedureName
            ,Message
            ,DateTime
        )
        SELECT 
             SUSER_SNAME()
            ,ERROR_NUMBER()
            ,ERROR_STATE()
            ,ERROR_SEVERITY()
            ,ERROR_LINE()
            ,ERROR_PROCEDURE()
            ,ERROR_MESSAGE()
            ,GETDATE();

        SET @outResultCode = 50008; -- Error general de base de datos

    END CATCH;

    SET NOCOUNT OFF;
END;
