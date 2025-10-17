/****** Object:  StoredProcedure [dbo].[sp_EliminarEmpleado]    Script Date: 10/17/2025 10:09:44 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER   PROCEDURE [dbo].[sp_EliminarEmpleado]
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
        -- El PDF no especifica una transacción aquí, pero es una buena práctica
        BEGIN TRANSACTION;

        DECLARE @IdEmpleado INT;

        -- Buscar el empleado activo
        SELECT @IdEmpleado = Id
        FROM dbo.Empleado
        WHERE ValorDocumentoIdentidad = @inValorDocumentoIdentidad AND EsActivo = 1;

        IF @IdEmpleado IS NULL
        BEGIN
            -- El empleado no existe o ya está inactivo, no se puede eliminar.
            -- Registramos el intento en la bitácora.
            INSERT INTO dbo.BitacoraEvento (IdTipoEvento, Descripcion, IdPostByUser, PostInIP, PostTime)
            VALUES (
                9, -- Id para "Intento de borrado" (fallido)
                'Intento de eliminar empleado no encontrado o inactivo. Documento: ' + @inValorDocumentoIdentidad,
                (SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser),
                @inIP,
                GETDATE()
            );
            SET @outResultCode = 50012; -- Código para "Empleado no encontrado"
            COMMIT TRANSACTION;
            RETURN;
        END

        -- Borrado lógico: Actualizar el estado a inactivo
        UPDATE dbo.Empleado
        SET 
            EsActivo = 0,
            PostBy = @inPostByUser,
            PostInIP = @inIP,
            PostTime = GETDATE()
        WHERE Id = @IdEmpleado;

        -- Registro en Bitácora: Éxito
        INSERT INTO dbo.BitacoraEvento (IdTipoEvento, Descripcion, IdPostByUser, PostInIP, PostTime)
        VALUES (
            10, -- Id para "Borrado exitoso"
            'Empleado eliminado lógicamente. Documento: ' + @inValorDocumentoIdentidad,
            (SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser),
            @inIP,
            GETDATE()
        );

        COMMIT TRANSACTION;
        SET @outResultCode = 0; -- Éxito

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;

        INSERT INTO dbo.DBError (UserName, Number, State, Severity, Line, ProcedureName, Message, DateTime)
        SELECT SUSER_SNAME(), ERROR_NUMBER(), ERROR_STATE(), ERROR_SEVERITY(), ERROR_LINE(), ERROR_PROCEDURE(), ERROR_MESSAGE(), GETDATE();

        SET @outResultCode = 50008; -- Error general
    END CATCH;

    SET NOCOUNT OFF;
END;
