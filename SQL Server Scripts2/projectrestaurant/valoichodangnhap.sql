USE restaurantdb;
GO

IF OBJECT_ID('dbo.EmployeeAccounts','U') IS NULL
BEGIN
  CREATE TABLE dbo.EmployeeAccounts (
    EmployeeID    VARCHAR(10)   NOT NULL PRIMARY KEY,
    Username      NVARCHAR(50)  NOT NULL UNIQUE,
    Salt          VARBINARY(16) NOT NULL,
    PasswordHash  VARBINARY(64) NOT NULL,
    Status        NVARCHAR(10)  NOT NULL DEFAULT N'Active'
  );
END
GO
