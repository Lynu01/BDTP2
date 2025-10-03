CREATE OR ALTER PROCEDURE dbo.sp_Logout
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
        SELECT @userId = Id
        FROM dbo.Usuario
        WHERE Username = @inUsername;

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
            ,'Logout Exitoso'                             -- No hay descripción extra para Logout
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
GO
