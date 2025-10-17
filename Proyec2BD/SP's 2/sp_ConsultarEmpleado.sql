/****** Object:  StoredProcedure [dbo].[sp_ConsultarEmpleado]    Script Date: 10/17/2025 10:09:16 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER   PROCEDURE [dbo].[sp_ConsultarEmpleado]
(
    @inValorDocumentoIdentidad NVARCHAR(30),
    @inIP NVARCHAR(50),
    @inPostByUser NVARCHAR(50),
    @outResultCode INT OUTPUT
)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Normalizar entradas
        SET @inValorDocumentoIdentidad = LTRIM(RTRIM(@inValorDocumentoIdentidad));

        -- Validar existencia
        IF NOT EXISTS (
            SELECT 1
            FROM dbo.Empleado
            WHERE ValorDocumentoIdentidad = @inValorDocumentoIdentidad
              AND EsActivo = 1
        )
        BEGIN
            -- Registrar fallo en bit�cora
            INSERT INTO dbo.BitacoraEvento (
                 IdTipoEvento
                ,Descripcion
                ,IdPostByUser
                ,PostInIP
                ,PostTime
            )
            VALUES (
                 9 -- Consulta no exitosa
                ,'Consulta fallida. Documento no encontrado: ' + @inValorDocumentoIdentidad
                ,(SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser)
                ,@inIP
                ,GETDATE()
            );

            SET @outResultCode = 50012; -- No encontrado
            COMMIT TRANSACTION;
            RETURN;
        END

        -- Hacer consulta
        SELECT 
             e.ValorDocumentoIdentidad
            ,e.Nombre
            ,p.Nombre AS NombrePuesto
            ,e.SaldoVacaciones
        FROM dbo.Empleado e
        INNER JOIN dbo.Puesto p ON e.IdPuesto = p.Id
        WHERE e.ValorDocumentoIdentidad = @inValorDocumentoIdentidad
          AND e.EsActivo = 1;

        -- Registrar �xito en bit�cora
        INSERT INTO dbo.BitacoraEvento (
             IdTipoEvento
            ,Descripcion
            ,IdPostByUser
            ,PostInIP
            ,PostTime
        )
        VALUES (
             10 -- Consulta exitosa
            ,'Consulta exitosa de empleado: ' + @inValorDocumentoIdentidad
            ,(SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser)
            ,@inIP
            ,GETDATE()
        );

        COMMIT TRANSACTION;
        SET @outResultCode = 0;

    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;

        -- Registrar en DBError
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

        SET @outResultCode = 50008; -- Error general
    END CATCH;

    SET NOCOUNT OFF;
END;
