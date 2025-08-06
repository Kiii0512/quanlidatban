USE restaurantdb;
GO
IF OBJECT_ID('dbo.sp_UpdateTable','P') IS NOT NULL
  DROP PROCEDURE dbo.sp_UpdateTable;
GO
CREATE PROCEDURE dbo.sp_UpdateTable
  @TableID   VARCHAR(10),
  @TableType VARCHAR(20)=NULL,
  @SeatCount INT        =NULL,
  @Status    NVARCHAR(20)=NULL
AS
BEGIN
  SET NOCOUNT ON;
  UPDATE dbo.TableList
  SET
    TableType = COALESCE(@TableType, TableType),
    SeatCount = COALESCE(@SeatCount, SeatCount),
    Status    = COALESCE(@Status,    Status)
  WHERE TableID = @TableID;
END
GO