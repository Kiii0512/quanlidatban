USE restaurantdb;
GO

-- Xóa các bảng đã tồn tại (nếu có)
IF OBJECT_ID('CompletedReservations', 'U') IS NOT NULL
    DROP TABLE CompletedReservations;
IF OBJECT_ID('CancelledReservations', 'U') IS NOT NULL
    DROP TABLE CancelledReservations;
IF OBJECT_ID('Reservations', 'U') IS NOT NULL
    DROP TABLE Reservations;
GO

-- Tạo lại 3 bảng với cột email
CREATE TABLE Reservations (
    reservation_id   INT IDENTITY(1,1) PRIMARY KEY,
    customer_name    NVARCHAR(100) NOT NULL,
    email            NVARCHAR(100) NOT NULL,
    phone_number     NVARCHAR(50)  NOT NULL,
    reservation_date DATE          NOT NULL,
    reservation_time TIME          NOT NULL,
    guest_count      INT           NOT NULL,
    message          NVARCHAR(255) NULL,
    created_at       DATETIME      DEFAULT GETDATE()
);

CREATE TABLE CancelledReservations (
    cancel_id        INT IDENTITY(1,1) PRIMARY KEY,
    reservation_id   INT           NOT NULL,
    customer_name    NVARCHAR(100) NOT NULL,
    email            NVARCHAR(100) NOT NULL,
    phone_number     NVARCHAR(50)  NOT NULL,
    reservation_date DATE          NOT NULL,
    reservation_time TIME          NOT NULL,
    guest_count      INT           NOT NULL,
    message          NVARCHAR(255) NULL,
    cancelled_at     DATETIME      DEFAULT GETDATE()
);

CREATE TABLE CompletedReservations (
    complete_id      INT IDENTITY(1,1) PRIMARY KEY,
    reservation_id   INT           NOT NULL,
    customer_name    NVARCHAR(100) NOT NULL,
    email            NVARCHAR(100) NOT NULL,
    phone_number     NVARCHAR(50)  NOT NULL,
    reservation_date DATE          NOT NULL,
    reservation_time TIME          NOT NULL,
    guest_count      INT           NOT NULL,
    message          NVARCHAR(255) NULL,
    completed_at     DATETIME      DEFAULT GETDATE()
);
GO
