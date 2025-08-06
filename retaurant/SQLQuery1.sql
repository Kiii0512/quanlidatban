-- 1.1 Chọn database
USE restaurantdb;
GO

-- 1.2 (Tuỳ chọn) Xóa bảng Admins cũ nếu đã tồn tại
IF OBJECT_ID('dbo.Admins','U') IS NOT NULL
    DROP TABLE dbo.Admins;
GO

-- 1.3 Tạo bảng Admins
CREATE TABLE dbo.Admins (
    AdminID      VARCHAR(10)   NOT NULL PRIMARY KEY,  -- Mã quản lí
    Username     NVARCHAR(50)  NOT NULL UNIQUE,       -- Tên đăng nhập
    Salt         VARBINARY(16) NOT NULL,              -- Salt cho bảo mật
    PasswordHash VARBINARY(64) NOT NULL               -- SHA2_512(salt + mật khẩu)
);
GO
