USE restaurantdb;
GO

-- Xóa bảng Employees cũ nếu đã tồn tại
IF OBJECT_ID('dbo.Employees','U') IS NOT NULL
    DROP TABLE dbo.Employees;
GO

-- Tạo bảng Employees
CREATE TABLE dbo.Employees (
    EmployeeID   VARCHAR(10)     NOT NULL PRIMARY KEY,     -- mã nhân viên do hệ thống cấp
    FullName     NVARCHAR(100)   NOT NULL,                 -- họ tên
    Address      NVARCHAR(200)   NULL,                     -- địa chỉ
    Age          INT             NULL,                     -- tuổi
    Gender       NCHAR(1)        NULL,                     -- 'M' hoặc 'F'
    Position     NVARCHAR(50)    NOT NULL DEFAULT N'Nhân viên',  -- 'Nhân viên' hoặc 'Quản lý'
    Salary       DECIMAL(18,2)   NULL,                     -- lương
    WorkStatus   NVARCHAR(20)    NOT NULL DEFAULT N'Đang làm việc',  -- 'Đang làm việc', 'Đã nghỉ việc',...
    CreatedDate  DATETIME        NOT NULL DEFAULT GETDATE()        -- ngày tạo
);
GO
