USE restaurantdb;
GO

IF OBJECT_ID('dbo.sp_GetAccountsByStatus','P') IS NOT NULL
  DROP PROCEDURE dbo.sp_GetAccountsByStatus;
GO

CREATE PROCEDURE dbo.sp_GetAccountsByStatus
  @Status NVARCHAR(20)
AS
BEGIN
  SET NOCOUNT ON;

  SELECT
    e.FullName           AS FullName,
    a.EmployeeID         AS EmployeeID,
    -- Salt dưới dạng HEX để hiển thị
    CONVERT(VARCHAR(32),  a.Salt,         2) AS Salt,
    -- Mật khẩu plaintext đã lưu sẵn
    a.PlainPassword      AS PlainPassword
  FROM dbo.EmployeeAccounts a
  JOIN dbo.Employees e
    ON e.EmployeeID = a.EmployeeID
  WHERE a.AccountStatus = @Status
  ORDER BY a.CreatedDate DESC;
END;
GO
