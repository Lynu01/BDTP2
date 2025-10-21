/****** Object:  StoredProcedure [dbo].[sp_ListarEmpleados]    Script Date: 10/17/2025 10:10:50 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER   PROCEDURE [dbo].[sp_ListarEmpleados]
(
    @inFiltro NVARCHAR(100)
	,@inIP NVARCHAR(50)
    ,@inPostByUser NVARCHAR(50)
    ,@outResultCode INT OUTPUT
)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Verificar si el filtro es solo numeros (documento) o letras (nombre)
        IF (@inFiltro IS NULL OR LTRIM(RTRIM(@inFiltro)) = '')
        BEGIN
            -- Sin filtro: listar todos los empleados
            SELECT 
                e.Id
                ,e.Nombre
                ,e.ValorDocumentoIdentidad
                ,p.Nombre AS NombrePuesto
                ,e.SaldoVacaciones
            FROM dbo.Empleado e
            INNER JOIN dbo.Puesto p ON p.Id = e.IdPuesto
            WHERE e.EsActivo = 1
            ORDER BY e.Nombre ASC;
        END
        ELSE IF (@inFiltro NOT LIKE '%[^0-9]%')
        BEGIN
            -- Filtro solo numeros: buscar por documento de identidad
            SELECT 
                e.Id
                ,e.Nombre
                ,e.ValorDocumentoIdentidad
                ,p.Nombre AS NombrePuesto
                ,e.SaldoVacaciones
            FROM dbo.Empleado e
            INNER JOIN dbo.Puesto p ON p.Id = e.IdPuesto
            WHERE e.EsActivo = 1
              AND e.ValorDocumentoIdentidad LIKE '%' + @inFiltro + '%'
            ORDER BY e.Nombre ASC;

            -- Registrar consulta con filtro de cedula
            INSERT INTO dbo.BitacoraEvento (
                IdTipoEvento
                ,Descripcion
                ,IdPostByUser
                ,PostInIP
                ,PostTime
            )
            VALUES (
                12 -- Consulta con filtro de cedula
                ,'Filtro de cedula utilizado: ' + @inFiltro
                ,(SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser)
                ,@inIP
                ,GETDATE()
            );
        END
        ELSE
        BEGIN
            -- Filtro con letras o espacios: buscar por nombre
            SELECT 
                e.Id
                ,e.Nombre
                ,e.ValorDocumentoIdentidad
                ,p.Nombre AS NombrePuesto
                ,e.SaldoVacaciones
            FROM dbo.Empleado e
            INNER JOIN dbo.Puesto p ON p.Id = e.IdPuesto
            WHERE e.EsActivo = 1
              AND e.Nombre LIKE '%' + @inFiltro + '%'
            ORDER BY e.Nombre ASC;

            -- Registrar consulta con filtro de nombre
            INSERT INTO dbo.BitacoraEvento (
                IdTipoEvento
                ,Descripcion
                ,IdPostByUser
                ,PostInIP
                ,PostTime
            )
            VALUES (
                11 -- Consulta con filtro de nombre
                ,'Filtro de nombre utilizado: ' + @inFiltro
                ,(SELECT Id FROM dbo.Usuario WHERE Username = @inPostByUser)
                ,@inIP
                ,GETDATE()
            );
        END

        COMMIT TRANSACTION;

        SET @outResultCode = 0;

    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;

        -- Manejo de errores
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
    END CATCH

    SET NOCOUNT OFF;
END
