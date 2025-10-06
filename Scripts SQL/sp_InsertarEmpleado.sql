CREATE OR ALTER PROCEDURE dbo.sp_InsertarEmpleado
(
    @inValorDocumentoIdentidad NVARCHAR(30)
	,@inIP NVARCHAR(50)
    ,@inNombre NVARCHAR(100)
    ,@inIdPuesto INT
    ,@inPostByUser NVARCHAR(50)
    ,@outResultCode INT OUTPUT
)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Normalizar entradas
        SET @inValorDocumentoIdentidad = LTRIM(RTRIM(@inValorDocumentoIdentidad));
        SET @inNombre = LTRIM(RTRIM(@inNombre));

        -- Validar si existe por ValorDocumentoIdentidad
        IF EXISTS (
            SELECT 1
            FROM dbo.Empleado
            WHERE ValorDocumentoIdentidad = @inValorDocumentoIdentidad
              AND EsActivo = 1
        )
        BEGIN
            -- Registro en bitácora
            INSERT INTO dbo.BitacoraEvento (
                 IdTipoEvento
                ,Descripcion
                ,IdPostByUser
                ,PostInIP
                ,PostTime
            )
            VALUES (
                 5 -- Inserción no exitosa
                ,'Documento identidad ya existe: ' + @inValorDocumentoIdentidad
                ,(SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser)
                ,@inIP
                ,GETDATE()
            );

            SET @outResultCode = 50004; -- Código: ValorDocumentoIdentidad ya existe
			COMMIT TRANSACTION;
            RETURN;
        END

        -- Validar si existe por Nombre
        IF EXISTS (
            SELECT 1
            FROM dbo.Empleado
            WHERE Nombre = @inNombre
              AND EsActivo = 1
        )
        BEGIN
            -- Registro en bitácora
            INSERT INTO dbo.BitacoraEvento (
                 IdTipoEvento
                ,Descripcion
                ,IdPostByUser
                ,PostInIP
                ,PostTime
            )
            VALUES (
                 5 -- Inserción no exitosa
                ,'Nombre empleado ya existe: ' + @inNombre
                ,(SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser)
                ,@inIP
                ,GETDATE()
            );

            SET @outResultCode = 50005; -- Código: Nombre ya existe
			COMMIT TRANSACTION;
            RETURN;
        END

        -- Insertar el empleado
        INSERT INTO dbo.Empleado (
             IdPuesto
            ,ValorDocumentoIdentidad
            ,Nombre
            ,FechaContratacion
            ,SaldoVacaciones
            ,EsActivo
            ,PostInIP
            ,PostBy
            ,PostTime
        )
        VALUES (
             @inIdPuesto
            ,@inValorDocumentoIdentidad
            ,@inNombre
            ,GETDATE() -- Se asume contratado hoy
            ,0 -- Saldo vacaciones inicia en 0
            ,1 -- Activo
            ,@inIP
            ,@inPostByUser
            ,GETDATE()
        );

        -- Registro en bitácora éxito
        INSERT INTO dbo.BitacoraEvento (
             IdTipoEvento
            ,Descripcion
            ,IdPostByUser
            ,PostInIP
            ,PostTime
        )
        VALUES (
             6 -- Inserción exitosa
            ,'Insertado: ' + @inValorDocumentoIdentidad + ', ' + @inNombre
            ,(SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser)
            ,@inIP
            ,GETDATE()
        );

        COMMIT TRANSACTION;
        SET @outResultCode = 0;

    END TRY
    BEGIN CATCH

        ROLLBACK TRANSACTION;

        -- Registro error en DBError
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
