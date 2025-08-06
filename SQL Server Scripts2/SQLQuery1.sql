USE restaurantdb;
GO

-- 1. Tạo bảng users nếu chưa tồn tại
IF OBJECT_ID('dbo.users', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id       INT          IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) NOT NULL UNIQUE,
        password NVARCHAR(50) NOT NULL
    );
END;
GO

-- 2. Chèn user 'admin' nếu chưa tồn tại
IF NOT EXISTS (
    SELECT 1 FROM dbo.users WHERE username = '0010968'
)
BEGIN
    INSERT INTO dbo.users (username, password)
    VALUES ('0010968', '1234');
END;
GO

-- 3. Chèn user 'nhanvien1' nếu chưa tồn tại
IF NOT EXISTS (
    SELECT 1 FROM dbo.users WHERE username = 'nhanvien1'
)
BEGIN
    INSERT INTO dbo.users (username, password)
    VALUES ('nhanvien1', 'pass1');
END;
GO