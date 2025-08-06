USE restaurantdb;
GO

-- 3.1.1 Sinh salt và hash cho mật khẩu mẫu "Admin@123"
DECLARE 
    @salt VARBINARY(16)   = CRYPT_GEN_RANDOM(16),
    @plain_pwd NVARCHAR(50) = N'Admin@123';

-- 3.1.2 Chèn admin với Username = 'admin'
INSERT INTO dbo.Admins(AdminID, Username, Salt, PasswordHash)
VALUES (
    'ADMIN1',          -- Mã quản lí
    N'admin',          -- Tên đăng nhập
    @salt,             
    HASHBYTES(
        'SHA2_512', 
        @salt + CONVERT(VARBINARY(8000), @plain_pwd)
    )
);
GO
