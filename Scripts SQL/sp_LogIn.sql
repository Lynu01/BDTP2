CREATE OR ALTER PROCEDURE dbo.sp_Login
(
    @inUsername NVARCHAR(50)
    ,@inPassword NVARCHAR(255)
	,@inIP NVARCHAR(50)
    ,@outResultCode INT OUTPUT
)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY

        DECLARE @idUsuario INT;

        -- Buscar el usuario
        SELECT @idUsuario = u.Id
        FROM dbo.Usuario u
        WHERE u.Username = @inUsername;

        -- Validar si el username existe
        IF @idUsuario IS NULL
        BEGIN
            SET @outResultCode = 50001; -- Username no existe

            -- Insertar en bitácora el intento fallido
            INSERT INTO dbo.BitacoraEvento (
                 IdTipoEvento
                ,Descripcion
                ,IdPostByUser
                ,PostInIP
                ,PostTime
            )
            VALUES (
                 2 -- Tipo de evento: Login no exitoso
                ,'Intento de login fallido - Username no existe: ' + @inUsername
                ,(SELECT Id FROM dbo.Usuario WHERE Username = 'NoConocido')
                ,@inIP
                ,GETDATE()
            );

            RETURN;
        END

        -- Validar password
        IF NOT EXISTS (
            SELECT 1
            FROM dbo.Usuario u
            WHERE u.Id = @idUsuario
              AND u.Password = @inPassword
        )
        BEGIN
            SET @outResultCode = 50002; -- Password incorrecta

            -- Insertar en bitácora el intento fallido
            INSERT INTO dbo.BitacoraEvento (
                 IdTipoEvento
                ,Descripcion
                ,IdPostByUser
                ,PostInIP
                ,PostTime
            )
            VALUES (
                 2 -- Tipo de evento: Login no exitoso
                ,'Intento de login fallido - Password incorrecta para usuario: ' + @inUsername
                ,@idUsuario
                ,@inIP
                ,GETDATE()
            );

            RETURN;
        END

        -- Login exitoso
        SET @outResultCode = 0;

        -- Insertar en bitácora el login exitoso
        INSERT INTO dbo.BitacoraEvento (
             IdTipoEvento
            ,Descripcion
            ,IdPostByUser
            ,PostInIP
            ,PostTime
        )
        VALUES (
             1 -- Tipo de evento: Login exitoso
            ,'Login exitoso del usuario: ' + @inUsername
            ,@idUsuario
            ,@inIP
            ,GETDATE()
        );

    END TRY
    BEGIN CATCH

        -- Manejo de error general
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

        SET @outResultCode = 50008; -- Error general de BD

    END CATCH;

    SET NOCOUNT OFF;
END;
GO
