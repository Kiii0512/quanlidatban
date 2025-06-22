USE restaurantdb;
GO

IF OBJECT_ID('dbo.sp_DeleteEmployee','P') IS NOT NULL
  DROP PROCEDURE dbo.sp_DeleteEmployee;
GO

CREATE PROCEDURE dbo.sp_DeleteEmployee
  @EmployeeID VARCHAR(10)
AS
BEGIN
  SET NOCOUNT ON;
  DELETE FROM dbo.EmployeeAccounts
  WHERE EmployeeID = @EmployeeID;

  DELETE FROM dbo.Employees
  WHERE EmployeeID = @EmployeeID;
END;
GO
