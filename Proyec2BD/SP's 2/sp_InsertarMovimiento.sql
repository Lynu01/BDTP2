CREATE OR ALTER PROCEDURE dbo.sp_InsertarMovimiento
    @inValorDocId       NVARCHAR(30),
    @inIdTipoMovimiento INT,
    @inFecha            DATE,
    @inMonto            DECIMAL(10,2),
    @inUserName         NVARCHAR(50),   -- se resuelve a Usuario.Id
    @inIP               NVARCHAR(50),
    @outResultCode      INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        DECLARE @idEmpleado   INT;
        DECLARE @empleadoNom  NVARCHAR(100);
        DECLARE @saldoActual  DECIMAL(10,2);
        DECLARE @nuevoSaldo   DECIMAL(10,2);
        DECLARE @tipoAccion   NVARCHAR(20);
        DECLARE @tipoMovNom   NVARCHAR(100);
        DECLARE @idUsuario    INT;

        -- Resolver usuario (FK)
        SELECT @idUsuario = u.Id
        FROM dbo.Usuario AS u
        WHERE u.Username = @inUserName;

        IF @idUsuario IS NULL
        BEGIN
            SET @outResultCode = 50001; -- Username no existe
            RETURN;
        END

        -- Resolver empleado por documento (FK y datos)
        SELECT 
            @idEmpleado  = e.Id,
            @empleadoNom = e.Nombre,
            @saldoActual = e.SaldoVacaciones
        FROM dbo.Empleado AS e
        WHERE e.ValorDocumentoIdentidad = @inValorDocId
          AND e.EsActivo = 1;

        IF @idEmpleado IS NULL
        BEGIN
            -- No vamos a inventar código nuevo; usamos 50008 para datos inválidos / no encontrado
            SET @outResultCode = 50008; -- Error de base de datos / datos inválidos
            RETURN;
        END

        -- TipoMovimiento (acción)
        SELECT 
            @tipoAccion = tm.TipoAccion,
            @tipoMovNom = tm.Nombre
        FROM dbo.TipoMovimiento AS tm
        WHERE tm.Id = @inIdTipoMovimiento;

        IF @tipoAccion IS NULL
        BEGIN
            SET @outResultCode = 50008; -- TipoMovimiento inválido
            RETURN;
        END

        -- Calcular nuevo saldo
        IF @tipoAccion = N'Credito'
            SET @nuevoSaldo = @saldoActual + @inMonto;
        ELSE
            SET @nuevoSaldo = @saldoActual - @inMonto;

        -- Valida saldo no negativo
        IF @nuevoSaldo < 0
        BEGIN
            -- Bitácora intento fallido (IdTipoEvento=13)
            INSERT INTO dbo.BitacoraEvento
                (IdTipoEvento
				, Descripcion
				, IdPostByUser
				, PostInIP
				, PostTime)
            VALUES
                (13,
                 CONCAT(
                    N'Intento de insertar movimiento (FALLIDO). '
                    , N'Doc=', @inValorDocId
					, N', Nombre=', @empleadoNom
                    , N', TipoMovimientoId=', @inIdTipoMovimiento
					, N' (', @tipoMovNom, N', Accion=', @tipoAccion, N')'
                    , N', Fecha=', CONVERT(NVARCHAR(19), @inFecha, 120)
                    , N', Monto=', FORMAT(@inMonto, 'N2')
                    , N', SaldoAnterior=', FORMAT(@saldoActual, 'N2')
                    , N', NuevoSaldo=', FORMAT(@nuevoSaldo, 'N2')
                    , N'. Usuario=', @inUserName, N', IP=', @inIP
                 ),
                 @idUsuario, @inIP, GETDATE());

            SET @outResultCode = 50011; -- Monto deja saldo negativo
            RETURN;
        END

        BEGIN TRANSACTION;
            -- Insertar movimiento (con NuevoSaldo)
            INSERT INTO dbo.Movimiento
                (idEmpleado
				, idTipoMovimiento
				, Fecha
				, Monto
				, NuevoSaldo
				, IdPostByUser
				, PostInIP
				, PostTime)
            VALUES
                (@idEmpleado
				, @inIdTipoMovimiento
				, @inFecha
				, @inMonto
				, @nuevoSaldo
				, @idUsuario
				, @inIP
				, GETDATE());

            -- Actualizar saldo del empleado
            UPDATE dbo.Empleado
            SET SaldoVacaciones = @nuevoSaldo
            WHERE Id = @idEmpleado;

            -- Bitácora exitosa (IdTipoEvento=14)
            INSERT INTO dbo.BitacoraEvento
                (IdTipoEvento
				, Descripcion
				, IdPostByUser
				, PostInIP
				, PostTime)
            VALUES
                (14,
                 CONCAT(
                    N'Insertar movimiento (EXITOSO). '
					, N'Doc=', @inValorDocId
					, N', Nombre=', @empleadoNom
					, N', TipoMovimientoId=', @inIdTipoMovimiento
					, N' (', @tipoMovNom, N', Accion=', @tipoAccion, N')'
					, N', Fecha=', CONVERT(NVARCHAR(19), @inFecha, 120)
					, N', Monto=', FORMAT(@inMonto, 'N2')
					, N', SaldoAnterior=', FORMAT(@saldoActual, 'N2')
					, N', NuevoSaldo=', FORMAT(@nuevoSaldo, 'N2')
					, N'. Usuario=', @inUserName
					, N', IP=', @inIP
                 ),
                 @idUsuario, @inIP, GETDATE());
        COMMIT TRANSACTION;

        SET @outResultCode = 0; -- éxito
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

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
            (@inUserName
			, ERROR_NUMBER()
			, ERROR_STATE()
			, ERROR_SEVERITY()
			, ERROR_LINE()
			, ERROR_PROCEDURE()
			, ERROR_MESSAGE()
			, GETDATE());

        SET @outResultCode = 50008; -- error de base de datos
    END CATCH
END
GO
