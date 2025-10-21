CREATE OR ALTER PROCEDURE dbo.sp_Bitacora_Add
  @inIdTipoEvento INT,         -- 1..14 seg�n tu tabla
  @inDescripcion  NVARCHAR(MAX),
  @inUserName     NVARCHAR(50),-- se resuelve a Usuario.Id
  @inIP           NVARCHAR(50),
  @outResultCode  INT OUTPUT
AS
BEGIN
  SET NOCOUNT ON;
  BEGIN TRY
    DECLARE @idUsuario INT;
    SELECT @idUsuario = u.Id 
	FROM dbo.Usuario u 
	WHERE u.Username = @inUserName;

    IF @idUsuario IS NULL BEGIN SET @outResultCode = 50001; RETURN; END

    INSERT INTO dbo.BitacoraEvento (IdTipoEvento
	, Descripcion
	, IdPostByUser
	, PostInIP
	, PostTime)
    VALUES (@inIdTipoEvento
	, @inDescripcion
	, @idUsuario
	, @inIP
	, GETDATE());

    SET @outResultCode = 0;
  END TRY
  BEGIN CATCH
    INSERT INTO dbo.DBError(UserName
	, Number
	, State
	, Severity
	, Line
	, ProcedureName
	, Message
	, DateTime)
    VALUES(@inUserName
	, ERROR_NUMBER()
	, ERROR_STATE()
	, ERROR_SEVERITY()
	, ERROR_LINE()
	, ERROR_PROCEDURE()
	, ERROR_MESSAGE()
	, GETDATE());
    SET @outResultCode = 50008;
  END CATCH
END
GO
