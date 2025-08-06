USE restaurantdb;
GO

ALTER TABLE dbo.EmployeeAccounts
ADD Status NVARCHAR(10) NOT NULL
  CONSTRAINT DF_EmployeeAccounts_Status DEFAULT N'Active';
GO
