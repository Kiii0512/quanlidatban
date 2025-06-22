package dao;

import model.Reservation;
import util.DatabaseConnector;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.sql.Timestamp;

public class ReservationDAO {
    private Connection conn;
    public ReservationDAO() throws SQLException {
        conn = DatabaseConnector.getConnection();
    }
    // 1. Thêm mới reservation
    public void addReservation(Reservation r) throws SQLException {
        String sql = "INSERT INTO Reservations "
                + "(customer_name, email, phone_number, reservation_date, reservation_time, guest_count, message) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, r.getCustomerName());
            pst.setString(2, r.getEmail());                   // ← mới
            pst.setString(3, r.getPhoneNumber());
            pst.setDate(4, Date.valueOf(r.getReservationDate()));
            LocalDateTime ldt = LocalDateTime.of(r.getReservationDate(), r.getReservationTime());
            pst.setTimestamp(5, Timestamp.valueOf(ldt));
            pst.setInt(6, r.getGuestCount());
            pst.setString(7, r.getMessage());
            pst.executeUpdate();
        }
    }

    // 2. Lấy danh sách Reservations
    public List<Reservation> getAllReservations() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT reservation_id, customer_name, email, phone_number, "
                + "reservation_date, reservation_time, guest_count, message "
                + "FROM Reservations "
                + "ORDER BY created_at DESC";

        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int idx = 1;
                int id = rs.getInt(idx++);
                String name = rs.getString(idx++);
                String email = rs.getString(idx++);               // ← mới
                String phone = rs.getString(idx++);
                // Lấy ngày
                LocalDate date = rs.getDate(idx++).toLocalDate();
                // Lấy datetime2 rồi tách ra LocalTime
                LocalDateTime dt = rs.getTimestamp(idx++).toLocalDateTime();
                LocalTime time = dt.toLocalTime();

                int count = rs.getInt(idx++);
                String msg = rs.getString(idx++);
                list.add(new Reservation(id, name, email, phone, date, time, count, msg));
            }
        }
        return list;
    }
    // Tìm reservation_id theo giá trị duy nhất

    public int findReservationId(
            String name, String email, String phone,
            LocalDate date, LocalTime time, int guestCount // đổi tên cho rõ ràng
    ) throws SQLException {

        String sql = ""
                + "SELECT reservation_id FROM Reservations "
                + " WHERE customer_name     = ?"
                + "   AND email             = ?"
                + "   AND phone_number      = ?"
                + "   AND reservation_date  = ?"
                + "   AND reservation_time  = ?" // cột TIME, so sánh trực tiếp
                + "   AND guest_count       = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, name);
            pst.setString(2, email);
            pst.setString(3, phone);
            pst.setDate(4, Date.valueOf(date));
// trở về dùng Time
            LocalDateTime ldt2 = LocalDateTime.of(date, time);
            pst.setTimestamp(5, Timestamp.valueOf(ldt2));
            pst.setInt(6, guestCount);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("reservation_id");
                }
                throw new SQLException("Không tìm thấy reservation_id");
            }
        }
    }

// Cancel theo giá trị
    public void cancelByInfo(String name, String email, String phone,
            LocalDate date, LocalTime time, int tableNo) throws SQLException {
        int id = findReservationId(name, email, phone, date, time, tableNo);
        cancelReservation(id);
    }

// Complete theo giá trị
    public void completeByInfo(String name, String email, String phone,
            LocalDate date, LocalTime time, int tableNo) throws SQLException {
        int id = findReservationId(name, email, phone, date, time, tableNo);
        completeReservation(id);
    }

    public List<Reservation> getAllCancelled() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = ""
                + "SELECT reservation_id, customer_name, email, phone_number, "
                + "       reservation_date, reservation_time, guest_count, message "
                + "FROM CancelledReservations "
                + "ORDER BY cancelled_at DESC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int idx = 1;
                int id = rs.getInt(idx++);
                String name = rs.getString(idx++);
                String email = rs.getString(idx++);
                String phone = rs.getString(idx++);
                
                LocalDate date = rs.getDate(idx++).toLocalDate();
                LocalDateTime dt = rs.getTimestamp(idx++).toLocalDateTime();
                LocalTime time = dt.toLocalTime();
                
                int count = rs.getInt(idx++);
                String msg = rs.getString(idx++);
                list.add(new Reservation(id, name, email, phone, date, time, count, msg));
            }
        }
        return list;
    }

// Lấy danh sách đơn đã hoàn tất
    public List<Reservation> getAllCompleted() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = ""
                + "SELECT reservation_id, customer_name, email, phone_number, "
                + "reservation_date, reservation_time, guest_count, message "
                + "FROM CompletedReservations "
                + "ORDER BY completed_at DESC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int idx = 1;
                int id = rs.getInt(idx++);
                String name = rs.getString(idx++);
                String email = rs.getString(idx++);
                String phone = rs.getString(idx++);
                
                LocalDate date = rs.getDate(idx++).toLocalDate();
                LocalDateTime dt = rs.getTimestamp(idx++).toLocalDateTime();
                LocalTime time = dt.toLocalTime();
                
                int count = rs.getInt(idx++);
                String msg = rs.getString(idx++);
                list.add(new Reservation(id, name, email, phone, date, time, count, msg));
            }
        }
        return list;
    }

    // 3. Hủy đơn
    public void cancelReservation(int id) throws SQLException {
        conn.setAutoCommit(false);
        try {
            String ins = "INSERT INTO CancelledReservations "
                    + "(reservation_id, customer_name,email, phone_number, reservation_date, reservation_time, guest_count, message) "
                    + "SELECT reservation_id, customer_name,email, phone_number, reservation_date, reservation_time, guest_count, message "
                    + "FROM Reservations WHERE reservation_id = ?";
            try (PreparedStatement pst1 = conn.prepareStatement(ins)) {
                pst1.setInt(1, id);
                pst1.executeUpdate();
            }
            try (PreparedStatement pst2 = conn.prepareStatement(
                    "DELETE FROM Reservations WHERE reservation_id = ?")) {
                pst2.setInt(1, id);
                pst2.executeUpdate();
            }
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // 4. Hoàn tất đơn
    public void completeReservation(int id) throws SQLException {
        conn.setAutoCommit(false);
        try {
            String ins = "INSERT INTO CompletedReservations "
                    + "(reservation_id, customer_name,email, phone_number, reservation_date, reservation_time, guest_count, message) "
                    + "SELECT reservation_id, customer_name,email, phone_number, reservation_date, reservation_time, guest_count, message "
                    + "FROM Reservations WHERE reservation_id = ?";
            try (PreparedStatement pst1 = conn.prepareStatement(ins)) {
                pst1.setInt(1, id);
                pst1.executeUpdate();
            }
            try (PreparedStatement pst2 = conn.prepareStatement(
                    "DELETE FROM Reservations WHERE reservation_id = ?")) {
                pst2.setInt(1, id);
                pst2.executeUpdate();
            }
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
