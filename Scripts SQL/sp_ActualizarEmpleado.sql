CREATE OR ALTER PROCEDURE dbo.sp_ActualizarEmpleado
(
    @inValorDocumentoIdentidad NVARCHAR(30),
    @inNuevoValorDocumentoIdentidad NVARCHAR(30),
    @inNuevoNombre NVARCHAR(100),
    @inNuevoIdPuesto INT,
    @inPostByUser NVARCHAR(50),
    @inIP NVARCHAR(50),
    @outResultCode INT OUTPUT
)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Normalizar entradas
        SET @inValorDocumentoIdentidad = LTRIM(RTRIM(@inValorDocumentoIdentidad));
        SET @inNuevoValorDocumentoIdentidad = LTRIM(RTRIM(@inNuevoValorDocumentoIdentidad));
        SET @inNuevoNombre = LTRIM(RTRIM(@inNuevoNombre));

        DECLARE @IdEmpleado INT;

        -- Buscar el IdEmpleado basado en el documento original
        SELECT @IdEmpleado = Id
        FROM dbo.Empleado
        WHERE ValorDocumentoIdentidad = @inValorDocumentoIdentidad
          AND EsActivo = 1;

        IF @IdEmpleado IS NULL
        BEGIN
            -- Registrar en bitácora que no se encontró el empleado
            INSERT INTO dbo.BitacoraEvento (
                IdTipoEvento,
                Descripcion,
                IdPostByUser,
                PostInIP,
                PostTime
            )
            VALUES (
                7, -- Update no exitoso
                'No se encontró el empleado con Documento: ' + @inValorDocumentoIdentidad,
                (SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser),
                @inIP,
                GETDATE()
            );

            SET @outResultCode = 50012; -- Empleado no encontrado
            COMMIT TRANSACTION;
            RETURN;
        END

        -- Validar duplicado de Documento en otro empleado
        IF EXISTS (
            SELECT 1
            FROM dbo.Empleado
            WHERE ValorDocumentoIdentidad = @inNuevoValorDocumentoIdentidad
              AND Id <> @IdEmpleado
              AND EsActivo = 1
        )
        BEGIN
            -- Registrar duplicado en bitácora
            INSERT INTO dbo.BitacoraEvento (
                IdTipoEvento,
                Descripcion,
                IdPostByUser,
                PostInIP,
                PostTime
            )
            VALUES (
                7, -- Update no exitoso
                'Documento duplicado al actualizar. Documento: ' + @inNuevoValorDocumentoIdentidad,
                (SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser),
                @inIP,
                GETDATE()
            );

            SET @outResultCode = 50006; -- Documento duplicado
            COMMIT TRANSACTION;
            RETURN;
        END

        -- Validar duplicado de Nombre en otro empleado
        IF EXISTS (
            SELECT 1
            FROM dbo.Empleado
            WHERE Nombre = @inNuevoNombre
              AND Id <> @IdEmpleado
              AND EsActivo = 1
        )
        BEGIN
            -- Registrar duplicado en bitácora
            INSERT INTO dbo.BitacoraEvento (
                IdTipoEvento,
                Descripcion,
                IdPostByUser,
                PostInIP,
                PostTime
            )
            VALUES (
                7, -- Update no exitoso
                'Nombre duplicado al actualizar. Nombre: ' + @inNuevoNombre,
                (SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser),
                @inIP,
                GETDATE()
            );

            SET @outResultCode = 50007; -- Nombre duplicado
            COMMIT TRANSACTION;
            RETURN;
        END

        -- Actualizar los datos
        UPDATE dbo.Empleado
        SET 
            ValorDocumentoIdentidad = @inNuevoValorDocumentoIdentidad,
            Nombre = @inNuevoNombre,
            IdPuesto = @inNuevoIdPuesto,
            PostBy = @inPostByUser,
            PostInIP = @inIP,
            PostTime = GETDATE()
        WHERE Id = @IdEmpleado;

        -- Registrar éxito en bitácora
        INSERT INTO dbo.BitacoraEvento (
            IdTipoEvento,
            Descripcion,
            IdPostByUser,
            PostInIP,
            PostTime
        )
        VALUES (
            8, -- Update exitoso
            'Empleado actualizado. Documento anterior: ' + @inValorDocumentoIdentidad,
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
