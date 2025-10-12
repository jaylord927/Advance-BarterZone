package config;

import java.sql.*;

public class config {

    // ✅ Use WAL mode + busy timeout to handle concurrency
    public static Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC");
            // WAL mode (better concurrency) + busy timeout (waits up to 5s if locked)
            con = DriverManager.getConnection("jdbc:sqlite:barterzone.db?journal_mode=WAL&busy_timeout=5000");
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e.getMessage());
        }
        return con;
    }

    // ✅ Utility to safely close resources
    public static void closeQuietly(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception e) {
                    System.out.println("Error closing resource: " + e.getMessage());
                }
            }
        }
    }

    // ✅ Generic Insert (standard version)
    public void addRecord(String sql, Object... values) {
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            bindValues(pstmt, values);
            pstmt.executeUpdate();
            System.out.println("✅ Record added successfully!");

        } catch (SQLException e) {
            System.out.println("⚠ Error adding record: " + e.getMessage());
        }
    }

    // ✅ Safer Insert (extra timeout + retry handling)
    public void addRecordSafe(String sql, Object... values) {
        int retries = 3; // retry up to 3 times
        while (retries-- > 0) {
            try (Connection conn = connectDB();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                bindValues(pstmt, values);
                pstmt.executeUpdate();
                System.out.println("✅ Record added successfully (safe mode)!");
                return; // done if successful

            } catch (SQLException e) {
                if (e.getMessage().contains("database is locked") && retries > 0) {
                    System.out.println("⚠ Database locked. Retrying...");
                    try {
                        Thread.sleep(1000); // wait 1 second before retry
                    } catch (InterruptedException ignored) {}
                } else {
                    System.out.println("❌ Error adding record (safe mode): " + e.getMessage());
                    break;
                }
            }
        }
    }

    // ✅ Helper: Bind all SQL values dynamically
    private void bindValues(PreparedStatement pstmt, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            Object val = values[i];
            if (val instanceof Integer) pstmt.setInt(i + 1, (Integer) val);
            else if (val instanceof Double) pstmt.setDouble(i + 1, (Double) val);
            else if (val instanceof Float) pstmt.setFloat(i + 1, (Float) val);
            else if (val instanceof Long) pstmt.setLong(i + 1, (Long) val);
            else if (val instanceof Boolean) pstmt.setBoolean(i + 1, (Boolean) val);
            else if (val instanceof java.util.Date)
                pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) val).getTime()));
            else if (val instanceof java.sql.Date) pstmt.setDate(i + 1, (java.sql.Date) val);
            else if (val instanceof java.sql.Timestamp) pstmt.setTimestamp(i + 1, (java.sql.Timestamp) val);
            else pstmt.setString(i + 1, val != null ? val.toString() : null);
        }
    }

    // ✅ Dynamic SELECT Viewer
    public void viewRecords(String sqlQuery, String[] columnHeaders, String[] columnNames) {
        if (columnHeaders.length != columnNames.length) {
            System.out.println("Error: Header/Column mismatch.");
            return;
        }

        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
             ResultSet rs = pstmt.executeQuery()) {

            // Print headers
            StringBuilder header = new StringBuilder("---------------------------------------------------------------------------------\n| ");
            for (String h : columnHeaders)
                header.append(String.format("%-20s | ", h));
            header.append("\n---------------------------------------------------------------------------------");
            System.out.println(header);

            // Print data
            while (rs.next()) {
                StringBuilder row = new StringBuilder("| ");
                for (String col : columnNames)
                    row.append(String.format("%-20s | ", rs.getString(col)));
                System.out.println(row);
            }
            System.out.println("---------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("⚠ Error retrieving records: " + e.getMessage());
        }
    }

    // ✅ Update
    public void updateRecord(String sql, Object... values) {
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            bindValues(pstmt, values);
            pstmt.executeUpdate();
            System.out.println("✅ Record updated successfully!");
        } catch (SQLException e) {
            System.out.println("⚠ Error updating record: " + e.getMessage());
        }
    }

    // ✅ Delete
    public void deleteRecord(String sql, Object... values) {
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            bindValues(pstmt, values);
            pstmt.executeUpdate();
            System.out.println("✅ Record deleted successfully!");
        } catch (SQLException e) {
            System.out.println("⚠ Error deleting record: " + e.getMessage());
        }
    }
}


//package config;
//
//import java.sql.*;
//
//
//public class config {
//  
//public static Connection connectDB() {
//        Connection con = null;
//        try {
//            Class.forName("org.sqlite.JDBC"); // Load the SQLite JDBC driver
//            con = DriverManager.getConnection("jdbc:sqlite:barterzone.db"); // Establish connection
//            System.out.println("Connection Successful");
//        } catch (Exception e) {
//            System.out.println("Connection Failed: " + e);
//        }
//        return con;
//    }
//
//
//// === NEW: Alternative connection with WAL mode ===
//    public static Connection connectSafeDB() {
//        Connection con = null;
//        try {
//            Class.forName("org.sqlite.JDBC");
//            // WAL mode allows better concurrency
//            con = DriverManager.getConnection("jdbc:sqlite:barterzone.db?journal_mode=WAL");
//            System.out.println("Connection Successful (WAL mode)");
//        } catch (Exception e) {
//            System.out.println("Connection Failed: " + e);
//        }
//        return con;
//    }
//
//    // === NEW: closeQuietly utility ===
//    public static void closeQuietly(AutoCloseable... resources) {
//        for (AutoCloseable res : resources) {
//            if (res != null) {
//                try {
//                    res.close();
//                } catch (Exception e) {
//                    System.out.println("Error closing resource: " + e.getMessage());
//                }
//            }
//        }
//    }
//
//public void addRecord(String sql, Object... values) {
//    try (Connection conn = this.connectDB(); // Use the connectDB method
//         PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//        // Loop through the values and set them in the prepared statement dynamically
//        for (int i = 0; i < values.length; i++) {
//            if (values[i] instanceof Integer) {
//                pstmt.setInt(i + 1, (Integer) values[i]); // If the value is Integer
//            } else if (values[i] instanceof Double) {
//                pstmt.setDouble(i + 1, (Double) values[i]); // If the value is Double
//            } else if (values[i] instanceof Float) {
//                pstmt.setFloat(i + 1, (Float) values[i]); // If the value is Float
//            } else if (values[i] instanceof Long) {
//                pstmt.setLong(i + 1, (Long) values[i]); // If the value is Long
//            } else if (values[i] instanceof Boolean) {
//                pstmt.setBoolean(i + 1, (Boolean) values[i]); // If the value is Boolean
//            } else if (values[i] instanceof java.util.Date) {
//                pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime())); // If the value is Date
//            } else if (values[i] instanceof java.sql.Date) {
//                pstmt.setDate(i + 1, (java.sql.Date) values[i]); // If it's already a SQL Date
//            } else if (values[i] instanceof java.sql.Timestamp) {
//                pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]); // If the value is Timestamp
//            } else {
//                pstmt.setString(i + 1, values[i].toString()); // Default to String for other types
//            }
//        }
//
//        pstmt.executeUpdate();
//        System.out.println("Record added successfully!");
//    } catch (SQLException e) {
//        System.out.println("Error adding record: " + e.getMessage());
//    }
//}
//
//    // Dynamic view method to display records from any table
//    public void viewRecords(String sqlQuery, String[] columnHeaders, String[] columnNames) {
//        // Check that columnHeaders and columnNames arrays are the same length
//        if (columnHeaders.length != columnNames.length) {
//            System.out.println("Error: Mismatch between column headers and column names.");
//            return;
//        }
//
//        try (Connection conn = this.connectDB();
//             PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
//             ResultSet rs = pstmt.executeQuery()) {
//
//            // Print the headers dynamically
//            StringBuilder headerLine = new StringBuilder();
//            headerLine.append("--------------------------------------------------------------------------------------------------------------------------------------------\n| ");
//            for (String header : columnHeaders) {
//                headerLine.append(String.format("%-20s | ", header)); // Adjust formatting as needed
//            }
//            headerLine.append("\n-------------------------------------------------------------------------------------------------------------------------------------------");
//
//            System.out.println(headerLine.toString());
//
//            // Print the rows dynamically based on the provided column names
//            while (rs.next()) {
//                StringBuilder row = new StringBuilder("| ");
//                for (String colName : columnNames) {
//                    String value = rs.getString(colName);
//                    row.append(String.format("%-20s | ", value != null ? value : "")); // Adjust formatting
//                }
//                System.out.println(row.toString());
//            }
//            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------");
//
//        } catch (SQLException e) {
//            System.out.println("Error retrieving records: " + e.getMessage());
//        }
//    }
//
//    
////-----------------------------------------------
//    // UPDATE METHOD
//    //-----------------------------------------------
//    
//    public void updateRecord(String sql, Object... values) {
//        try (Connection conn = this.connectDB(); // Use the connectDB method
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            // Loop through the values and set them in the prepared statement dynamically
//            for (int i = 0; i < values.length; i++) {
//                if (values[i] instanceof Integer) {
//                    pstmt.setInt(i + 1, (Integer) values[i]); // If the value is Integer
//                } else if (values[i] instanceof Double) {
//                    pstmt.setDouble(i + 1, (Double) values[i]); // If the value is Double
//                } else if (values[i] instanceof Float) {
//                    pstmt.setFloat(i + 1, (Float) values[i]); // If the value is Float
//                } else if (values[i] instanceof Long) {
//                    pstmt.setLong(i + 1, (Long) values[i]); // If the value is Long
//                } else if (values[i] instanceof Boolean) {
//                    pstmt.setBoolean(i + 1, (Boolean) values[i]); // If the value is Boolean
//                } else if (values[i] instanceof java.util.Date) {
//                    pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime())); // If the value is Date
//                } else if (values[i] instanceof java.sql.Date) {
//                    pstmt.setDate(i + 1, (java.sql.Date) values[i]); // If it's already a SQL Date
//                } else if (values[i] instanceof java.sql.Timestamp) {
//                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]); // If the value is Timestamp
//                } else {
//                    pstmt.setString(i + 1, values[i].toString()); // Default to String for other types
//                }
//            }
//
//            pstmt.executeUpdate();
//            System.out.println("Record updated successfully!");
//        } catch (SQLException e) {
//            System.out.println("Error updating record: " + e.getMessage());
//        }
//    }
//
//// Add this method in the config class
//public void deleteRecord(String sql, Object... values) {
//    try (Connection conn = this.connectDB();
//         PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//        // Loop through the values and set them in the prepared statement dynamically
//        for (int i = 0; i < values.length; i++) {
//            if (values[i] instanceof Integer) {
//                pstmt.setInt(i + 1, (Integer) values[i]); // If the value is Integer
//            } else {
//                pstmt.setString(i + 1, values[i].toString()); // Default to String for other types
//            }
//        }
//
//        pstmt.executeUpdate();
//        System.out.println("Record deleted successfully!");
//    } catch (SQLException e) {
//        System.out.println("Error deleting record: " + e.getMessage());
//    }
//}
//
//// === NEW: Safe insert method to avoid SQLITE_BUSY (non-blocking) ===
//public void addRecordSafe(String sql, Object... values) {
//    Connection conn = null;
//    PreparedStatement pstmt = null;
//
//    try {
//        // use busy_timeout + WAL mode for smoother writes
//        conn = DriverManager.getConnection("jdbc:sqlite:barterzone.db?busy_timeout=5000&journal_mode=WAL");
//        pstmt = conn.prepareStatement(sql);
//
//        // dynamically bind parameters (same as your original addRecord)
//        for (int i = 0; i < values.length; i++) {
//            if (values[i] instanceof Integer) {
//                pstmt.setInt(i + 1, (Integer) values[i]);
//            } else if (values[i] instanceof Double) {
//                pstmt.setDouble(i + 1, (Double) values[i]);
//            } else if (values[i] instanceof Float) {
//                pstmt.setFloat(i + 1, (Float) values[i]);
//            } else if (values[i] instanceof Long) {
//                pstmt.setLong(i + 1, (Long) values[i]);
//            } else if (values[i] instanceof Boolean) {
//                pstmt.setBoolean(i + 1, (Boolean) values[i]);
//            } else if (values[i] instanceof java.util.Date) {
//                pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime()));
//            } else if (values[i] instanceof java.sql.Date) {
//                pstmt.setDate(i + 1, (java.sql.Date) values[i]);
//            } else if (values[i] instanceof java.sql.Timestamp) {
//                pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]);
//            } else {
//                pstmt.setString(i + 1, values[i].toString());
//            }
//        }
//
//        pstmt.executeUpdate();
//        System.out.println("✅ Record added successfully (safe mode)!");
//
//    } catch (SQLException e) {
//        System.out.println("⚠ Error adding record (safe mode): " + e.getMessage());
//    } finally {
//        closeQuietly(pstmt, conn); // ensures clean close
//    }
//}
//
//}