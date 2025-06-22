USE restaurantdb;
GO

-- 1) Xóa bảng con EmployeeAccounts nếu đã tồn tại
IF OBJECT_ID('dbo.EmployeeAccounts','U') IS NOT NULL
BEGIN
    DROP TABLE dbo.EmployeeAccounts;
END
GO

-- 2) Xóa bảng Employees
IF OBJECT_ID('dbo.Employees','U') IS NOT NULL
BEGIN
    DROP TABLE dbo.Employees;
END
GO

-- 3) Tạo lại bảng Employees
CREATE TABLE dbo.Employees (
    EmployeeID   VARCHAR(10)     NOT NULL PRIMARY KEY,
    FullName     NVARCHAR(100)   NOT NULL,
    Address      NVARCHAR(200)   NULL,
    Age          INT             NULL,
    Gender       NCHAR(10)        NULL,
    Position     NVARCHAR(50)    NOT NULL DEFAULT N'Nhân viên',
    Salary       DECIMAL(18,2)   NULL,
    WorkStatus   NVARCHAR(20)    NOT NULL DEFAULT N'Đang làm việc',
    Photo        VARBINARY(MAX)  NULL,
	CreatedDate  DATETIME        NOT NULL DEFAULT GETDATE()
);
GO

-- 4) Tạo lại bảng EmployeeAccounts (bây giờ FK đã không còn vướng)
CREATE TABLE dbo.EmployeeAccounts (
    AccountID     INT            IDENTITY(1,1) PRIMARY KEY,
    EmployeeID    VARCHAR(10)    NOT NULL
        CONSTRAINT FK_Accounts_Employees
        FOREIGN KEY REFERENCES dbo.Employees(EmployeeID),
    Username      NVARCHAR(50)   NOT NULL UNIQUE,
    Salt          VARBINARY(16)  NOT NULL,
    PasswordHash  VARBINARY(64)  NOT NULL,
    PlainPassword   NVARCHAR(50)   NULL,  
    AccountStatus NVARCHAR(20)   NOT NULL DEFAULT N'Active',
    CreatedDate   DATETIME       NOT NULL DEFAULT GETDATE()
);
GO
