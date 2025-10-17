
-----------------------------------------*******************************************
/* ===========================
   PRUEBA MANUAL POR INTENTO
   Ejecutar UNA vez por intento.
   Cambia @user/@pwdAttempt/@ip según necesites.
   =========================== */

-- ---------- CONFIG (edita aquí antes de ejecutar) ----------
DECLARE @user NVARCHAR(50)        = N'UsuarioScripts';   -- <- Usuario objetivo (cámbialo)
DECLARE @pwdAttempt NVARCHAR(255)= N')*2LnSr^lk';       -- <- Contraseña que quieres probar (fallará)
DECLARE @ip NVARCHAR(50)         = N'127.0.0.1';        -- <- Tu IP de prueba
-- ----------------------------------------------------------------

-- 1) Chequeo de throttle para este (usuario, ip)
DECLARE @isBlocked BIT, @secsRemaining INT, @rcCheck INT;
EXEC dbo.sp_CheckLoginThrottle
     @inUsername          = @user,
     @inIP                = @ip,
     @outIsBlocked        = @isBlocked         OUTPUT,
     @outSecondsRemaining = @secsRemaining    OUTPUT,
     @outResultCode       = @rcCheck          OUTPUT;

PRINT '--- CHECK THROTTLE ---';
PRINT CONCAT('Usuario=', @user, '  IP=', @ip);
PRINT CONCAT('Blocked=', @isBlocked, '  ResultCode=', @rcCheck, '  SecsRemaining=', @secsRemaining);
SELECT @isBlocked AS IsBlocked, @secsRemaining AS SecondsRemaining, @rcCheck AS CheckResultCode;

IF (@isBlocked = 1)
BEGIN
    PRINT 'Usuario BLOQUEADO. No se intentará login.';
    PRINT CONCAT('Mostrar mensaje UI: ''Demasiados intentos, intente de nuevo dentro de ', CEILING(@secsRemaining/60.0), ' minutos.''');
END
ELSE
BEGIN
    -- 2) Intento de login UNA vez (esto se registrará en bitácora)
    DECLARE @rcLogin INT;
    EXEC dbo.sp_Login
         @inUsername     = @user,
         @inPassword     = @pwdAttempt,
         @inIP           = @ip,
         @outResultCode  = @rcLogin OUTPUT;

    PRINT '--- INTENTO DE LOGIN (UNO) ---';
    PRINT CONCAT('Usuario=', @user, '  PasswordIntento=', @pwdAttempt, '  ResultCode=', @rcLogin);
    SELECT @rcLogin AS LoginResultCode;

    IF (@rcLogin = 0)
        PRINT 'Login exitoso (esto deberia ocurrir solo si la contraseña es correcta).';
    ELSE IF (@rcLogin = 50002)
        PRINT 'Password incorrecta (falla registrada en BitacoraEvento).';
    ELSE IF (@rcLogin = 50001)
        PRINT 'Username no existe.';
    ELSE IF (@rcLogin = 50021)
        PRINT 'Bloqueado por throttle (defensa adicional en sp_Login).';
    ELSE IF (@rcLogin = 50008)
        PRINT 'Error general (revisar DBError).';
END

-- 3) Mostrar últimas entradas de bitácora relacionadas al (usuario, ip) para ver el efecto del intento
PRINT '--- BITACORA RECIENTE PARA ESTE USUARIO/IP ---';
SELECT TOP (20)
       b.Id,
       b.IdTipoEvento,
       b.Descripcion,
       u.Username AS PostByUser,
       b.PostInIP,
       b.PostTime
FROM dbo.BitacoraEvento AS b
LEFT JOIN dbo.Usuario AS u ON u.Id = b.IdPostByUser
WHERE (u.Username = @user) OR (b.PostInIP = @ip AND b.IdPostByUser = (SELECT Id FROM dbo.Usuario WHERE Username = @user))
ORDER BY b.PostTime DESC, b.Id DESC;

-- 4) Estado rápido: cuántas fallas en los últimos 5 minutos (útil para ver progreso hacia el bloqueo)
PRINT '--- FALLAS EN VENTANA 5min (este usuario/ip) ---';
DECLARE @failCount INT;
SELECT @failCount = COUNT(1)
FROM dbo.BitacoraEvento AS b
WHERE (b.IdPostByUser = (SELECT Id FROM dbo.Usuario WHERE Username = @user))
  AND (b.PostInIP = @ip)
  AND (b.IdTipoEvento = 2) -- login no exitoso
  AND (b.PostTime >= DATEADD(MINUTE, -5, GETDATE()))
  AND (b.Descripcion NOT LIKE N'THROTTLE:%');

SELECT @failCount AS Failures_Last5Min;

-- 5) (Opcional) Mostrar si otro usuario desde la misma IP está bloqueado o no
PRINT '--- CHECK RÁPIDO OTRO USUARIO (edita si quieres) ---';
DECLARE @otherUser NVARCHAR(50) = N'UsuarioScripts'; -- cambia si querés
DECLARE @othBlocked BIT, @othSecs INT, @othRc INT;
EXEC dbo.sp_CheckLoginThrottle
     @inUsername = @otherUser,
     @inIP       = @ip,
     @outIsBlocked = @othBlocked OUTPUT,
     @outSecondsRemaining = @othSecs OUTPUT,
     @outResultCode = @othRc OUTPUT;
SELECT @otherUser AS OtherUser, @othBlocked AS OtherBlocked, @othSecs AS OtherSecsRemaining, @othRc AS OtherResultCode;
