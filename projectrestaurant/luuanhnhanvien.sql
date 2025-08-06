USE restaurantdb;
GO

ALTER TABLE Employees
  ADD Photo VARBINARY(MAX) NULL;
GO