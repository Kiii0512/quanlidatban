USE restaurantdb;
GO

IF OBJECT_ID('dbo.sp_CreateEmployeeAccount','P') IS NOT NULL
  DROP PROCEDURE dbo.sp_CreateEmployeeAccount;
GO

CREATE PROCEDURE dbo.sp_CreateEmployeeAccount
  @EmployeeID   VARCHAR(10),
  @Result       INT             OUTPUT,
  @Message      NVARCHAR(200)   OUTPUT,
  @PlainPwd     NVARCHAR(50)    OUTPUT
AS
BEGIN
  SET NOCOUNT ON;

  -- 1) Kiểm tra tồn tại nhân viên
  IF NOT EXISTS (SELECT 1 FROM dbo.Employees WHERE EmployeeID = @EmployeeID)
  BEGIN
    SET @Result   = 0; 
    SET @Message  = N'Nhân viên không tồn tại.';
    RETURN;
  END

  -- 2) Kiểm tra đã có tài khoản chưa
  IF EXISTS (SELECT 1 FROM dbo.EmployeeAccounts WHERE EmployeeID = @EmployeeID)
  BEGIN
    SET @Result   = 0; 
    SET @Message  = N'Nhân viên đã có tài khoản.';
    RETURN;
  END

  -- 3) Sinh salt & hash cho password, ở đây tạm dùng '1234'
  DECLARE @salt VARBINARY(16);
  DECLARE @pw   NVARCHAR(50);
  DECLARE @hash VARBINARY(64);

  SET @salt = CRYPT_GEN_RANDOM(16);
  SET @pw   = N'1234';  -- hoặc sinh random tại đây
  SET @hash = HASHBYTES(
    'SHA2_512',
    @salt + CONVERT(VARBINARY(8000), @pw)
  );

  -- 4) Chèn tài khoản mới, kèm lưu luôn plaintext vào PlainPassword
  INSERT INTO dbo.EmployeeAccounts
    (EmployeeID, Username, Salt, PasswordHash, PlainPassword)
  VALUES
    (@EmployeeID, @EmployeeID, @salt, @hash, @pw);

  -- 5) Đặt output plaintext để Java có thể nhận
  SET @PlainPwd = @pw;

  SET @Result   = 1; 
  SET @Message  = N'Tạo tài khoản thành công. Mật khẩu: ' + @pw;
END;
GO
