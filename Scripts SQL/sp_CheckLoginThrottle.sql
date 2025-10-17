USE [prueba];
GO
SET ANSI_NULLS ON;
GO
SET QUOTED_IDENTIFIER ON;
GO

-- Chequea si un usuario está bloqueado por demasiados intentos fallidos
-- Reglas:
--   - Umbral: 5 fallos en 5 minutos (IdTipoEvento = 2, excluye THROTTLE)
--   - Cooldown: 10 minutos por (usuario, ip)
-- Salidas:
--   - @outIsBlocked = 1 si bloqueado, 0 si no
--   - @outSecondsRemaining = segundos que faltan del cooldown (si bloqueado)
--   - @outResultCode = 50021 si bloqueado, 0 si no
CREATE OR ALTER PROCEDURE dbo.sp_CheckLoginThrottle
(
    @inUsername           NVARCHAR(50)
  , @inIP                 NVARCHAR(50)
  , @outIsBlocked         BIT           OUTPUT
  , @outSecondsRemaining  INT           OUTPUT
  , @outResultCode        INT           OUTPUT
)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        -- Normalizar
        SET @inUsername = LTRIM(RTRIM(@inUsername));
        SET @inIP       = LTRIM(RTRIM(@inIP));

        DECLARE
            @now           DATETIME = GETDATE()
          , @winStart      DATETIME = DATEADD(MINUTE, -5,  GETDATE())
          , @cooldownLast  DATETIME
          , @idUsuario     INT
          , @failCount     INT;

        -- Defaults
        SET @outIsBlocked        = 0;
        SET @outSecondsRemaining = 0;
        SET @outResultCode       = 0;

        -- Resolver usuario
        SELECT
            @idUsuario = u.Id
        FROM dbo.Usuario AS u
        WHERE (u.Username = @inUsername);

        -- Si el usuario no existe, acá NO bloqueamos (lo maneja sp_Login con 50001)
        IF (@idUsuario IS NULL)
        BEGIN
            RETURN;
        END

        -- ¿Sigue en cooldown activo por un THROTTLE previo? (ventana 10 min)
        SELECT
            @cooldownLast = MAX(b.PostTime)
        FROM dbo.BitacoraEvento AS b
        WHERE (b.IdPostByUser = @idUsuario)
          AND (b.PostInIP     = @inIP)
          AND (b.IdTipoEvento = 2)                 -- login no exitoso
          AND (b.Descripcion  LIKE N'THROTTLE:%'); -- marca de bloqueo

        IF (@cooldownLast IS NOT NULL AND DATEADD(MINUTE, 10, @cooldownLast) > @now)
        BEGIN
            SET @outIsBlocked        = 1;
            SET @outSecondsRemaining = DATEDIFF(SECOND, @now, DATEADD(MINUTE, 10, @cooldownLast));
            SET @outResultCode       = 50021;
            RETURN;
        END

        -- ¿Alcanza umbral de fallos en 5 minutos? (excluye eventos THROTTLE)
        SELECT
            @failCount = COUNT(1)
        FROM dbo.BitacoraEvento AS b
        WHERE (b.IdPostByUser = @idUsuario)
          AND (b.PostInIP     = @inIP)
          AND (b.IdTipoEvento = 2)                  -- login no exitoso
          AND (b.PostTime     >= @winStart)
          AND (b.Descripcion  NOT LIKE N'THROTTLE:%');

        IF (@failCount >= 5)
        BEGIN
            -- Disparar cooldown: registrar THROTTLE y bloquear por 10 min
            INSERT INTO dbo.BitacoraEvento
            (
                IdTipoEvento
              , Descripcion
              , IdPostByUser
              , PostInIP
              , PostTime
            )
            VALUES
            (   2
              , N'THROTTLE: demasiados intentos fallidos en 5 min (usuario=' + @inUsername + N', ip=' + @inIP + N')'
              , @idUsuario
              , @inIP
              , @now
            );

            SET @outIsBlocked        = 1;
            SET @outSecondsRemaining = 600;  -- 10 min
            SET @outResultCode       = 50021;
            RETURN;
        END

        -- No bloqueado
        RETURN;
    END TRY
    BEGIN CATCH
        -- En error no bloqueamos, pero registramos
        INSERT INTO dbo.DBError
        (
            UserName, Number, State, Severity, Line, ProcedureName, Message, DateTime
        )
        SELECT
            SUSER_SNAME(), ERROR_NUMBER(), ERROR_STATE(), ERROR_SEVERITY(),
            ERROR_LINE(), ERROR_PROCEDURE(), ERROR_MESSAGE(), GETDATE();

        -- No tocar flags de bloqueo; la UI puede intentar de nuevo
        SET @outIsBlocked        = 0;
        SET @outSecondsRemaining = 0;
        SET @outResultCode       = 50008; -- error general
    END CATCH;
END;
GO
