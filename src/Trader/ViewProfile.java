package Trader;

import config.config;
import java.sql.*;
import java.util.Scanner;

public class ViewProfile {

    private config con;
    private int traderId;

    public ViewProfile(config con, int traderId) {
        this.con = con;
        this.traderId = traderId;
    }

    // ----------------------------------------------------
    // EDIT PROFILE INFORMATION
    // ----------------------------------------------------
    public void editProfileInfo(Scanner scan) {
        System.out.println("\n--- EDIT PROFILE INFORMATION ---");

        // First display current info
        String currentQuery = "SELECT tbl_FullName, tbl_Email, tbl_Contact, tbl_Location FROM tbl_trader WHERE trader_id = ?";

        try (Connection conn = con.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(currentQuery)) {

            pstmt.setInt(1, traderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String currentName = rs.getString("tbl_FullName");
                String currentEmail = rs.getString("tbl_Email");
                String currentContact = rs.getString("tbl_Contact");
                String currentLocation = rs.getString("tbl_Location");

                System.out.println("Current Information:");
                System.out.println("Full Name: " + currentName);
                System.out.println("Email: " + currentEmail);
                System.out.println("Contact: " + currentContact);
                System.out.println("Location: " + currentLocation);

                System.out.println("\nLeave field blank to keep current value.");

                System.out.print("Enter New Full Name [" + currentName + "]: ");
                String newName = scan.nextLine();
                if (newName.isEmpty()) {
                    newName = currentName;
                }

                System.out.print("Enter New Email [" + currentEmail + "]: ");
                String newEmail = scan.nextLine();
                if (newEmail.isEmpty()) {
                    newEmail = currentEmail;
                }

                System.out.print("Enter New Contact [" + currentContact + "]: ");
                String newContact = scan.nextLine();
                if (newContact.isEmpty()) {
                    newContact = currentContact;
                }

                System.out.print("Enter New Location [" + currentLocation + "]: ");
                String newLocation = scan.nextLine();
                if (newLocation.isEmpty()) {
                    newLocation = currentLocation;
                }

                System.out.println("\nPlease confirm the changes:");
                System.out.println("Full Name: " + newName);
                System.out.println("Email: " + newEmail);
                System.out.println("Contact: " + newContact);
                System.out.println("Location: " + newLocation);
                System.out.print("Proceed with update? (yes/no): ");
                String confirm = scan.nextLine().trim().toLowerCase();

                if (confirm.equals("yes")) {
                    String updateSQL = "UPDATE tbl_trader SET tbl_FullName = ?, tbl_Email = ?, tbl_Contact = ?, tbl_Location = ? WHERE trader_id = ?";
                    con.updateRecord(updateSQL, newName, newEmail, newContact, newLocation, traderId);
                    System.out.println(" Profile updated successfully!");
                } else {
                    System.out.println(" Profile update cancelled.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error updating profile: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
    // CHANGE PASSWORD
    // ----------------------------------------------------
    public void changePassword(Scanner scan) {
        System.out.println("\n--- CHANGE PASSWORD ---");

        System.out.print("Enter Current Password: ");
        String currentPassword = scan.nextLine();

        System.out.print("Enter New Password: ");
        String newPassword = scan.nextLine();

        System.out.print("Confirm New Password: ");
        String confirmPassword = scan.nextLine();

        // Validate inputs
        if (newPassword.isEmpty()) {
            System.out.println("Password cannot be empty!");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("New passwords do not match!");
            return;
        }

        try (Connection conn = con.connectDB()) {
            // Verify current password
            String verifySQL = "SELECT tbl_Password FROM tbl_trader WHERE trader_id = ?";
            PreparedStatement verifyStmt = conn.prepareStatement(verifySQL);
            verifyStmt.setInt(1, traderId);
            ResultSet rs = verifyStmt.executeQuery();

            if (rs.next()) {
                String currentHashedPassword = rs.getString("tbl_Password");
                String inputHashedPassword = config.hashPassword(currentPassword);

                if (!currentHashedPassword.equals(inputHashedPassword)) {
                    System.out.println("Current password is incorrect!");
                    return;
                }

                System.out.print("Are you sure you want to change your password? (yes/no): ");
                String confirm = scan.nextLine().trim().toLowerCase();

                if (confirm.equals("yes")) {
                    String newHashedPassword = config.hashPassword(newPassword);
                    String updateSQL = "UPDATE tbl_trader SET tbl_Password = ? WHERE trader_id = ?";
                    con.updateRecord(updateSQL, newHashedPassword, traderId);
                    System.out.println(" Password changed successfully!");
                } else {
                    System.out.println(" Password change cancelled.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error changing password: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
    // DISPLAY COMPLETE PROFILE INFORMATION
    // ----------------------------------------------------
    public void displayCompleteProfile() {
        System.out.println("\n--- COMPLETE PROFILE INFORMATION ---");

        String query = "SELECT tbl_Username, tbl_FullName, tbl_Email, tbl_Contact, tbl_Location, tbl_Status, "
                + "(SELECT COUNT(*) FROM tbl_trade_history WHERE offer_trader_id = ? OR target_trader_id = ?) as completed_trades, "
                + "(SELECT COUNT(*) FROM tbl_reports WHERE reporter_id = ?) as reports_filed "
                + "FROM tbl_trader WHERE trader_id = ?";

        try (Connection conn = con.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, traderId);
            pstmt.setInt(2, traderId);
            pstmt.setInt(3, traderId);
            pstmt.setInt(4, traderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("==========================================");
                System.out.println("           PROFILE INFORMATION");
                System.out.println("==========================================");
                System.out.printf("%-15s: %s\n", "Username", rs.getString("tbl_Username"));
                System.out.printf("%-15s: %s\n", "Full Name", rs.getString("tbl_FullName"));
                System.out.printf("%-15s: %s\n", "Email", rs.getString("tbl_Email"));
                System.out.printf("%-15s: %s\n", "Contact", rs.getString("tbl_Contact"));
                System.out.printf("%-15s: %s\n", "Location", rs.getString("tbl_Location"));
                System.out.printf("%-15s: %s\n", "Status", rs.getString("tbl_Status"));
                System.out.printf("%-15s: %d\n", "Completed Trades", rs.getInt("completed_trades"));
                System.out.printf("%-15s: %d\n", "Reports Filed", rs.getInt("reports_filed"));
                System.out.println("==========================================");
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving profile information: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
// DISPLAY TRADE STATISTICS
// ----------------------------------------------------
    public void displayTradeStatistics() {
        System.out.println("\n--- TRADE STATISTICS ---");

        String query = "SELECT "
                + "COUNT(*) as total_trades, "
                + "COUNT(CASE WHEN trade_status = 'completed' THEN 1 END) as completed_trades, "
                + "COUNT(CASE WHEN trade_status = 'negotiating' THEN 1 END) as negotiating_trades, "
                + "COUNT(CASE WHEN trade_status = 'arrangements_confirmed' THEN 1 END) as arrangements_trades, "
                + "COUNT(CASE WHEN trade_status = 'pending' THEN 1 END) as pending_trades, "
                + "COUNT(CASE WHEN trade_status = 'declined' THEN 1 END) as declined_trades "
                + "FROM ("
                + "    SELECT trade_id, trade_status FROM tbl_trade WHERE offer_trader_id = ? OR target_trader_id = ? "
                + "    UNION ALL "
                + "    SELECT trade_id, trade_status FROM tbl_trade_history WHERE offer_trader_id = ? OR target_trader_id = ? "
                + ") all_trades";

        try (Connection conn = con.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, traderId);
            pstmt.setInt(2, traderId);
            pstmt.setInt(3, traderId);
            pstmt.setInt(4, traderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total_trades");
                int completed = rs.getInt("completed_trades");
                int negotiating = rs.getInt("negotiating_trades");
                int arrangements = rs.getInt("arrangements_trades");
                int pending = rs.getInt("pending_trades");
                int declined = rs.getInt("declined_trades");

                String successRate = total > 0 ? (completed * 100 / total) + "%" : "0%";

                System.out.println("==========================================");
                System.out.println("           TRADE STATISTICS");
                System.out.println("==========================================");
                System.out.printf("%-25s: %d\n", "Total Trades", total);
                System.out.printf("%-25s: %d\n", "Completed Trades", completed);
                System.out.printf("%-25s: %s\n", "Success Rate", successRate); // Changed %d to %s
                System.out.println("------------------------------------------");
                System.out.printf("%-25s: %d\n", "Pending Requests", pending);
                System.out.printf("%-25s: %d\n", "Negotiating Trades", negotiating);
                System.out.printf("%-25s: %d\n", "Arrangements Confirmed", arrangements);
                System.out.printf("%-25s: %d\n", "Declined Trades", declined);
                System.out.println("==========================================");
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving trade statistics: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
    // DISPLAY REPORT HISTORY
    // ----------------------------------------------------
    public void displayReportHistory() {
        System.out.println("\n--- REPORT HISTORY ---");

        String query = "SELECT r.report_id, t.tbl_FullName AS reported_trader, "
                + "r.report_reason, r.report_description, r.report_date, r.report_status, r.admin_notes "
                + "FROM tbl_reports r "
                + "JOIN tbl_trader t ON r.reported_trader_id = t.trader_id "
                + "WHERE r.reporter_id = ? "
                + "ORDER BY r.report_date DESC";

        try (Connection conn = con.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, traderId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("===============================================================================================================");
            System.out.printf("| %-5s | %-20s | %-20s | %-10s | %-20s | %-30s |\n",
                    "ID", "Reported Trader", "Reason", "Status", "Date", "Admin Notes");
            System.out.println("===============================================================================================================");

            boolean hasReports = false;
            while (rs.next()) {
                hasReports = true;
                String adminNotes = rs.getString("admin_notes") != null
                        ? (rs.getString("admin_notes").length() > 25 ? rs.getString("admin_notes").substring(0, 25) + "..." : rs.getString("admin_notes"))
                        : "No notes";

                System.out.printf("| %-5d | %-20s | %-20s | %-10s | %-20s | %-30s |\n",
                        rs.getInt("report_id"),
                        rs.getString("reported_trader"),
                        rs.getString("report_reason"),
                        rs.getString("report_status"),
                        rs.getString("report_date"),
                        adminNotes);
            }

            if (!hasReports) {
                System.out.println("| No reports filed yet.                                                                                             |");
            }
            System.out.println("===============================================================================================================");

            String summaryQuery = "SELECT COUNT(*) as total_reports, "
                    + "COUNT(CASE WHEN report_status = 'resolved' THEN 1 END) as resolved_reports, "
                    + "COUNT(CASE WHEN report_status = 'pending' THEN 1 END) as pending_reports "
                    + "FROM tbl_reports WHERE reporter_id = ?";

            PreparedStatement summaryStmt = conn.prepareStatement(summaryQuery);
            summaryStmt.setInt(1, traderId);
            ResultSet summaryRs = summaryStmt.executeQuery();

            if (summaryRs.next()) {
                System.out.println("\nREPORT SUMMARY:");
                System.out.println("Total Reports Filed: " + summaryRs.getInt("total_reports"));
                System.out.println("Resolved Reports: " + summaryRs.getInt("resolved_reports"));
                System.out.println("Pending Reports: " + summaryRs.getInt("pending_reports"));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving report history: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
    // DISPLAY ALL INFORMATION (PROFILE + STATS + REPORTS)
    // ----------------------------------------------------
    public void displayAllInformation() {
        System.out.println("\n--- COMPLETE PROFILE OVERVIEW ---");
        System.out.println("==================================");

        displayCompleteProfile();

        displayTradeStatistics();

        displayReportHistory();

        System.out.println("\n--- END OF PROFILE OVERVIEW ---");
    }
}
