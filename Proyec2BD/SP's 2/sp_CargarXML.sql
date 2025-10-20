CREATE OR ALTER PROCEDURE dbo.sp_CargarDatosDesdeXML
    @inXml XML,
    @outResultCode INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        -- ============================
        -- 1) CATALOGOS (IF NOT EXISTS)
        -- ============================

        -- Puesto
        ;WITH x AS (
            SELECT
                T.P.value('@Nombre','nvarchar(100)')     AS Nombre,
                T.P.value('@SalarioxHora','decimal(10,2)') AS Salario
            FROM @inXml.nodes('/Datos/Puestos/Puesto') AS T(P)
        )
        INSERT INTO dbo.Puesto (Nombre
		, SalarioxHora
		, PostInIP
		, PostBy
		, PostTime)
        SELECT x.Nombre
		, x.Salario
		, N'0.0.0.0'
		, N'UsuarioScripts'
		, GETDATE()
        FROM x
        WHERE NOT EXISTS (SELECT 1 FROM dbo.Puesto p WHERE p.Nombre = x.Nombre);
        -- (Puesto: Nombre, SalarioxHora, …) :contentReference[oaicite:2]{index=2}

        -- TipoEvento
        ;WITH x AS (
            SELECT
                T.E.value('@Id','int')      AS Id,
                T.E.value('@Nombre','nvarchar(100)') AS Nombre
            FROM @inXml.nodes('/Datos/TiposEvento/TipoEvento') AS T(E)
        )
        INSERT INTO dbo.TipoEvento (Id, Nombre, PostInIP, PostBy, PostTime)
        SELECT x.Id
		, x.Nombre
		, N'0.0.0.0'
		, N'UsuarioScripts'
		, GETDATE()
        FROM x
        WHERE NOT EXISTS (SELECT 1 FROM dbo.TipoEvento te WHERE te.Id = x.Id);
        -- (TipoEvento: Id PK, Nombre, …) :contentReference[oaicite:3]{index=3}

        -- TipoMovimiento
        ;WITH x AS (
            SELECT
                T.M.value('@Id','int')                 AS Id,
                T.M.value('@Nombre','nvarchar(100)')   AS Nombre,
                T.M.value('@TipoAccion','nvarchar(20)') AS TipoAccion
            FROM @inXml.nodes('/Datos/TiposMovimientos/TipoMovimiento') AS T(M)
        )
        INSERT INTO dbo.TipoMovimiento (Id
		, Nombre
		, TipoAccion
		, PostInIP
		, PostBy
		, PostTime)
        SELECT x.Id
		, x.Nombre
		, x.TipoAccion
		, N'0.0.0.0'
		, N'UsuarioScripts'
		, GETDATE()
        FROM x
        WHERE NOT EXISTS (SELECT 1 FROM dbo.TipoMovimiento tm WHERE tm.Id = x.Id);
        -- (TipoMovimiento: Id PK, Nombre, TipoAccion) :contentReference[oaicite:4]{index=4}

        -- Usuario (Username = @Nombre del XML)
        ;WITH x AS (
            SELECT
                T.U.value('@Id','int')                 AS Id,
                T.U.value('@Nombre','nvarchar(50)')    AS Username,
                T.U.value('@Pass','nvarchar(255)')     AS Password
            FROM @inXml.nodes('/Datos/Usuarios/usuario') AS T(U)
        )
        INSERT INTO dbo.Usuario (Id, Username, Password, PostInIP, PostBy, PostTime)
        SELECT x.Id
		, x.Username
		, x.Password
		, N'0.0.0.0'
		, N'UsuarioScripts'
		, GETDATE()
        FROM x
        WHERE NOT EXISTS (SELECT 1 FROM dbo.Usuario u WHERE u.Id = x.Id);
        -- (Usuario: Id PK, Username, Password) :contentReference[oaicite:5]{index=5}

        -- Error (catálogo de errores)
        ;WITH x AS (
            SELECT
                T.E.value('@Codigo','int')             AS Codigo,
                T.E.value('@Descripcion','nvarchar(500)') AS Descripcion
            FROM @inXml.nodes('/Datos/Error/errorCodigo') AS T(E)
        )
        INSERT INTO dbo.[Error] (Codigo, Descripcion)
        SELECT x.Codigo, x.Descripcion
        FROM x
        WHERE NOT EXISTS (SELECT 1 FROM dbo.[Error] e WHERE e.Codigo = x.Codigo);
        -- (Error: Codigo, Descripcion) :contentReference[oaicite:6]{index=6}:contentReference[oaicite:7]{index=7}

        -- ============================
        -- 2) EMPLEADOS (upsert + saldo=0)
        -- ============================
        ;WITH x AS (
            SELECT
                T.E.value('@Puesto','nvarchar(100)')           AS PuestoNombre,
                T.E.value('@ValorDocumentoIdentidad','nvarchar(30)') AS Doc,
                T.E.value('@Nombre','nvarchar(100)')           AS Nombre,
                T.E.value('@FechaContratacion','date')         AS FechaContratacion
            FROM @inXml.nodes('/Datos/Empleados/empleado') AS T(E)
        )
        MERGE dbo.Empleado AS target
        USING (
            SELECT 
                x.Doc
				, x.Nombre
				, x.FechaContratacion
				, p.Id AS IdPuesto
            FROM x
            JOIN dbo.Puesto p ON p.Nombre = x.PuestoNombre
        ) AS src
        ON (target.ValorDocumentoIdentidad = src.Doc)
        WHEN MATCHED THEN
            UPDATE SET target.IdPuesto = src.IdPuesto,
                       target.Nombre   = src.Nombre,
                       target.FechaContratacion = src.FechaContratacion,
                       target.SaldoVacaciones   = 0,  -- reset pedido
                       target.EsActivo          = 1
        WHEN NOT MATCHED BY TARGET THEN
            INSERT (IdPuesto, ValorDocumentoIdentidad, Nombre, FechaContratacion, SaldoVacaciones, EsActivo, PostInIP, PostBy, PostTime)
            VALUES (src.IdPuesto, src.Doc, src.Nombre, src.FechaContratacion, 0, 1, N'0.0.0.0', N'UsuarioScripts', GETDATE());
        -- (Empleado: IdPuesto FK, ValorDocumentoIdentidad, Nombre, FechaContratacion, SaldoVacaciones, EsActivo, …) :contentReference[oaicite:8]{index=8}

        -- ==================================
        -- 3) RESET CONTROLADO (como pediste)
        -- ==================================
        TRUNCATE TABLE dbo.Movimiento;      -- tiene FK a Empleado, OK si primero Movs
        TRUNCATE TABLE dbo.BitacoraEvento;  -- FK a Usuario/TipoEvento, no se toca catálogo
        -- (Movimiento, BitacoraEvento) :contentReference[oaicite:9]{index=9}

        -- ==================================
        -- 4) APLICAR MOVIMIENTOS UNO-A-UNO
        -- ==================================
        DECLARE @docId NVARCHAR(30),
                @idTipoMov INT,
                @fecha DATE,
                @monto DECIMAL(10,2),
                @rc INT;

        DECLARE cur CURSOR FAST_FORWARD FOR
            SELECT 
                T.M.value('@ValorDocId','nvarchar(30)')      AS Doc,
                T.M.value('@IdTipoMovimiento','int')         AS IdTipoMovimiento,
                T.M.value('@Fecha','date')                   AS Fecha,
                T.M.value('@Monto','decimal(10,2)')          AS Monto
            FROM @inXml.nodes('/Datos/Movimientos/movimiento') AS T(M);

        OPEN cur;
        FETCH NEXT FROM cur INTO @docId, @idTipoMov, @fecha, @monto;
        WHILE @@FETCH_STATUS = 0
        BEGIN
            EXEC dbo.sp_InsertarMovimiento
                 @inValorDocId       = @docId,
                 @inIdTipoMovimiento = @idTipoMov,
                 @inFecha            = @fecha,                  -- preservamos la fecha de negocio
                 @inMonto            = @monto,
                 @inUserName         = N'UsuarioScripts',       -- carga XML -> fijo
                 @inIP               = N'0.0.0.0',              -- carga XML -> fijo
                 @outResultCode      = @rc OUTPUT;

            -- continuamos aunque falle alguno; la traza queda en BitácoraEvento con el código del SP
            FETCH NEXT FROM cur INTO @docId, @idTipoMov, @fecha, @monto;
        END
        CLOSE cur;
        DEALLOCATE cur;

        SET @outResultCode = 0; -- todo el proceso terminó
    END TRY
    BEGIN CATCH
        INSERT INTO dbo.DBError
            (UserName
			, Number
			, State
			, Severity
			, Line
			, ProcedureName
			, Message
			, DateTime)
        VALUES
            (N'sp_CargarDatosDesdeXML'
			, ERROR_NUMBER()
			, ERROR_STATE()
			, ERROR_SEVERITY()
			, ERROR_LINE()
			, ERROR_PROCEDURE()
			, ERROR_MESSAGE()
			, GETDATE());

        SET @outResultCode = 50008; -- Error de base de datos
    END CATCH
END
GO
