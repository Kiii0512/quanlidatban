USE restaurantdb;
GO
IF OBJECT_ID('dbo.sp_EmployeeLogin','P') IS NOT NULL
  DROP PROCEDURE dbo.sp_EmployeeLogin;
GO

CREATE PROCEDURE dbo.sp_EmployeeLogin
  @EmployeeID VARCHAR(10),
  @Password   NVARCHAR(50)
AS
BEGIN
  SET NOCOUNT ON;

  DECLARE @dbHash VARBINARY(64),
          @dbSalt VARBINARY(16),
          @calcHash VARBINARY(64),
          @loginResult INT,
          @message NVARCHAR(200),
          @role NVARCHAR(20);

  -- 1) Lấy hash/salt từ DB
  SELECT @dbSalt = Salt,
         @dbHash = PasswordHash
    FROM dbo.EmployeeAccounts
   WHERE EmployeeID = @EmployeeID;

  IF @dbHash IS NULL
  BEGIN
    SET @loginResult = 0;
    SET @message     = N'Tài khoản không tồn tại.';
    SET @role        = N'';
  END
  ELSE
  BEGIN
    -- 2) Tính hash trên mật khẩu nhập vào
    SET @calcHash = HASHBYTES('SHA2_512', @dbSalt + CONVERT(VARBINARY(8000), @Password));

    IF @calcHash = @dbHash
    BEGIN
      SET @loginResult = 1;
      SET @message     = N'Đăng nhập thành công.';
      SET @role        = N'Employee';
    END
    ELSE
    BEGIN
      SET @loginResult = 0;
      SET @message     = N'Mật khẩu không đúng.';
      SET @role        = N'';
    END
  END

  -- 3) Trả result-set đầu: LoginResult, Message, Role
  SELECT
    @loginResult AS LoginResult,
    @message     AS Message,
    @role        AS Role;

  -- 4) Nếu thành công, trả thêm result-set với thông tin user
  IF @loginResult = 1
  BEGIN
    SELECT
      e.EmployeeID AS UserID,
      e.FullName,
      e.WorkStatus AS Status
    FROM dbo.Employees e
    WHERE e.EmployeeID = @EmployeeID;
  END
END;
GO
