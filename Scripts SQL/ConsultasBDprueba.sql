/* =======================
   RESET + REPLAY CON TUS SPs
   ======================= */

-- 1) Cargar XML a variable (ruta LITERAL)
DECLARE @XmlData XML;

SELECT @XmlData = TRY_CAST(BulkColumn AS XML)
FROM OPENROWSET(
       BULK N'C:\Users\kevin\OneDrive - Estudiantes ITCR\Documentos\GitHub\BD_TP_2\BDTP2\Scripts SQL\archivoDatos.xml',  -- CAMBIAR RUTA DEL xml
       SINGLE_BLOB
     ) AS src;

IF @XmlData IS NULL
BEGIN
  RAISERROR('No se pudo leer el XML desde la ruta especificada.', 16, 1);
  RETURN;
END;

-- 2) Reset limpio con tu loader (carga catálogos, usuarios y empleados)
DECLARE @rc INT;
EXEC dbo.sp_CargarDatosDesdeXML
     @inXmlData     = @XmlData,
     @outResultCode = @rc OUTPUT;
SELECT Resultado_CargarDatosDesdeXML = @rc;  -- 0 = OK

-- 3) Borrar movimientos importados “a pelo” y poner saldos a 0
DELETE FROM dbo.Movimiento;

UPDATE dbo.Empleado
   SET SaldoVacaciones = 0,
       PostBy          = N'XML-Replay',
       PostInIP        = N'127.0.0.1',
       PostTime        = GETDATE();

-- 4) Reproducir SOLO los <movimiento> del XML, en el MISMO orden del archivo
IF OBJECT_ID('tempdb..#MovsReplay') IS NOT NULL DROP TABLE #MovsReplay;
;WITH X AS (
  SELECT
      seq               = ROW_NUMBER() OVER (ORDER BY (SELECT NULL)),
      ValorDocId        = T.X.value('@ValorDocId',       'NVARCHAR(30)'),
      IdTipoMovimiento  = T.X.value('@IdTipoMovimiento', 'INT'),
      Monto             = T.X.value('@Monto',            'DECIMAL(10,2)'),
      PostByUser        = T.X.value('@PostByUser',       'NVARCHAR(50)'),
      PostInIP          = T.X.value('@PostInIP',         'NVARCHAR(50)'),
      FechaXML          = T.X.value('@Fecha',            'DATE'),       -- informativo
      PostTimeXML       = T.X.value('@PostTime',         'DATETIME')    -- informativo
  FROM @XmlData.nodes('/Datos/Movimientos/movimiento') AS T(X)
)
SELECT *
INTO #MovsReplay
FROM X
ORDER BY seq;

IF OBJECT_ID('tempdb..#ResultadosReplay') IS NOT NULL DROP TABLE #ResultadosReplay;
CREATE TABLE #ResultadosReplay
(
  seq               INT           NOT NULL PRIMARY KEY,
  ValorDocId        NVARCHAR(30)  NOT NULL,
  IdTipoMovimiento  INT           NOT NULL,
  Monto             DECIMAL(10,2) NOT NULL,
  FechaXML          DATE          NULL,
  PostTimeXML       DATETIME      NULL,
  ResultCode        INT           NULL
);

DECLARE @seq INT = 1, @seqMax INT;
SELECT @seqMax = MAX(seq) FROM #MovsReplay;

WHILE @seq <= @seqMax
BEGIN
  DECLARE
      @doc   NVARCHAR(30),
      @tipo  INT,
      @monto DECIMAL(10,2),
      @user  NVARCHAR(50),
      @ip    NVARCHAR(50),
      @fx    DATE,
      @ptx   DATETIME,
      @rcOut INT;

  SELECT
      @doc   = ValorDocId,
      @tipo  = IdTipoMovimiento,
      @monto = Monto,
      @user  = PostByUser,
      @ip    = PostInIP,
      @fx    = FechaXML,
      @ptx   = PostTimeXML
  FROM #MovsReplay
  WHERE seq = @seq;

  EXEC dbo.sp_InsertarMovimiento
       @inValorDocumentoIdentidad = @doc,
       @inIdTipoMovimiento        = @tipo,
       @inMonto                   = @monto,
       @inPostByUser              = @user,
       @inIP                      = @ip,
       @outResultCode             = @rcOut OUTPUT;

  INSERT INTO #ResultadosReplay (seq, ValorDocId, IdTipoMovimiento, Monto, FechaXML, PostTimeXML, ResultCode)
  VALUES (@seq, @doc, @tipo, @monto, @fx, @ptx, @rcOut);

  SET @seq += 1;
END

-- 5) Reportes: qué pasó en la corrida
SELECT ResultCode, COUNT(*) AS Cantidad
FROM #ResultadosReplay
GROUP BY ResultCode
ORDER BY ResultCode;

SELECT TOP (30)
       b.Id, b.IdTipoEvento, b.Descripcion,
       u.Username AS PostByUser, b.PostInIP, b.PostTime
FROM dbo.BitacoraEvento AS b
LEFT JOIN dbo.Usuario AS u ON u.Id = b.IdPostByUser
ORDER BY b.Id DESC;

SELECT TOP (20)
       m.Id, e.Nombre, e.ValorDocumentoIdentidad,
       tm.Nombre AS TipoMovimiento, tm.TipoAccion,
       m.Monto, m.NuevoSaldo, m.Fecha,
       u.Username AS PostByUser, m.PostTime
FROM dbo.Movimiento AS m
JOIN dbo.Empleado       AS e  ON e.Id = m.IdEmpleado
JOIN dbo.TipoMovimiento AS tm ON tm.Id = m.IdTipoMovimiento
JOIN dbo.Usuario        AS u  ON u.Id = m.IdPostByUser
ORDER BY m.Id DESC;

SELECT e.ValorDocumentoIdentidad, e.Nombre, e.SaldoVacaciones
FROM dbo.Empleado AS e
ORDER BY e.Nombre;
