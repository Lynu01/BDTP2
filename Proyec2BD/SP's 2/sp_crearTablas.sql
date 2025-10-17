USE BDTP2;
GO

-- Tabla: Puesto
CREATE TABLE dbo.Puesto (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Nombre NVARCHAR(100) NOT NULL,
    SalarioxHora DECIMAL(10,2) NOT NULL,
    PostInIP NVARCHAR(50) NULL,
    PostBy NVARCHAR(50) NULL,
    PostTime DATETIME NULL
);
GO

-- Tabla: Empleado
CREATE TABLE dbo.Empleado (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    IdPuesto INT NOT NULL FOREIGN KEY REFERENCES dbo.Puesto(Id),
    ValorDocumentoIdentidad NVARCHAR(30) NOT NULL,
    Nombre NVARCHAR(100) NOT NULL,
    FechaContratacion DATE NOT NULL,
    SaldoVacaciones DECIMAL(10,2) NOT NULL DEFAULT 0,
    EsActivo BIT NOT NULL DEFAULT 1,
    PostInIP NVARCHAR(50) NULL,
    PostBy NVARCHAR(50) NULL,
    PostTime DATETIME NULL
);
GO

-- Tabla: TipoMovimiento
CREATE TABLE dbo.TipoMovimiento (
    Id INT PRIMARY KEY,
    Nombre NVARCHAR(100) NOT NULL,
    TipoAccion NVARCHAR(20) NOT NULL, -- Credito o Debito
    PostInIP NVARCHAR(50) NULL,
    PostBy NVARCHAR(50) NULL,
    PostTime DATETIME NULL
);
GO

-- Tabla: Usuario
CREATE TABLE dbo.Usuario (
    Id INT PRIMARY KEY,
    Username NVARCHAR(50) NOT NULL,
    Password NVARCHAR(255) NOT NULL,
    PostInIP NVARCHAR(50) NULL,
    PostBy NVARCHAR(50) NULL,
    PostTime DATETIME NULL
);
GO

-- Tabla: Movimiento
CREATE TABLE dbo.Movimiento (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    idEmpleado INT NOT NULL FOREIGN KEY REFERENCES dbo.Empleado(Id),
    idTipoMovimiento INT NOT NULL FOREIGN KEY REFERENCES dbo.TipoMovimiento(Id),
    Fecha DATE NOT NULL,
    Monto DECIMAL(10,2) NOT NULL,
    NuevoSaldo DECIMAL(10,2) NOT NULL,
    IdPostByUser INT NOT NULL FOREIGN KEY REFERENCES dbo.Usuario(Id),
    PostInIP NVARCHAR(50) NULL,
    PostTime DATETIME NULL
);
GO

-- Tabla: TipoEvento
CREATE TABLE dbo.TipoEvento (
    Id INT PRIMARY KEY,
    Nombre NVARCHAR(100) NOT NULL,
    PostInIP NVARCHAR(50) NULL,
    PostBy NVARCHAR(50) NULL,
    PostTime DATETIME NULL
);
GO

-- Tabla: BitacoraEvento
CREATE TABLE dbo.BitacoraEvento (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    IdTipoEvento INT NOT NULL FOREIGN KEY REFERENCES dbo.TipoEvento(Id),
    Descripcion NVARCHAR(4000) NOT NULL,
    IdPostByUser INT NOT NULL FOREIGN KEY REFERENCES dbo.Usuario(Id),
    PostInIP NVARCHAR(50) NULL,
    PostTime DATETIME NULL
);
GO

-- Tabla: DBError (para errores de SQL Server)
CREATE TABLE dbo.DBError (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    UserName NVARCHAR(50),
    Number INT,
    State INT,
    Severity INT,
    Line INT,
    ProcedureName NVARCHAR(255),
    Message NVARCHAR(4000),
    DateTime DATETIME
);
GO

-- Tabla: Error (Catálogo de errores)
CREATE TABLE dbo.Error (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Codigo INT NOT NULL,
    Descripcion NVARCHAR(500) NOT NULL
);
GO

-- Tabla: Feriado
CREATE TABLE dbo.Feriado (
    Id INT IDENTITY(1,1) PRIMARY KEY,   -- PK con autoincremento
    Fecha DATE NOT NULL,                -- Fecha del feriado
    Descripcion NVARCHAR(255) NOT NULL, -- Descripción del feriado
    PostInIP NVARCHAR(50) NULL,          -- IP desde donde se registró (opcional, por consistencia)
    PostBy NVARCHAR(50) NULL,            -- Usuario que registró (opcional, por consistencia)
    PostTime DATETIME NULL               -- Estampa de tiempo (opcional, por consistencia)
);
GO