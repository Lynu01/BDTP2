SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE OR ALTER PROCEDURE [dbo].[sp_CheckThrottle]
(
    @inUsername NVARCHAR(50),
    @inIP NVARCHAR(50),
    @outIsBlocked BIT OUTPUT,
    @outSecondsRemaining INT OUTPUT,
    @outResultCode INT OUTPUT
)
AS
BEGIN
    SET NOCOUNT ON;

    -- Inicializar valores de salida
    SET @outIsBlocked = 0;
    SET @outSecondsRemaining = 0;
    SET @outResultCode = 0;

    DECLARE @idUsuario INT;
    SELECT @idUsuario = Id FROM dbo.Usuario WHERE Username = @inUsername;

    -- Si el usuario no existe, no puede estar bloqueado.
    IF @idUsuario IS NULL
    BEGIN
        SET @outResultCode = 50001; -- Mismo código que sp_Login para "Username no existe"
        RETURN;
    END

    -- Contar los logins no exitosos (IdTipoEvento = 2) en los últimos 5 minutos
    -- para la combinación específica de usuario e IP.
    DECLARE @intentosFallidos INT;
    SELECT @intentosFallidos = COUNT(*)
    FROM dbo.BitacoraEvento
    WHERE IdPostByUser = @idUsuario
      AND PostInIP = @inIP
      AND IdTipoEvento = 2 -- 2 es el ID para "Login No Exitoso"
      AND PostTime >= DATEADD(MINUTE, -5, GETDATE());

    -- Según el PDF, el bloqueo ocurre con MÁS de 5 intentos.
    IF @intentosFallidos > 5
    BEGIN
        SET @outIsBlocked = 1;

        -- Calcular el tiempo restante para el desbloqueo.
        -- El bloqueo dura 5 minutos DESDE EL ÚLTIMO INTENTO FALLIDO.
        DECLARE @ultimoIntento DATETIME;
        SELECT TOP 1 @ultimoIntento = PostTime
        FROM dbo.BitacoraEvento
        WHERE IdPostByUser = @idUsuario
          AND PostInIP = @inIP
          AND IdTipoEvento = 2
        ORDER BY PostTime DESC;

        -- Si por alguna razón no se encuentra el último intento, se pone un valor por defecto.
        IF @ultimoIntento IS NULL
        BEGIN
            SET @outSecondsRemaining = 300; -- 5 minutos
        END
        ELSE
        BEGIN
            -- El tiempo de desbloqueo es 5 minutos después del último intento.
            DECLARE @tiempoDesbloqueo DATETIME = DATEADD(MINUTE, 5, @ultimoIntento);
            -- Calculamos cuántos segundos faltan desde ahora hasta el tiempo de desbloqueo.
            SET @outSecondsRemaining = DATEDIFF(SECOND, GETDATE(), @tiempoDesbloqueo);
            
            -- Si el resultado es negativo, significa que el tiempo ya pasó, así que lo dejamos en 0.
            IF @outSecondsRemaining < 0
            BEGIN
                SET @outSecondsRemaining = 0;
                SET @outIsBlocked = 0; -- El bloqueo ya expiró.
            END
        END
    END

    SET NOCOUNT OFF;
END
GO