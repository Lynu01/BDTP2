CREATE OR ALTER PROCEDURE dbo.sp_EliminarEmpleado
(
    @inValorDocumentoIdentidad NVARCHAR(30),
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
        DECLARE @NombreEmpleado NVARCHAR(100);

        -- Buscar el empleado
        SELECT 
            @IdEmpleado = Id,
            @NombreEmpleado = Nombre
        FROM dbo.Empleado
        WHERE ValorDocumentoIdentidad = @inValorDocumentoIdentidad
          AND EsActivo = 1;

        IF @IdEmpleado IS NULL
        BEGIN
            -- Registro en Bitácora: empleado no encontrado
            INSERT INTO dbo.BitacoraEvento (
                IdTipoEvento,
                Descripcion,
                IdPostByUser,
                PostInIP,
                PostTime
            )
            VALUES (
                9, -- Eliminación no exitosa
                'No se encontró empleado para eliminar. Documento: ' + @inValorDocumentoIdentidad,
                (SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser),
                @inIP,
                GETDATE()
            );

            SET @outResultCode = 50012; -- Empleado no encontrado
            COMMIT TRANSACTION;
            RETURN;
        END

        -- Borrado lógico
        UPDATE dbo.Empleado
        SET 
            EsActivo = 0,
            PostBy = @inPostByUser,
            PostInIP = @inIP,
            PostTime = GETDATE()
        WHERE Id = @IdEmpleado;

        -- Registro en Bitácora: éxito
        INSERT INTO dbo.BitacoraEvento (
            IdTipoEvento,
            Descripcion,
            IdPostByUser,
            PostInIP,
            PostTime
        )
        VALUES (
            10, -- Eliminación exitosa
            'Empleado eliminado. Documento: ' + @inValorDocumentoIdentidad + ', Nombre: ' + @NombreEmpleado,
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
