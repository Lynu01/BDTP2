/****** Object:  StoredProcedure [dbo].[sp_ListarTiposMovimiento]    Script Date: 10/17/2025 10:11:18 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER     PROCEDURE [dbo].[sp_ListarTiposMovimiento]
AS
BEGIN
    SET NOCOUNT ON;

    SELECT 
        Id, 
        Nombre, 
        TipoAccion
    FROM 
        TipoMovimiento;
END