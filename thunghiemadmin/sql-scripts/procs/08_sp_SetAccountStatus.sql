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
    e.FullName    AS FullName,
    e.Address     AS Address,
    e.Age         AS Age,
    e.Gender      AS Gender,
    e.Position    AS Position,
    e.Salary      AS Salary,
    e.WorkStatus  AS Status,       -- đổi thành WorkStatus và alias thành Status
    e.Photo       AS Photo
  FROM dbo.Employees e
  WHERE e.EmployeeID = @EmployeeID;
END;
GO
