USE restaurantdb;
GO

IF OBJECT_ID('dbo.sp_UpdateEmployee','P') IS NOT NULL
  DROP PROCEDURE dbo.sp_UpdateEmployee;
GO

CREATE PROCEDURE dbo.sp_UpdateEmployee
  @EmployeeID VARCHAR(10),
  @FullName   NVARCHAR(100),
  @Address    NVARCHAR(200),
  @Age        INT,
  @Position   NVARCHAR(50),
  @Salary     DECIMAL(18,2),
  @WorkStatus NVARCHAR(20),
  @Photo      VARBINARY(MAX)     = NULL   
AS
BEGIN
  SET NOCOUNT ON;
  UPDATE dbo.Employees
  SET
    FullName   = @FullName,
    Address    = @Address,
    Age        = @Age,
    Position   = @Position,
    Salary     = @Salary,
    WorkStatus = @WorkStatus,
    Photo      = COALESCE(@Photo, Photo)
  WHERE EmployeeID = @EmployeeID;
END;
GO
