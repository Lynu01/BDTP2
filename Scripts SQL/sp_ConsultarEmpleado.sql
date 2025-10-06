CREATE OR ALTER PROCEDURE dbo.sp_ConsultarEmpleado
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
            -- Registrar fallo en bitácora
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

        -- Registrar éxito en bitácora
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
GO
