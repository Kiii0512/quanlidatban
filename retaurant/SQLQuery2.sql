USE restaurantdb;
GO

-- 1) Xoá SP cũ nếu đã tồn tại
IF OBJECT_ID('dbo.sp_EmployeeLogin','P') IS NOT NULL
    DROP PROCEDURE dbo.sp_EmployeeLogin;
GO

-- 2) Tạo lại SP với Role và result‑set thứ hai
CREATE PROCEDURE dbo.sp_EmployeeLogin
  @UserID   VARCHAR(10),
  @Password NVARCHAR(50)
AS
BEGIN
  SET NOCOUNT ON;

  DECLARE
    @StoredHash VARBINARY(64),
    @Salt       VARBINARY(16),
    @InputHash  VARBINARY(64),
    @Role       NVARCHAR(10);

  -- 2.1) Thử lấy trong Employees
  SELECT 
    @StoredHash = PasswordHash,
    @Salt       = Salt
  FROM dbo.Employees
  WHERE EmployeeID = @UserID;

  -- 2.2) Nếu không có trong Employees, thử Admins
  IF @StoredHash IS NULL
  BEGIN
    SELECT
      @StoredHash = PasswordHash,
      @Salt       = Salt
    FROM dbo.Admins
    WHERE AdminID = @UserID;

    SET @Role = N'Admin';
  END
  ELSE
    SET @Role = N'Employee';

  -- 2.3) Nếu vẫn không tìm thấy
  IF @StoredHash IS NULL
  BEGIN
    SELECT
      0        AS LoginResult,
      N'Tài khoản không tồn tại.' AS Message,
      NULL     AS Role;
    RETURN;
  END

  -- 2.4) Tính hash của mật khẩu nhập vào
  SET @InputHash = HASHBYTES(
    'SHA2_512',
    @Salt + CONVERT(VARBINARY(8000), @Password)
  );

  -- 2.5) Nếu mật khẩu sai
  IF @InputHash <> @StoredHash
  BEGIN
    SELECT
      0        AS LoginResult,
      N'Mật khẩu không đúng.' AS Message,
      NULL     AS Role;
    RETURN;
  END

  -- 2.6) Đăng nhập thành công: trả thêm Role
  SELECT
    1        AS LoginResult,
    N'Đăng nhập thành công.' AS Message,
    @Role    AS Role;

  -- 2.7) Result‑set thứ hai: thông tin user (ID + FullName)
  IF @Role = N'Employee'
  BEGIN
    SELECT
      EmployeeID AS UserID,
      FullName
    FROM dbo.Employees
    WHERE EmployeeID = @UserID;
  END
  ELSE  -- Admin
  BEGIN
    SELECT
      AdminID   AS UserID,
      Username  AS FullName
    FROM dbo.Admins
    WHERE AdminID = @UserID;
  END
END;
GO
