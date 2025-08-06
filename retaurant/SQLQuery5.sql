USE restaurantdb;
GO
ALTER PROCEDURE dbo.sp_AdminLogin
    @AdminID  VARCHAR(10),
    @Username NVARCHAR(50),
    @Password NVARCHAR(50)
AS
BEGIN
  SET NOCOUNT ON;
  DECLARE @Salt VARBINARY(16), @StoredHash VARBINARY(64), @InputHash VARBINARY(64);

  SELECT @Salt = Salt, @StoredHash = PasswordHash
  FROM dbo.Admins
  WHERE AdminID = @AdminID
    AND Username = @Username;

  IF @StoredHash IS NULL
  BEGIN
    SELECT 0 AS LoginResult, N'Tài khoản không tồn tại.' AS Message;
    RETURN;
  END

  SET @InputHash = HASHBYTES('SHA2_512', @Salt + CONVERT(VARBINARY(8000), @Password));

  IF @InputHash = @StoredHash
    SELECT 1 AS LoginResult, N'Đăng nhập thành công.' AS Message;
  ELSE
    SELECT 0 AS LoginResult, N'Mật khẩu không đúng.' AS Message;
END;
GO
