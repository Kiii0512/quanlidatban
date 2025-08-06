USE restaurantdb;
GO

-- Nếu bạn muốn thay thế hoàn toàn:
IF OBJECT_ID('dbo.sp_EmployeeLogin','P') IS NOT NULL
  DROP PROCEDURE dbo.sp_EmployeeLogin;
GO

CREATE PROCEDURE dbo.sp_EmployeeLogin
    @EmployeeID VARCHAR(10),
    @Password   NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE
      @StoredHash VARBINARY(64),
      @Salt       VARBINARY(16),
      @InputHash  VARBINARY(64);

    -- 1) Thử Employees
    SELECT @StoredHash = PasswordHash,
           @Salt       = Salt
    FROM dbo.Employees
    WHERE EmployeeID = @EmployeeID;

    -- 2) Nếu không tìm, thử Admins
    IF @StoredHash IS NULL
    BEGIN
        SELECT @StoredHash = PasswordHash,
               @Salt       = Salt
        FROM dbo.Admins
        WHERE AdminID = @EmployeeID;
    END

    -- 3) Nếu vẫn không có
    IF @StoredHash IS NULL
    BEGIN
        SELECT 0 AS LoginResult, N'Tài khoản không tồn tại.' AS Message;
        RETURN;
    END

    -- 4) Tính hash và so sánh
    SET @InputHash = HASHBYTES('SHA2_512', @Salt + CONVERT(VARBINARY(8000), @Password));

    IF @InputHash = @StoredHash
    BEGIN
        -- 4.1) Kết quả login thành công
        SELECT 1 AS LoginResult, N'Đăng nhập thành công.' AS Message;

        -- 4.2) **TRẢ THÊM** thông tin nhân viên (fullName, v.v.)
        SELECT EmployeeID, FullName
        FROM dbo.Employees
        WHERE EmployeeID = @EmployeeID;
    END
    ELSE
    BEGIN
        SELECT 0 AS LoginResult, N'Mật khẩu không đúng.' AS Message;
    END
END;
GO
