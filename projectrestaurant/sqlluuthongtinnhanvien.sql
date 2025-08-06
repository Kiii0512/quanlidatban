USE restaurantdb;
GO

-- Xóa bảng EmployeeAccounts cũ nếu đã tồn tại
IF OBJECT_ID('dbo.EmployeeAccounts','U') IS NOT NULL
    DROP TABLE dbo.EmployeeAccounts;
GO

-- Tạo bảng EmployeeAccounts
CREATE TABLE dbo.EmployeeAccounts (
    AccountID     INT            IDENTITY(1,1) PRIMARY KEY,
    EmployeeID    VARCHAR(10)    NOT NULL
        CONSTRAINT FK_Accounts_Employees FOREIGN KEY REFERENCES dbo.Employees(EmployeeID),
    Username      NVARCHAR(50)   NOT NULL UNIQUE,        -- dùng để login, mặc định bằng mã nhân viên
    Salt          VARBINARY(16)  NOT NULL,               -- salt cho hash
    PasswordHash  VARBINARY(64)  NOT NULL,               -- SHA2_512(salt + mật khẩu)
    AccountStatus NVARCHAR(20)   NOT NULL DEFAULT N'Active',  -- 'Active' / 'Inactive'
    CreatedDate   DATETIME       NOT NULL DEFAULT GETDATE()
);
GO
