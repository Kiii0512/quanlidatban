USE restaurantdb;
GO

DECLARE 
    @salt      VARBINARY(16) = CRYPT_GEN_RANDOM(16),
    @plain_pwd NVARCHAR(50)   = N'Welcome@123';

INSERT INTO dbo.Employees(EmployeeID, FullName, Salt, PasswordHash)
VALUES (
    'NV002',
    N'Trần Thị B',
    @salt,
    HASHBYTES('SHA2_512', @salt + CONVERT(VARBINARY(8000), @plain_pwd))
);
GO
