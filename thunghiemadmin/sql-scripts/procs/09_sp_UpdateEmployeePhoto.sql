USE restaurantdb;
GO

IF OBJECT_ID('dbo.sp_UpdateEmployeePhoto','P') IS NOT NULL
  DROP PROCEDURE dbo.sp_UpdateEmployeePhoto;
GO

CREATE PROCEDURE dbo.sp_UpdateEmployeePhoto
  @EmployeeID VARCHAR(10),
  @Photo      VARBINARY(MAX)
AS
BEGIN
  SET NOCOUNT ON;
  UPDATE dbo.Employees
  SET Photo = @Photo
  WHERE EmployeeID = @EmployeeID;
END;
GO