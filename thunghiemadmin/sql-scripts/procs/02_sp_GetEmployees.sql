USE restaurantdb;
GO

IF OBJECT_ID('dbo.sp_GetEmployees','P') IS NOT NULL
  DROP PROCEDURE dbo.sp_GetEmployees;
GO

CREATE PROCEDURE dbo.sp_GetEmployees
AS
BEGIN
  SET NOCOUNT ON;
  SELECT
    EmployeeID,
    FullName,
    Gender,
    Position
  FROM dbo.Employees
  ORDER BY CreatedDate DESC;
END;
GO
