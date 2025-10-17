ALTER PROCEDURE [dbo].[sp_InsertarMovimiento]
(
    @inValorDocumentoIdentidad NVARCHAR(30),
    @inIdTipoMovimiento INT,
    @inMonto DECIMAL(10,2),
    @inPostByUser NVARCHAR(50),
    @inIP NVARCHAR(50),
    @outResultCode INT OUTPUT
)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- --- VARIABLES ADICIONALES PARA LA BITÁCORA ---
        DECLARE @IdEmpleado INT, 
                @TipoAccion NVARCHAR(20), 
                @SaldoActual DECIMAL(10,2), 
                @NuevoSaldo DECIMAL(10,2),
                @NombreEmpleado NVARCHAR(100), -- NUEVO: Para guardar el nombre del empleado.
                @NombreTipoMovimiento NVARCHAR(100), -- NUEVO: Para guardar el nombre del tipo de movimiento.
                @DescripcionError NVARCHAR(500); -- NUEVO: Para obtener el mensaje de error del catálogo.

        -- Obtenemos más datos del empleado en una sola consulta
        SELECT 
            @IdEmpleado = Id, 
            @SaldoActual = SaldoVacaciones,
            @NombreEmpleado = Nombre -- NUEVO: Capturamos el nombre.
        FROM dbo.Empleado 
        WHERE ValorDocumentoIdentidad = @inValorDocumentoIdentidad AND EsActivo = 1;

        IF @IdEmpleado IS NULL
        BEGIN
            SET @outResultCode = 50012; -- Empleado no encontrado
            ROLLBACK TRANSACTION; RETURN; -- Corregido: Usar ROLLBACK en lugar de COMMIT en caso de error.
        END

        -- Obtenemos los datos del tipo de movimiento
        SELECT 
            @TipoAccion = TipoAccion,
            @NombreTipoMovimiento = Nombre -- NUEVO: Capturamos el nombre del movimiento.
        FROM dbo.TipoMovimiento WHERE Id = @inIdTipoMovimiento;

        IF @TipoAccion IS NULL
        BEGIN
            SET @outResultCode = 50009; -- TipoMovimiento no existe
            ROLLBACK TRANSACTION; RETURN; -- Corregido: Usar ROLLBACK
        END

        -- --- VALIDACIÓN CLAVE (SALDO NEGATIVO) ---
        IF @TipoAccion = 'Debito' AND (@SaldoActual - @inMonto < 0)
        BEGIN
            -- NUEVO: Obtenemos la descripción del error desde la tabla Error.
            SELECT @DescripcionError = Descripcion FROM dbo.Error WHERE Codigo = 50011;

            -- NUEVO: Construimos la descripción detallada para la bitácora según R7.
            DECLARE @DescBitacoraFallo NVARCHAR(4000) = CONCAT(
                @DescripcionError, 
                '. Empleado: ', @NombreEmpleado, ' (', @inValorDocumentoIdentidad, ')',
                ', Saldo Actual: ', CAST(@SaldoActual AS NVARCHAR(20)), 
                ', Movimiento: ', @NombreTipoMovimiento,
                ', Monto Rechazado: ', CAST(@inMonto AS NVARCHAR(20))
            );

            INSERT INTO dbo.BitacoraEvento (IdTipoEvento, Descripcion, IdPostByUser, PostInIP, PostTime)
            VALUES (13, @DescBitacoraFallo, (SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser), @inIP, GETDATE());
            
            SET @outResultCode = 50011; -- Código para "Saldo sería negativo"
            COMMIT TRANSACTION; RETURN; -- Se usa COMMIT aquí porque la operación de log fue exitosa.
        END

        -- Calcular nuevo saldo
        IF @TipoAccion = 'Credito'
            SET @NuevoSaldo = @SaldoActual + @inMonto;
        ELSE
            SET @NuevoSaldo = @SaldoActual - @inMonto;

        -- Insertar movimiento
        INSERT INTO dbo.Movimiento (IdEmpleado, IdTipoMovimiento, Fecha, Monto, NuevoSaldo, IdPostByUser, PostInIP, PostTime)
        VALUES (@IdEmpleado, @inIdTipoMovimiento, GETDATE(), @inMonto, @NuevoSaldo, (SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser), @inIP, GETDATE());

        -- Actualizar SaldoVacaciones en Empleado
        UPDATE dbo.Empleado SET SaldoVacaciones = @NuevoSaldo WHERE Id = @IdEmpleado;

        -- NUEVO: Construimos la descripción detallada para la bitácora de éxito según R7.
        DECLARE @DescBitacoraExito NVARCHAR(4000) = CONCAT(
            'Movimiento insertado. Empleado: ', @NombreEmpleado, ' (', @inValorDocumentoIdentidad, ')',
            ', Tipo: ', @NombreTipoMovimiento,
            ', Monto: ', CAST(@inMonto AS NVARCHAR(20)),
            ', Nuevo Saldo: ', CAST(@NuevoSaldo AS NVARCHAR(20))
        );

        INSERT INTO dbo.BitacoraEvento (IdTipoEvento, Descripcion, IdPostByUser, PostInIP, PostTime)
        VALUES (14, @DescBitacoraExito, (SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser), @inIP, GETDATE());

        COMMIT TRANSACTION;
        SET @outResultCode = 0;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;

        -- Añadido: Registrar el error en la tabla DBError como en otros SPs.
        INSERT INTO dbo.DBError (UserName, Number, State, Severity, Line, ProcedureName, Message, DateTime)
        SELECT SUSER_SNAME(), ERROR_NUMBER(), ERROR_STATE(), ERROR_SEVERITY(), ERROR_LINE(), ERROR_PROCEDURE(), ERROR_MESSAGE(), GETDATE();

        SET @outResultCode = 50008;
    END CATCH;

    SET NOCOUNT OFF;
END;