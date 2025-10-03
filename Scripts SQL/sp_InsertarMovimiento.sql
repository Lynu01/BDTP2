CREATE OR ALTER PROCEDURE dbo.sp_InsertarMovimiento
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

        -- Normalizar entrada
        SET @inValorDocumentoIdentidad = LTRIM(RTRIM(@inValorDocumentoIdentidad));

        DECLARE @IdEmpleado INT;
        DECLARE @TipoAccion NVARCHAR(20);
        DECLARE @NuevoSaldo DECIMAL(10,2);

        -- Buscar empleado activo
        SELECT @IdEmpleado = Id
        FROM dbo.Empleado
        WHERE ValorDocumentoIdentidad = @inValorDocumentoIdentidad
          AND EsActivo = 1;

        IF @IdEmpleado IS NULL
        BEGIN
            -- Registrar intento fallido de inserción de movimiento
            INSERT INTO dbo.BitacoraEvento (
                IdTipoEvento,
                Descripcion,
                IdPostByUser,
                PostInIP,
                PostTime
            )
            VALUES (
                13, -- Intento de insertar movimiento
                'Empleado no encontrado al insertar movimiento: ' + @inValorDocumentoIdentidad,
                (SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser),
                @inIP,
                GETDATE()
            );

            SET @outResultCode = 50012; -- Empleado no encontrado
            COMMIT TRANSACTION;
            RETURN;
        END

        -- Obtener TipoAccion (Crédito o Débito)
        SELECT @TipoAccion = TipoAccion
        FROM dbo.TipoMovimiento
        WHERE Id = @inIdTipoMovimiento;

        IF @TipoAccion IS NULL
        BEGIN
            -- Registrar intento fallido de inserción de movimiento
            INSERT INTO dbo.BitacoraEvento (
                IdTipoEvento,
                Descripcion,
                IdPostByUser,
                PostInIP,
                PostTime
            )
            VALUES (
                13, -- Intento de insertar movimiento
                'Tipo de movimiento no encontrado. IdTipoMovimiento: ' + CAST(@inIdTipoMovimiento AS NVARCHAR(10)),
                (SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser),
                @inIP,
                GETDATE()
            );

            SET @outResultCode = 50009; -- TipoMovimiento no existe
            COMMIT TRANSACTION;
            RETURN;
        END

        -- Calcular nuevo saldo
        IF @TipoAccion = 'Credito'
            SET @NuevoSaldo = (SELECT SaldoVacaciones FROM dbo.Empleado WHERE Id = @IdEmpleado) + @inMonto;
        ELSE
            SET @NuevoSaldo = (SELECT SaldoVacaciones FROM dbo.Empleado WHERE Id = @IdEmpleado) - @inMonto;

        -- Insertar movimiento
        INSERT INTO dbo.Movimiento (
             IdEmpleado,
             IdTipoMovimiento,
             Fecha,
             Monto,
             NuevoSaldo,
             IdPostByUser,
             PostInIP,
             PostTime
        )
        VALUES (
             @IdEmpleado,
             @inIdTipoMovimiento,
             GETDATE(),
             @inMonto,
             @NuevoSaldo,
             (SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser),
             @inIP,
             GETDATE()
        );

        -- Actualizar SaldoVacaciones en Empleado
        UPDATE dbo.Empleado
        SET SaldoVacaciones = @NuevoSaldo,
            PostBy = @inPostByUser,
            PostInIP = @inIP,
            PostTime = GETDATE()
        WHERE Id = @IdEmpleado;

        -- Registrar éxito en Bitácora
        INSERT INTO dbo.BitacoraEvento (
            IdTipoEvento,
            Descripcion,
            IdPostByUser,
            PostInIP,
            PostTime
        )
        VALUES (
            14, -- Insertar movimiento exitoso
            'Movimiento exitoso. Documento: ' + @inValorDocumentoIdentidad,
            (SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser),
            @inIP,
            GETDATE()
        );

        COMMIT TRANSACTION;
        SET @outResultCode = 0;

    END TRY
    BEGIN CATCH

        ROLLBACK TRANSACTION;

        INSERT INTO dbo.DBError (
            UserName,
            Number,
            State,
            Severity,
            Line,
            ProcedureName,
            Message,
            DateTime
        )
        SELECT 
            SUSER_SNAME(),
            ERROR_NUMBER(),
            ERROR_STATE(),
            ERROR_SEVERITY(),
            ERROR_LINE(),
            ERROR_PROCEDURE(),
            ERROR_MESSAGE(),
            GETDATE();

        SET @outResultCode = 50008; -- Error general
    END CATCH;

    SET NOCOUNT OFF;
END;
GO
