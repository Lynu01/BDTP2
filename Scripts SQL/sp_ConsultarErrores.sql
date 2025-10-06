CREATE OR ALTER PROCEDURE dbo.sp_ConsultarErrores
(
    @inCodigo INT,
    @inPostByUser NVARCHAR(50),
    @inIP NVARCHAR(50),
    @outResultCode INT OUTPUT
)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Consulta de errores por código
        SELECT 
             Codigo,
             Descripcion
        FROM dbo.Error
        WHERE Codigo = @inCodigo;

        -- Registrar en Bitácora
        INSERT INTO dbo.BitacoraEvento (
             IdTipoEvento,
             Descripcion,
             IdPostByUser,
             PostInIP,
             PostTime
        )
        VALUES (
             11, -- Usamos el evento 11: "Consulta con filtro de nombre" (lo adaptamos para consulta de errores)
             'Consulta de error. Código: ' + CAST(@inCodigo AS NVARCHAR(10)),
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
