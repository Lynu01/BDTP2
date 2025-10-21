/****** Object:  StoredProcedure [dbo].[sp_Logout]    Script Date: 10/17/2025 10:12:18 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER   PROCEDURE [dbo].[sp_Logout]
(
    @inUsername NVARCHAR(50)
    ,@inIP NVARCHAR(50)
    ,@outResultCode INT OUTPUT
)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @userId INT;

        -- Obtener el Id del usuario
        SELECT @userId = u.Id
        FROM dbo.Usuario u
        WHERE u.Username = @inUsername;

        -- Insertar en la Bitácora
        INSERT INTO dbo.BitacoraEvento (
             IdTipoEvento
            ,Descripcion
            ,IdPostByUser
            ,PostInIP
            ,PostTime
        )
        VALUES (
             4                                -- Logout
            ,'Logout Exitoso'                             -- No hay descripci�n extra para Logout
            ,@userId
            ,@inIP
            ,GETDATE()
        );

        COMMIT TRANSACTION;

        SET @outResultCode = 0;

    END TRY
    BEGIN CATCH

        ROLLBACK TRANSACTION;

        -- Registrar el error
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
