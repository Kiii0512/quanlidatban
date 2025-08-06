-- 1. Chọn database
USE restaurantdb;
GO

-- 2. Nếu bảng đã tồn tại thì xoá (tuỳ chọn)
IF OBJECT_ID('dbo.Employees', 'U') IS NOT NULL
    DROP TABLE dbo.Employees;
GO

-- 3. Tạo bảng Employees
CREATE TABLE dbo.Employees (
    EmployeeID   VARCHAR(10)    NOT NULL PRIMARY KEY,  -- Mã NV
    FullName     NVARCHAR(100)  NOT NULL,             -- Họ tên
    Salt         VARBINARY(16)  NOT NULL,             -- Salt cho hash
    PasswordHash VARBINARY(64)  NOT NULL              -- SHA2_512(salt + pwd)
);
GO

-- 4. Nếu procedure đã tồn tại thì xoá (tuỳ chọn)
IF OBJECT_ID('dbo.sp_EmployeeLogin', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_EmployeeLogin;
GO

-- 5. Tạo stored procedure sp_EmployeeLogin
CREATE PROCEDURE dbo.sp_EmployeeLogin
    @EmployeeID VARCHAR(10),
    @Password   NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE 
        @StoredHash VARBINARY(64),
        @Salt        VARBINARY(16),
        @InputHash   VARBINARY(64);

    -- Lấy hash và salt từ bảng
    SELECT 
        @StoredHash = PasswordHash,
        @Salt       = Salt
    FROM dbo.Employees
    WHERE EmployeeID = @EmployeeID;

    -- Nếu không tồn tại tài khoản
    IF @StoredHash IS NULL
    BEGIN
        SELECT 
            0 AS LoginResult, 
            N'Tài khoản không tồn tại.' AS Message;
        RETURN;
    END

    -- Tính hash từ mật khẩu nhập vào
    SET @InputHash = HASHBYTES(
        'SHA2_512', 
        @Salt + CONVERT(VARBINARY(8000), @Password)
    );

    -- So sánh hash
    IF @InputHash = @StoredHash
    BEGIN
        -- Đăng nhập thành công
        SELECT 
            1 AS LoginResult, 
            N'Đăng nhập thành công.' AS Message;

        -- Trả về thông tin nhân viên
        SELECT 
            EmployeeID,
            FullName
        FROM dbo.Employees
        WHERE EmployeeID = @EmployeeID;
    END
    ELSE
    BEGIN
        -- Sai mật khẩu
        SELECT 
            0 AS LoginResult, 
            N'Mật khẩu không đúng.' AS Message;
    END
END;
GO
