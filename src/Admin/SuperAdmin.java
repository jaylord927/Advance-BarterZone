package Admin;

import config.config;
import java.sql.*;

public class SuperAdmin {

    public static void ensureDefaultAdmin(config con) {
        String checkSQL = "SELECT COUNT(*) FROM tbl_admin WHERE admin_username = 'admin'";
        String insertSQL = "INSERT INTO tbl_admin (admin_username, admin_password) VALUES ('admin', 'jaylord')";

        try (Connection conn = con.connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSQL)) {

            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate(insertSQL);
            }

        } catch (Exception e) {
            System.out.println("⚠ Error ensuring default admin account: " + e.getMessage());
        }
    }
}
