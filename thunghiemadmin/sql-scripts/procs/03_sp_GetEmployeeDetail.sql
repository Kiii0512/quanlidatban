USE restaurantdb;
GO

IF OBJECT_ID('dbo.sp_GetEmployeeDetail','P') IS NOT NULL
  DROP PROCEDURE dbo.sp_GetEmployeeDetail;
GO

CREATE PROCEDURE dbo.sp_GetEmployeeDetail
  @EmployeeID VARCHAR(10)
AS
BEGIN
  SET NOCOUNT ON;
  SELECT 
    EmployeeID,
    FullName,
    Gender,
    Position,
    Address,
    Age,
    Salary,
    WorkStatus AS Status,
    Photo   
  FROM dbo.Employees
  WHERE EmployeeID = @EmployeeID;
END;
GO
