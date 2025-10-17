/****** Object:  StoredProcedure [dbo].[sp_InsertarMovimiento]    Script Date: 10/17/2025 10:10:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

ALTER   PROCEDURE [dbo].[sp_InsertarMovimiento]
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

        DECLARE @IdEmpleado INT, @TipoAccion NVARCHAR(20), @SaldoActual DECIMAL(10,2), @NuevoSaldo DECIMAL(10,2);

        SELECT @IdEmpleado = Id, @SaldoActual = SaldoVacaciones FROM dbo.Empleado WHERE ValorDocumentoIdentidad = @inValorDocumentoIdentidad AND EsActivo = 1;

        IF @IdEmpleado IS NULL
        BEGIN
            SET @outResultCode = 50012; -- Empleado no encontrado
            COMMIT TRANSACTION; RETURN;
        END

        SELECT @TipoAccion = TipoAccion FROM dbo.TipoMovimiento WHERE Id = @inIdTipoMovimiento;

        IF @TipoAccion IS NULL
        BEGIN
            SET @outResultCode = 50009; -- TipoMovimiento no existe
            COMMIT TRANSACTION; RETURN;
        END

        -- --- VALIDACIÓN CLAVE AÑADIDA ---
        IF @TipoAccion = 'Debito' AND (@SaldoActual - @inMonto < 0)
        BEGIN
            -- El débito resultaría en un saldo negativo. Rechazar la operación.
            INSERT INTO dbo.BitacoraEvento (IdTipoEvento, Descripcion, IdPostByUser, PostInIP, PostTime)
            VALUES (13, 'Intento de insertar movimiento rechazado (saldo negativo). Documento: ' + @inValorDocumentoIdentidad, (SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser), @inIP, GETDATE());
            
            SET @outResultCode = 50011; -- Código para "Saldo sería negativo"
            COMMIT TRANSACTION; RETURN;
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

        INSERT INTO dbo.BitacoraEvento (IdTipoEvento, Descripcion, IdPostByUser, PostInIP, PostTime)
        VALUES (14, 'Movimiento exitoso. Documento: ' + @inValorDocumentoIdentidad, (SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser), @inIP, GETDATE());

        COMMIT TRANSACTION;
        SET @outResultCode = 0;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        SET @outResultCode = 50008;
    END CATCH;

    SET NOCOUNT OFF;
END;
