package main;

import config.config;
import java.sql.*;
import java.util.Scanner;

public class AdminOption {

    private config con;

    public AdminOption(config con) {
        this.con = con;
    }

    // ----------------------------------------------------
    // 1. VIEW TRADERS
    // ----------------------------------------------------
    public void viewTraders() {
        String query = "SELECT * FROM tbl_trader";
        String[] headers = {"ID", "Username", "Full Name", "Email", "Contact", "Location", "Status"};
        String[] columns = {"trader_id", "tbl_Username", "tbl_FullName", "tbl_Email", "tbl_Contact", "tbl_Location", "tbl_Status"};
        con.viewRecords(query, headers, columns);
    }

    // ----------------------------------------------------
    // 2. UPDATE TRADER STATUS
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
    // 3. DELETE TRADER
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
    // 4. ADD ANOTHER ADMIN
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
    // 5. VIEW ALL ADMINS
    // ----------------------------------------------------
    public void viewAdmins() {
        System.out.println("\n--- VIEW ALL ADMINS ---");
        String query = "SELECT * FROM tbl_admin";

        String[] headers = {"ID", "Username", "Password"};
        String[] columns = {"admin_id", "admin_username", "admin_password"};

        con.viewRecords(query, headers, columns);
    }

    // ----------------------------------------------------
    // 6. UPDATE ADMIN ACCOUNT
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
    // 7. DELETE ADMIN ACCOUNT
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
    // ADMIN MENU
    // ----------------------------------------------------
    public void AdminMenu(Scanner scan) {
        int adminChoice;
        do {
            System.out.println("\n========== ADMIN MENU ==========");
            System.out.println("1. View Traders");
            System.out.println("2. Update Trader Status");
            System.out.println("3. Delete Trader");
            System.out.println("4. Add Another Admin");
            System.out.println("5. View All Admins");
            System.out.println("6. Update Admin");
            System.out.println("7. Delete Admin");
            System.out.println("8. Back to Main Menu");
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
                    viewTraders();
                    break;
                case 2:
                    updateTraderStatus(scan);
                    break;
                case 3:
                    deleteTrader(scan);
                    break;
                case 4:
                    addAnotherAdmin(scan);
                    break;
                case 5:
                    viewAdmins();
                case 6:

                    updateAdmin(scan);
                case 7:

                    deleteAdmin(scan);
                case 8:
                    System.out.println(" Returning to Main Menu...");
                default:
                    System.out.println(" Invalid option! Please choose 1–8.");
            }

        } while (adminChoice != 8);
    }
}
