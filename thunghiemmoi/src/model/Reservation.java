package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reservation {
    private int reservationId;
    private String customerName;
    private String email;              // ← thêm
    private String phoneNumber;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private int guestCount;
    private String message;

    // Constructor khi thêm mới (không có ID):
    public Reservation(String customerName, String email,
                       String phoneNumber, LocalDate reservationDate,
                       LocalTime reservationTime, int guestCount,
                       String message) {
        this.customerName    = customerName;
        this.email           = email;
        this.phoneNumber     = phoneNumber;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.guestCount      = guestCount;
        this.message         = message;
    }

    // Constructor đầy đủ (có ID):
    public Reservation(int reservationId, String customerName, String email,
                       String phoneNumber, LocalDate reservationDate,
                       LocalTime reservationTime, int guestCount,
                       String message) {
        this.reservationId   = reservationId;
        this.customerName    = customerName;
        this.email           = email;
        this.phoneNumber     = phoneNumber;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.guestCount      = guestCount;
        this.message         = message;
    }
    // --- getters & setters --
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public int getReservationId() { return reservationId; }
    public String getCustomerName() { return customerName; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getReservationDate() { return reservationDate; }
    public LocalTime getReservationTime() { return reservationTime; }
    public int getGuestCount() { return guestCount; }
    public String getMessage() { return message; }

    public void setReservationId(int reservationId) { this.reservationId = reservationId; }
    // (các setter khác nếu cần)
}