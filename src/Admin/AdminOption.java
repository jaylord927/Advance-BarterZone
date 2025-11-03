package Admin;

import config.config;
import java.sql.*;
import java.util.Scanner;

public class AdminOption {

    private config con;

    public AdminOption(config con) {
        this.con = con;
    }

    // ----------------------------------------------------
    // VIEW TRADERS
    // ----------------------------------------------------
    public void viewTraders() {
        String query = "SELECT * FROM tbl_trader";
        String[] headers = {"ID", "Username", "Full Name", "Email", "Contact", "Location", "Status"};
        String[] columns = {"trader_id", "tbl_Username", "tbl_FullName", "tbl_Email", "tbl_Contact", "tbl_Location", "tbl_Status"};
        con.viewRecords(query, headers, columns);
    }

    // ----------------------------------------------------
    // UPDATE TRADER STATUS
    // ----------------------------------------------------
    public void updateTraderStatus(Scanner scan) {
        viewTraders();
        System.out.print("Enter Trader ID to update status: ");
        int traderId = scan.nextInt();
        scan.nextLine();

        System.out.print("Enter new status (pending / approved / declined): ");
        String newStatus = scan.nextLine().trim().toLowerCase();

        if (!newStatus.equals("pending") && !newStatus.equals("approved") && !newStatus.equals("declined")) {
            System.out.println(" Invalid status. Must be: pending / approved / declined.");
            return;
        }

        System.out.print("Are you sure you want to update this trader's status to '" + newStatus + "'? (yes/no): ");
        String confirm = scan.nextLine().trim().toLowerCase();

        if (confirm.equals("yes")) {
            String sqlupdate = "UPDATE tbl_trader SET tbl_Status = ? WHERE trader_id = ?";
            con.updateRecord(sqlupdate, newStatus, traderId);
            System.out.println(" Trader status updated successfully!");
        } else {
            System.out.println(" Update cancelled.");
        }
    }

    // ----------------------------------------------------
    // DELETE TRADER
    // ----------------------------------------------------
    public void deleteTrader(Scanner scan) {
        viewTraders();
        System.out.print("Enter Trader ID to delete: ");
        int traderId = scan.nextInt();
        scan.nextLine();

        System.out.print("Are you sure you want to DELETE this trader account? (yes/no): ");
        String confirm = scan.nextLine().trim().toLowerCase();

        if (confirm.equals("yes")) {
            String sqldelete = "DELETE FROM tbl_trader WHERE trader_id = ?";
            con.deleteRecord(sqldelete, traderId);
            System.out.println(" Trader account deleted successfully!");
        } else {
            System.out.println(" Deletion cancelled.");
        }
    }

    // ----------------------------------------------------
    // 1. MANAGE TRADERS
    // ----------------------------------------------------
    public void manageTraders(Scanner scan) {
        int traderChoice;
        do {
            System.out.println("\n--- MANAGE TRADERS ---");
            System.out.println("1. View All Traders");
            System.out.println("2. Update Trader Status");
            System.out.println("3. Delete Trader");
            System.out.println("4. Back to Admin Menu");
            System.out.print("Select option: ");

            while (!scan.hasNextInt()) {
                System.out.println(" Invalid input. Please enter a number.");
                scan.next();
                System.out.print("Select option: ");
            }

            traderChoice = scan.nextInt();
            scan.nextLine();

            switch (traderChoice) {
                case 1:
                    viewTraders();
                    break;
                case 2:
                    updateTraderStatus(scan);
                    break;
                case 3:
                    deleteTrader(scan);
                    break;
                case 4:
                    System.out.println(" Returning to Admin Menu...");
                    break;
                default:
                    System.out.println(" Invalid option! Please choose 1–4.");
            }

        } while (traderChoice != 4);
    }

    // ----------------------------------------------------
    // VIEW ALL ADMINS
    // ----------------------------------------------------
    public void viewAdmins() {
        System.out.println("\n--- VIEW ALL ADMINS ---");
        String query = "SELECT * FROM tbl_admin";

        String[] headers = {"ID", "Username", "Password"};
        String[] columns = {"admin_id", "admin_username", "admin_password"};

        con.viewRecords(query, headers, columns);
    }

    // ----------------------------------------------------
    // ADD ANOTHER ADMIN
    // ----------------------------------------------------
    public void addAnotherAdmin(Scanner scan) {
        System.out.println("\n--- ADD ANOTHER ADMIN ---");

        System.out.print("Enter new admin username: ");
        String username = scan.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println(" Username cannot be empty.");
            return;
        }

        try (Connection conn = config.connectDB();
                PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) AS cnt FROM tbl_admin WHERE admin_username = ?")) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt("cnt") > 0) {
                System.out.println(" Username already exists.");
                return;
            }
        } catch (SQLException e) {
            System.out.println(" Error checking username: " + e.getMessage());
            return;
        }

        System.out.print("Enter password: ");
        String password = scan.nextLine().trim();
        if (password.isEmpty()) {
            System.out.println(" Password cannot be empty.");
            return;
        }

        System.out.print("Confirm add admin? (yes/no): ");
        String confirm = scan.nextLine().trim().toLowerCase();
        if (!confirm.equals("yes")) {
            System.out.println(" Cancelled adding new admin.");
            return;
        }

        String sql = "INSERT INTO tbl_admin (admin_username, admin_password) VALUES (?, ?)";
        try {
            con.addRecord(sql, username, password);
            System.out.println(" New admin added successfully!");
        } catch (Exception e) {
            System.out.println(" Error adding admin: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
    // UPDATE ADMIN ACCOUNT
    // ----------------------------------------------------
    public void updateAdmin(Scanner scan) {
        viewAdmins();
        System.out.print("Enter Admin ID to update: ");
        int adminId = scan.nextInt();
        scan.nextLine();

        System.out.print("Enter new username: ");
        String newUser = scan.nextLine().trim();
        System.out.print("Enter new password: ");
        String newPass = scan.nextLine().trim();

        System.out.print("Confirm update? (yes/no): ");
        String confirm = scan.nextLine().trim().toLowerCase();
        if (!confirm.equals("yes")) {
            System.out.println(" Update cancelled.");
            return;
        }

        String sql = "UPDATE tbl_admin SET admin_username = ?, admin_password = ? WHERE admin_id = ?";
        con.updateRecord(sql, newUser, newPass, adminId);
        System.out.println(" Admin updated successfully!");
    }

    // ----------------------------------------------------
    // DELETE ADMIN ACCOUNT
    // ----------------------------------------------------
    public void deleteAdmin(Scanner scan) {
        viewAdmins();
        System.out.print("Enter Admin ID to delete: ");
        int adminId = scan.nextInt();
        scan.nextLine();

        System.out.print("Are you sure you want to DELETE this admin account? (yes/no): ");
        String confirm = scan.nextLine().trim().toLowerCase();

        if (confirm.equals("yes")) {
            String sqldelete = "DELETE FROM tbl_admin WHERE admin_id = ?";
            con.deleteRecord(sqldelete, adminId);
            System.out.println(" Admin account deleted successfully!");
        } else {
            System.out.println(" Deletion cancelled.");
        }
    }
    
    // ----------------------------------------------------
    // 2. MANAGE ADMINS
    // ----------------------------------------------------
    public void manageAdmins(Scanner scan) {
        int adminChoice;
        do {
            System.out.println("\n--- MANAGE ADMINS ---");
            System.out.println("1. View All Admins");
            System.out.println("2. Add Another Admin");
            System.out.println("3. Update Admin Account");
            System.out.println("4. Delete Admin Account");
            System.out.println("5. Back to Admin Menu");
            System.out.print("Select option: ");

            while (!scan.hasNextInt()) {
                System.out.println(" Invalid input. Please enter a number.");
                scan.next();
                System.out.print("Select option: ");
            }

            adminChoice = scan.nextInt();
            scan.nextLine();

            switch (adminChoice) {
                case 1:
                    viewAdmins();
                    break;
                case 2:
                    addAnotherAdmin(scan);
                    break;
                case 3:
                    updateAdmin(scan);
                    break;
                case 4:
                    deleteAdmin(scan);
                    break;
                case 5:
                    System.out.println(" Returning to Admin Menu...");
                    break;
                default:
                    System.out.println(" Invalid option! Please choose 1–5.");
            }

        } while (adminChoice != 5);
    }
    
    // ----------------------------------------------------
    // VIEW ALL REPORTS
    // ----------------------------------------------------
    public void viewAllReports() {
        System.out.println("\n--- VIEW ALL REPORTS ---");
        String query = "SELECT r.report_id, r.reporter_id, rep.tbl_FullName AS reporter_name, "
                + "r.reported_trader_id, rept.tbl_FullName AS reported_name, "
                + "r.report_reason, r.report_description, r.report_date, r.report_status, "
                + "r.admin_notes, r.resolved_date "
                + "FROM tbl_reports r "
                + "JOIN tbl_trader rep ON r.reporter_id = rep.trader_id "
                + "JOIN tbl_trader rept ON r.reported_trader_id = rept.trader_id "
                + "ORDER BY r.report_date DESC";

        String[] headers = {"Report ID", "Reporter ID", "Reporter", "Reported ID", "Reported Trader",
            "Reason", "Description", "Date", "Status", "Admin Notes", "Resolved Date"};
        String[] columns = {"report_id", "reporter_id", "reporter_name", "reported_trader_id", "reported_name",
            "report_reason", "report_description", "report_date", "report_status",
            "admin_notes", "resolved_date"};

        con.viewRecords(query, headers, columns);
    }

    // ----------------------------------------------------
    // VIEW PENDING REPORTS
    // ----------------------------------------------------
    public void viewPendingReports() {
        System.out.println("\n--- PENDING REPORTS ---");
        String query = "SELECT r.report_id, r.reporter_id, rep.tbl_FullName AS reporter_name, "
                + "r.reported_trader_id, rept.tbl_FullName AS reported_name, "
                + "r.report_reason, r.report_description, r.report_date "
                + "FROM tbl_reports r "
                + "JOIN tbl_trader rep ON r.reporter_id = rep.trader_id "
                + "JOIN tbl_trader rept ON r.reported_trader_id = rept.trader_id "
                + "WHERE r.report_status = 'pending' "
                + "ORDER BY r.report_date DESC";

        String[] headers = {"Report ID", "Reporter", "Reported Trader", "Reason", "Description", "Date"};
        String[] columns = {"report_id", "reporter_name", "reported_name", "report_reason", "report_description", "report_date"};

        con.viewRecords(query, headers, columns);
    }

    // ----------------------------------------------------
    // UPDATE REPORT STATUS
    // ----------------------------------------------------
    public void updateReportStatus(Scanner scan) {
        // Show all reports first so admin can see the IDs
        viewAllReports();

        System.out.print("Enter Report ID to update: ");
        int reportId = scan.nextInt();
        scan.nextLine();

        // Verify report exists
        if (!reportExists(reportId)) {
            System.out.println(" Report ID " + reportId + " does not exist.");
            return;
        }

        System.out.println("Select new status:");
        System.out.println("1. Pending");
        System.out.println("2. Under Review");
        System.out.println("3. Resolved");
        System.out.println("4. Dismissed");
        System.out.print("Enter choice (1-4): ");

        int statusChoice = scan.nextInt();
        scan.nextLine();

        String newStatus;
        switch (statusChoice) {
            case 1:
                newStatus = "pending";
                break;
            case 2:
                newStatus = "under review";
                break;
            case 3:
                newStatus = "resolved";
                break;
            case 4:
                newStatus = "dismissed";
                break;
            default:
                System.out.println(" Invalid choice. Using 'under review'.");
                newStatus = "under review";
        }

        System.out.print("Enter admin notes: ");
        String adminNotes = scan.nextLine();

        String sql;
        if (newStatus.equals("resolved") || newStatus.equals("dismissed")) {
            sql = "UPDATE tbl_reports SET report_status = ?, admin_notes = ?, resolved_date = datetime('now') WHERE report_id = ?";
        } else {
            sql = "UPDATE tbl_reports SET report_status = ?, admin_notes = ? WHERE report_id = ?";
        }

        System.out.print("Are you sure you want to update report #" + reportId + " to '" + newStatus + "'? (yes/no): ");
        String confirm = scan.nextLine().trim().toLowerCase();

        if (confirm.equals("yes")) {
            con.updateRecord(sql, newStatus, adminNotes, reportId);
            System.out.println(" Report status updated successfully!");
        } else {
            System.out.println(" Update cancelled.");
        }
    }

    // ----------------------------------------------------
    // VIEW REPORT DETAILS
    // ----------------------------------------------------
    public void viewReportDetails(Scanner scan) {
        // Show all reports first so admin can see available IDs
        viewAllReports();

        System.out.print("Enter Report ID to view details: ");
        int reportId = scan.nextInt();
        scan.nextLine();

        String query = "SELECT r.report_id, r.reporter_id, rep.tbl_FullName AS reporter_name, "
                + "rep.tbl_Email AS reporter_email, rep.tbl_Contact AS reporter_contact, "
                + "r.reported_trader_id, rept.tbl_FullName AS reported_name, "
                + "rept.tbl_Email AS reported_email, rept.tbl_Contact AS reported_contact, "
                + "r.report_reason, r.report_description, r.report_date, r.report_status, "
                + "r.admin_notes, r.resolved_date "
                + "FROM tbl_reports r "
                + "JOIN tbl_trader rep ON r.reporter_id = rep.trader_id "
                + "JOIN tbl_trader rept ON r.reported_trader_id = rept.trader_id "
                + "WHERE r.report_id = ?";

        try (Connection conn = con.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, reportId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n=== REPORT DETAILS #" + reportId + " ===");
                System.out.println("------------------------------------------------------------");
                System.out.printf("%-20s: %s\n", "Reporter", rs.getString("reporter_name"));
                System.out.printf("%-20s: %s\n", "Reporter Email", rs.getString("reporter_email"));
                System.out.printf("%-20s: %s\n", "Reporter Contact", rs.getString("reporter_contact"));
                System.out.printf("%-20s: %s\n", "Reported Trader", rs.getString("reported_name"));
                System.out.printf("%-20s: %s\n", "Reported Email", rs.getString("reported_email"));
                System.out.printf("%-20s: %s\n", "Reported Contact", rs.getString("reported_contact"));
                System.out.printf("%-20s: %s\n", "Reason", rs.getString("report_reason"));
                System.out.printf("%-20s: %s\n", "Description", rs.getString("report_description"));
                System.out.printf("%-20s: %s\n", "Report Date", rs.getString("report_date"));
                System.out.printf("%-20s: %s\n", "Status", rs.getString("report_status"));
                System.out.printf("%-20s: %s\n", "Admin Notes",
                        rs.getString("admin_notes") != null ? rs.getString("admin_notes") : "N/A");
                System.out.printf("%-20s: %s\n", "Resolved Date",
                        rs.getString("resolved_date") != null ? rs.getString("resolved_date") : "N/A");
                System.out.println("------------------------------------------------------------");
            } else {
                System.out.println(" Report not found with ID: " + reportId);
            }

        } catch (SQLException e) {
            System.out.println(" Error retrieving report details: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
    // HELPER: CHECK IF REPORT EXISTS
    // ----------------------------------------------------
    private boolean reportExists(int reportId) {
        String query = "SELECT COUNT(*) FROM tbl_reports WHERE report_id = ?";
        try (Connection conn = con.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, reportId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.out.println(" Error checking report existence: " + e.getMessage());
            return false;
        }
    }
    
    // ----------------------------------------------------
    // 3. MANAGE REPORTS
    // ----------------------------------------------------
    public void manageReports(Scanner scan) {
        int reportChoice;
        do {
            System.out.println("\n--- MANAGE REPORTS ---");
            System.out.println("1. View All Reports");
            System.out.println("2. View Pending Reports");
            System.out.println("3. Update Report Status");
            System.out.println("4. View Report Details");
            System.out.println("5. Back to Admin Menu");
            System.out.print("Select option: ");

            while (!scan.hasNextInt()) {
                System.out.println(" Invalid input. Please enter a number.");
                scan.next();
                System.out.print("Select option: ");
            }

            reportChoice = scan.nextInt();
            scan.nextLine();

            switch (reportChoice) {
                case 1:
                    viewAllReports();
                    break;
                case 2:
                    viewPendingReports();
                    break;
                case 3:
                    updateReportStatus(scan);
                    break;
                case 4:
                    viewReportDetails(scan);
                    break;
                case 5:
                    System.out.println(" Returning to Admin Menu...");
                    break;
                default:
                    System.out.println(" Invalid option! Please choose 1–5.");
            }

        } while (reportChoice != 5);
    }

    // ----------------------------------------------------
    // ADMIN MENU
    // ----------------------------------------------------
    public void AdminMenu(Scanner scan) {
        int adminChoice;
        do {
            System.out.println("\n========== ADMIN MENU ==========");
            System.out.println("1. Manage Traders");
            System.out.println("2. Manage Admins");
            System.out.println("3. Manage Reports");
            System.out.println("4. Log out");
            System.out.print("Select option: ");

            while (!scan.hasNextInt()) {
                System.out.println(" Invalid input. Please enter a number.");
                scan.next();
                System.out.print("Select option: ");
            }

            adminChoice = scan.nextInt();
            scan.nextLine();

            switch (adminChoice) {
                case 1:
                    manageTraders(scan);
                    break;
                case 2:
                    manageAdmins(scan);
                    break;
                case 3:
                    manageReports(scan);
                    break;
                case 4:
                    System.out.println(" Returning to Main Menu...");
                    break;
                default:
                    System.out.println(" Invalid option! Please choose 1–4.");
            }

        } while (adminChoice != 4);
    }
}
