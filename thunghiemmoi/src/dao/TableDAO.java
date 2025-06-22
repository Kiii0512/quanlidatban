package dao;

import model.Table;
import util.DatabaseConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {
    /** Lấy toàn bộ danh sách bàn từ database */
    public List<Table> getAll() throws SQLException {
        List<Table> list = new ArrayList<>();
        try (Connection conn = DatabaseConnector.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_GetTables}")) {
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                list.add(new Table(
                    rs.getString("TableID"),
                    rs.getString("TableType"),
                    rs.getInt("SeatCount"),
                    rs.getString("Status")
                ));
            }
        }
        return list;
    }

    /** Thêm bàn mới */
    public void add(Table t) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_AddTable(?,?,?,?)}")) {
            cs.setString(1, t.getTableID());
            cs.setString(2, t.getTableType());
            cs.setInt(3, t.getSeatCount());
            cs.setString(4, t.getStatus());
            cs.execute();
        }
    }
    public void update(Table t) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection(); CallableStatement cs = conn.prepareCall("{call sp_UpdateTable(?,?,?,?)}")) {
            cs.setString(1, t.getTableID());
            cs.setString(2, t.getTableType());
            cs.setInt(3, t.getSeatCount());
            cs.setString(4, t.getStatus());
            cs.execute();
        }
    }
    /** Xóa bàn theo TableID */
    public void delete(String tableID) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_DeleteTable(?)}")) {
            cs.setString(1, tableID);
            cs.execute();
        }
    }
}