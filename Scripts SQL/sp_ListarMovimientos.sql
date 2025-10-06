CREATE OR ALTER PROCEDURE dbo.sp_ListarMovimientos
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
        -- Normalizar entrada
        SET @inValorDocumentoIdentidad = LTRIM(RTRIM(@inValorDocumentoIdentidad));

        BEGIN TRANSACTION;

        -- Validar existencia del empleado
        IF NOT EXISTS (
            SELECT 1
            FROM dbo.Empleado
            WHERE ValorDocumentoIdentidad = @inValorDocumentoIdentidad
              AND EsActivo = 1
        )
        BEGIN
            -- Registrar en bitácora consulta fallida
            INSERT INTO dbo.BitacoraEvento (
                 IdTipoEvento
                ,Descripcion
                ,IdPostByUser
                ,PostInIP
                ,PostTime
            )
            VALUES (
                 9 -- Consulta no exitosa
                ,'Intento listar movimientos de documento inexistente: ' + @inValorDocumentoIdentidad
                ,(SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser)
                ,@inIP
                ,GETDATE()
            );

            SET @outResultCode = 50012; -- Empleado no encontrado
            COMMIT TRANSACTION;
            RETURN;
        END

        -- Listar movimientos
        SELECT 
             m.Id
            ,m.Fecha
            ,tm.Nombre AS TipoMovimiento
            ,m.Monto
            ,m.NuevoSaldo
            ,u.Username AS PostByUser
            ,m.PostInIP
            ,m.PostTime
        FROM dbo.Movimiento m
        INNER JOIN dbo.Empleado e ON m.IdEmpleado = e.Id
        INNER JOIN dbo.TipoMovimiento tm ON m.IdTipoMovimiento = tm.Id
        INNER JOIN dbo.Usuario u ON m.IdPostByUser = u.Id
        WHERE e.ValorDocumentoIdentidad = @inValorDocumentoIdentidad
        ORDER BY m.Fecha ASC;

        -- Registrar en bitácora consulta exitosa
        INSERT INTO dbo.BitacoraEvento (
             IdTipoEvento
            ,Descripcion
            ,IdPostByUser
            ,PostInIP
            ,PostTime
        )
        VALUES (
             10 -- Consulta exitosa
            ,'Listados movimientos para documento: ' + @inValorDocumentoIdentidad
            ,(SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser)
            ,@inIP
            ,GETDATE()
        );

        COMMIT TRANSACTION;
        SET @outResultCode = 0;

    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;

        -- Registrar error técnico en DBError
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
