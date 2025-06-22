USE restaurantdb;
GO

IF OBJECT_ID('dbo.sp_AddEmployee','P') IS NOT NULL
  DROP PROCEDURE dbo.sp_AddEmployee;
GO

CREATE PROCEDURE dbo.sp_AddEmployee
  @FullName NVARCHAR(100),
  @Address  NVARCHAR(200),
  @Age      INT,
  @Gender   NCHAR(1),
  @Salary   DECIMAL(18,2),
  @Photo      VARBINARY(MAX)     = NULL,   
  @NewEmpID VARCHAR(10) OUTPUT
AS
BEGIN
  SET NOCOUNT ON;

  DECLARE
    @Prefix NVARCHAR(6) = FORMAT(GETDATE(),'yyyyMM'),
    @MaxID  VARCHAR(10);

  SELECT @MaxID = MAX(EmployeeID)
  FROM dbo.Employees
  WHERE EmployeeID LIKE 'EMP' + @Prefix + '%';

  IF @MaxID IS NULL
    SET @NewEmpID = 'EMP' + @Prefix + '001';
  ELSE
    SET @NewEmpID = 'EMP' + @Prefix
      + RIGHT('000' + CAST(CAST(RIGHT(@MaxID,3) AS INT) + 1 AS VARCHAR(3)), 3);

  INSERT INTO dbo.Employees
    (EmployeeID, FullName, Address, Age, Gender, Salary, Photo)
  VALUES
    (@NewEmpID, @FullName, @Address, @Age, @Gender, @Salary, @Photo);

  SELECT @NewEmpID AS EmployeeID;
END;
GO
