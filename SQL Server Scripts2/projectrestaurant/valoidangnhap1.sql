USE restaurantdb;
GO

ALTER PROCEDURE dbo.sp_EmployeeLogin
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

  -- 1) Tìm trong EmployeeAccounts với Status = 'Active'
  SELECT 
    @StoredHash = ea.PasswordHash,
    @Salt       = ea.Salt
  FROM dbo.EmployeeAccounts ea
  WHERE ea.Username = @UserID
    AND ea.Status   = N'Active';

  IF @StoredHash IS NOT NULL
    SET @Role = N'Employee';

  -- 2) Nếu không tìm thấy, thử Admins
  IF @StoredHash IS NULL
  BEGIN
    SELECT 
      @StoredHash = a.PasswordHash,
      @Salt       = a.Salt
    FROM dbo.Admins a
    WHERE a.Username = @UserID;

    IF @StoredHash IS NOT NULL
      SET @Role = N'Admin';
  END

  -- 3) Không tìm thấy ở cả hai bảng
  IF @StoredHash IS NULL
  BEGIN
    SELECT 0 AS LoginResult, N'Tài khoản không tồn tại.' AS Message, NULL AS Role;
    RETURN;
  END

  -- 4) Tính hash và so sánh
  SET @InputHash = HASHBYTES('SHA2_512', @Salt + CONVERT(VARBINARY(8000), @Password));
  IF @InputHash <> @StoredHash
  BEGIN
    SELECT 0 AS LoginResult, N'Mật khẩu không đúng.' AS Message, NULL AS Role;
    RETURN;
  END

  -- 5) Đăng nhập thành công: trả Role
  SELECT 1 AS LoginResult, N'Đăng nhập thành công.' AS Message, @Role AS Role;

  -- 6) Trả thêm thông tin user
  IF @Role = N'Employee'
  BEGIN
    SELECT e.EmployeeID AS UserID, e.FullName
    FROM dbo.Employees e
    WHERE e.EmployeeID = @UserID;
  END
  ELSE  -- Admin
  BEGIN
    SELECT a.AdminID   AS UserID, a.Username AS FullName
    FROM dbo.Admins a
    WHERE a.Username = @UserID;
  END
END;
GO
