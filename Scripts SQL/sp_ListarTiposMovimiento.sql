CREATE OR ALTER   PROCEDURE [dbo].[sp_ListarTiposMovimiento]
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