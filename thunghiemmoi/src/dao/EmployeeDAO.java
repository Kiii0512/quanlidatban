package dao;

import model.Employee;
import util.DatabaseConnector;            // lớp bạn tự viết để lấy Connection
import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public class EmployeeDAO {
    public static Employee findById(String empId) throws SQLException, IOException {
        String sql = "{ call sp_GetEmployeeDetail(?) }";
        try (Connection conn = DatabaseConnector.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, empId);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    Employee e = new Employee();
                    e.setId(rs.getString("EmployeeId"));
                    e.setName(rs.getString("FullName"));
                    e.setAddress(rs.getString("Address"));
                    e.setAge(rs.getInt("Age"));
                    e.setGender(rs.getString("Gender"));
                    e.setTitle(rs.getString("Position"));
                    Blob blob = rs.getBlob("Photo");
                    if (blob != null) {
                        try (InputStream in = blob.getBinaryStream()) {
                            Image img = ImageIO.read(in);
                            e.setPhoto(img);
                        }
                    }
                    return e;
                }
            }
        }
        return null;
    }
}