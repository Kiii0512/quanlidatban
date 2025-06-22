USE restaurantdb;
GO
IF OBJECT_ID('dbo.sp_ResetEmployeePassword','P') IS NOT NULL
  DROP PROCEDURE dbo.sp_ResetEmployeePassword;
GO
CREATE PROCEDURE dbo.sp_ResetEmployeePassword
  @EmployeeID  VARCHAR(10),
  @FullName    NVARCHAR(100),
  @NewPassword NVARCHAR(50),
  @Result      INT           OUTPUT,
  @Message     NVARCHAR(200) OUTPUT
AS
BEGIN
  SET NOCOUNT ON;


  IF NOT EXISTS (SELECT 1 
                  FROM dbo.Employees 
                  WHERE EmployeeID=@EmployeeID 
                    AND FullName=@FullName)
  BEGIN
    SET @Result = 0;
    SET @Message = N'Thông tin nhân viên không chính xác.';
    RETURN;
  END


  DECLARE @salt VARBINARY(16) = CRYPT_GEN_RANDOM(16);
  DECLARE @hash VARBINARY(64) = HASHBYTES('SHA2_512', @salt + CONVERT(VARBINARY(8000), @NewPassword));


  UPDATE dbo.EmployeeAccounts
  SET Salt=@salt,
      PasswordHash=@hash,
     PlainPassword=@NewPassword 
  WHERE EmployeeID=@EmployeeID;

  SET @Result = 1;
  SET @Message = N'Cấp lại mật khẩu thành công.';
END;
GO
