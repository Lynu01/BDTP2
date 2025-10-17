/****** Object:  StoredProcedure [dbo].[sp_BloqueoUsuario]    Script Date: 10/17/2025 10:08:55 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER   PROCEDURE [dbo].[sp_BloqueoUsuario]
(
    @inUsername NVARCHAR(50),
    @inIP NVARCHAR(50),
    @outIntentosFallidos INT OUTPUT
)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @idUsuario INT;
    SELECT @idUsuario = Id FROM dbo.Usuario WHERE Username = @inUsername;

    -- Si el usuario no existe, no puede estar bloqueado.
    IF @idUsuario IS NULL
    BEGIN
        SET @outIntentosFallidos = 0;
        RETURN;
    END

    -- Contar los logins no exitosos (IdTipoEvento = 2) en los últimos 5 minutos
    SELECT @outIntentosFallidos = COUNT(*)
    FROM dbo.BitacoraEvento
    WHERE IdPostByUser = @idUsuario
      AND PostInIP = @inIP
      AND IdTipoEvento = 2 -- 2 es el ID para "Login No Exitoso"
      AND PostTime >= DATEADD(MINUTE, -5, GETDATE());

    SET NOCOUNT OFF;
END;
