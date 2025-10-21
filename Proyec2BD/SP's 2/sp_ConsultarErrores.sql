/****** Object:  StoredProcedure [dbo].[sp_ConsultarErrores]    Script Date: 10/17/2025 10:09:30 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER   PROCEDURE [dbo].[sp_ConsultarErrores]
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
             e.Codigo,
             e.Descripcion
        FROM dbo.Error e
        WHERE e.Codigo = @inCodigo;

        -- Registrar en Bitácora
        INSERT INTO dbo.BitacoraEvento (
             IdTipoEvento,
             Descripcion,
             IdPostByUser,
             PostInIP,
             PostTime
        )
        VALUES (
             11, -- Usamos el evento 11: "Consulta con filtro de nombre"
             'Consulta de error. Codigo: ' + CAST(@inCodigo AS NVARCHAR(10)),
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
