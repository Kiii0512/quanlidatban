USE restaurantdb;
GO

-- 1. Tạo bảng lưu thông tin bàn
IF OBJECT_ID('dbo.TableList', 'U') IS NOT NULL
    DROP TABLE dbo.TableList;
GO

CREATE TABLE dbo.TableList (
    TableID      VARCHAR(10)    NOT NULL PRIMARY KEY,
    TableType    NVARCHAR(20)    NOT NULL,       -- e.g. 'Thường', 'VIP'
    SeatCount    INT            NOT NULL,       -- số chỗ
    Status       NVARCHAR(20)    NOT NULL        -- e.g. 'Còn trống', 'Đang sử dụng', 'Đã được đặt', 'Không khả dụng'
);
GO


-- 2. Procedure: Lấy danh sách bàn
IF OBJECT_ID('dbo.sp_GetTables', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_GetTables;
GO

CREATE PROCEDURE dbo.sp_GetTables
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        TableID,
        TableType,
        SeatCount,
        Status
    FROM dbo.TableList
    ORDER BY
        -- sắp xếp theo số bàn (nếu TableID là số), nếu không thì theo TableID string
        TRY_CAST(TableID AS INT),
        TableID;
END
GO


-- 3. Procedure: Thêm bàn mới
IF OBJECT_ID('dbo.sp_AddTable', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_AddTable;
GO

CREATE PROCEDURE dbo.sp_AddTable
    @TableID     VARCHAR(10),
    @TableType   NVARCHAR(20),
    @SeatCount   INT,
    @Status      NVARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (SELECT 1 FROM dbo.TableList WHERE TableID = @TableID)
    BEGIN
        RAISERROR('TableID %s đã tồn tại.', 16, 1, @TableID);
        RETURN;
    END

    INSERT INTO dbo.TableList (TableID, TableType, SeatCount, Status)
    VALUES (@TableID, @TableType, @SeatCount, @Status);
END
GO


-- 4. Procedure: Xóa bàn
IF OBJECT_ID('dbo.sp_DeleteTable', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_DeleteTable;
GO

CREATE PROCEDURE dbo.sp_DeleteTable
    @TableID VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM dbo.TableList WHERE TableID = @TableID)
    BEGIN
        RAISERROR('Không tìm thấy TableID %s để xóa.', 16, 1, @TableID);
        RETURN;
    END

    DELETE FROM dbo.TableList
    WHERE TableID = @TableID;
END
GO


-- 5. (Tùy chọn) Procedure: Cập nhật thông tin bàn (ví dụ đổi trạng thái)
IF OBJECT_ID('dbo.sp_UpdateTable', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_UpdateTable;
GO

CREATE PROCEDURE dbo.sp_UpdateTable
    @TableID     VARCHAR(10),
    @TableType   NVARCHAR(20)    = NULL,
    @SeatCount   INT            = NULL,
    @Status      NVARCHAR(20)    = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM dbo.TableList WHERE TableID = @TableID)
    BEGIN
        RAISERROR('Không tìm thấy TableID %s để cập nhật.', 16, 1, @TableID);
        RETURN;
    END

    UPDATE dbo.TableList
    SET
        TableType = COALESCE(@TableType, TableType),
        SeatCount = COALESCE(@SeatCount, SeatCount),
        Status    = COALESCE(@Status,    Status)
    WHERE TableID = @TableID;
END
GO