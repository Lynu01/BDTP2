CREATE OR ALTER PROCEDURE dbo.sp_TrazarIntentoBorrado
(
    @inValorDocumentoIdentidad NVARCHAR(30),
    @inPostByUser              NVARCHAR(50),
    @inIP                      NVARCHAR(50),
    @outResultCode             INT OUTPUT
)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        SET @inValorDocumentoIdentidad = LTRIM(RTRIM(@inValorDocumentoIdentidad));

        DECLARE @idUsuario INT;
        SELECT @idUsuario = u.Id 
        FROM dbo.Usuario u 
        WHERE u.Username = @inPostByUser;

        IF @idUsuario IS NULL
        BEGIN
            SET @outResultCode = 50001; -- username no existe
            DECLARE @rc INT, @msg NVARCHAR(MAX);
            SET @msg = N'Intento de borrado. Error=Username no existe. Doc='
                     + ISNULL(@inValorDocumentoIdentidad,N'');
            EXEC dbo.sp_Bitacora_Add 9, @msg, @inPostByUser, @inIP, @rc OUTPUT;
            RETURN;
        END

        DECLARE @doc NVARCHAR(30), @nom NVARCHAR(100), @puesto NVARCHAR(100), @saldo DECIMAL(10,2);

        SELECT TOP(1)
            @doc    = e.ValorDocumentoIdentidad,
            @nom    = e.Nombre,
            @saldo  = e.SaldoVacaciones,
            @puesto = p.Nombre
        FROM dbo.Empleado e
        JOIN dbo.Puesto  p ON p.Id = e.IdPuesto
        WHERE e.ValorDocumentoIdentidad = @inValorDocumentoIdentidad
          AND e.EsActivo = 1;

        DECLARE @desc NVARCHAR(MAX);
        IF @doc IS NULL
        BEGIN
            SET @desc = N'Intento de borrado. Empleado no encontrado o inactivo. Doc='
                      + ISNULL(@inValorDocumentoIdentidad,N'');
        END
        ELSE
        BEGIN
            SET @desc = N'Intento de borrado. Doc=' + @doc
                      + N'; Nombre=' + ISNULL(@nom,N'')
                      + N'; Puesto=' + ISNULL(@puesto,N'')
                      + N'; SaldoVacaciones=' + CONVERT(NVARCHAR(50), @saldo);
        END

        DECLARE @rc2 INT;
        EXEC dbo.sp_Bitacora_Add 9, @desc, @inPostByUser, @inIP, @rc2 OUTPUT;

        SET @outResultCode = 0;
    END TRY
    BEGIN CATCH
        INSERT INTO dbo.DBError(UserName, Number, State, Severity, Line, ProcedureName, Message, DateTime)
        VALUES(@inPostByUser, ERROR_NUMBER(), ERROR_STATE(), ERROR_SEVERITY(), ERROR_LINE(), ERROR_PROCEDURE(), ERROR_MESSAGE(), GETDATE());
        SET @outResultCode = 50008;
    END CATCH
END
GO
