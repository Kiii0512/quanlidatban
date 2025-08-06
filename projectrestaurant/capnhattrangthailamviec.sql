ALTER TABLE dbo.Employees
ADD Status NVARCHAR(20) NOT NULL
    CONSTRAINT DF_Employees_Status DEFAULT N'Đang làm việc';
