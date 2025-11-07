package Admin;

import config.config;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Announcement {

    private config con;

    public Announcement(config con) {
        this.con = con;
    }

    // ----------------------------------------------------
// CREATE NEW ANNOUNCEMENT
// ----------------------------------------------------
    public void createAnnouncement(Scanner scan, int adminId) {
        System.out.println("\n--- CREATE NEW ANNOUNCEMENT ---");

        String title;
        while (true) {
            System.out.print("Enter Announcement Title (or 0 to cancel): ");
            title = scan.nextLine().trim();

            if (title.equals("0")) {
                System.out.println("Announcement creation cancelled.");
                return;
            }

            if (!title.isEmpty()) {
                break;
            } else {
                System.out.println(" Title cannot be empty! Please enter a title.");
            }
        }

        System.out.println("Enter Announcement Message (type 'END' on a new line to finish, or type '0' to cancel):");
        StringBuilder messageBuilder = new StringBuilder();
        String line;

        while (true) {
            line = scan.nextLine();

            if (line.equals("0")) {
                System.out.println("Announcement creation cancelled.");
                return;
            }

            if (line.equalsIgnoreCase("END")) {
                break;
            }

            messageBuilder.append(line).append("\n");
        }

        String message = messageBuilder.toString().trim();
        if (message.isEmpty()) {
            System.out.println(" Message cannot be empty! Announcement creation cancelled.");
            return;
        }

        String publish;
        while (true) {
            System.out.print("Publish this announcement now? (yes/no/0 to cancel): ");
            publish = scan.nextLine().trim().toLowerCase();

            if (publish.equals("0")) {
                System.out.println("Announcement creation cancelled.");
                return;
            }

            if (publish.equals("yes") || publish.equals("no")) {
                break;
            } else {
                System.out.println(" Please enter 'yes', 'no', or '0' to cancel.");
            }
        }

        boolean isActive = publish.equals("yes");

        System.out.println("\n--- CONFIRM ANNOUNCEMENT ---");
        System.out.println("Title: " + title);
        System.out.println("Message: " + message);
        System.out.println("Status: " + (isActive ? "PUBLISHED" : "DRAFT"));
        System.out.print("Create this announcement? (yes/no): ");
        String confirm = scan.nextLine().trim().toLowerCase();

        if (confirm.equals("yes")) {
            String sql = "INSERT INTO tbl_announcement (admin_id, title, message, is_active) VALUES (?, ?, ?, ?)";
            int result = con.addRecordAndReturnId(sql, adminId, title, message, isActive ? 1 : 0);

            if (result > 0) {
                System.out.println(" Announcement " + (isActive ? "published" : "saved as draft") + " successfully!");
            } else {
                System.out.println(" Failed to create announcement.");
            }
        } else {
            System.out.println(" Announcement creation cancelled.");
        }
    }

    // ----------------------------------------------------
    // VIEW ALL ANNOUNCEMENTS
    // ----------------------------------------------------
    public void viewAllAnnouncements() {
        System.out.println("\n--- ALL ANNOUNCEMENTS ---");

        String query = "SELECT a.announcement_id, a.title, a.message, a.announcement_date, "
                + "a.is_active, ad.admin_username "
                + "FROM tbl_announcement a "
                + "JOIN tbl_admin ad ON a.admin_id = ad.admin_id "
                + "ORDER BY a.announcement_date DESC";

        String[] headers = {"ID", "Title", "Message", "Date", "Status", "Posted By"};
        String[] columns = {"announcement_id", "title", "message", "announcement_date", "is_active", "admin_username"};

        con.viewRecords(query, headers, columns);
    }

    // ----------------------------------------------------
    // VIEW ACTIVE ANNOUNCEMENTS
    // ----------------------------------------------------
    public void viewActiveAnnouncements() {
        System.out.println("\n--- ACTIVE ANNOUNCEMENTS ---");

        String query = "SELECT a.announcement_id, a.title, a.message, a.announcement_date, ad.admin_username "
                + "FROM tbl_announcement a "
                + "JOIN tbl_admin ad ON a.admin_id = ad.admin_id "
                + "WHERE a.is_active = 1 "
                + "ORDER BY a.announcement_date DESC";

        try (Connection conn = con.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            boolean hasAnnouncements = false;
            while (rs.next()) {
                hasAnnouncements = true;
                System.out.println("\n═══════════════════════════════════════════════════");
                System.out.println("TITLE: " + rs.getString("title"));
                System.out.println("DATE: " + rs.getString("announcement_date"));
                System.out.println("POSTED BY: " + rs.getString("admin_username"));
                System.out.println("MESSAGE:");
                System.out.println(rs.getString("message"));
                System.out.println("═══════════════════════════════════════════════════");
            }

            if (!hasAnnouncements) {
                System.out.println("No active announcements at the moment.");
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving announcements: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
// UPDATE ANNOUNCEMENT STATUS
// ----------------------------------------------------
    public void updateAnnouncementStatus(Scanner scan) {
        viewAllAnnouncements();

        int announcementId;
        while (true) {
            System.out.print("Enter Announcement ID to update (0 to cancel): ");
            if (scan.hasNextInt()) {
                announcementId = scan.nextInt();
                scan.nextLine();

                if (announcementId == 0) {
                    System.out.println("Status update cancelled.");
                    return;
                }

                if (announcementExists(announcementId)) {
                    break;
                } else {
                    System.out.println("Announcement ID " + announcementId + " does not exist. Please try again.");
                }
            } else {
                System.out.println("Invalid input! Please enter a valid number.");
                scan.nextLine();
            }
        }

        int choice;
        while (true) {
            System.out.println("\nSelect new status:");
            System.out.println("1. Activate");
            System.out.println("2. Deactivate");
            System.out.print("Enter choice (1-2, or 0 to cancel): ");

            if (scan.hasNextInt()) {
                choice = scan.nextInt();
                scan.nextLine();

                if (choice == 0) {
                    System.out.println("Status update cancelled.");
                    return;
                }

                if (choice == 1 || choice == 2) {
                    break;
                } else {
                    System.out.println("Invalid choice! Please enter 1, 2, or 0 to cancel.");
                }
            } else {
                System.out.println("Invalid input! Please enter a number.");
                scan.nextLine();
            }
        }

        boolean newStatus = (choice == 1);
        String statusText = newStatus ? "ACTIVATED" : "DEACTIVATED";

        System.out.print("Are you sure you want to " + (newStatus ? "activate" : "deactivate")
                + " announcement #" + announcementId + "? (yes/no): ");
        String confirm = scan.nextLine().trim().toLowerCase();

        if (confirm.equals("yes")) {
            String sql = "UPDATE tbl_announcement SET is_active = ? WHERE announcement_id = ?";
            con.updateRecord(sql, newStatus ? 1 : 0, announcementId);
            System.out.println("Announcement " + (newStatus ? "activated" : "deactivated") + " successfully!");
        } else {
            System.out.println("Status update cancelled.");
        }
    }

    // ----------------------------------------------------
// DELETE ANNOUNCEMENT
// ----------------------------------------------------
    public void deleteAnnouncement(Scanner scan) {
        viewAllAnnouncements();

        int announcementId;
        while (true) {
            System.out.print("Enter Announcement ID to delete (0 to cancel): ");
            if (scan.hasNextInt()) {
                announcementId = scan.nextInt();
                scan.nextLine();

                if (announcementId == 0) {
                    System.out.println("Deletion cancelled.");
                    return;
                }

                if (announcementExists(announcementId)) {
                    break;
                } else {
                    System.out.println("Announcement ID " + announcementId + " does not exist. Please try again.");
                }
            } else {
                System.out.println("Invalid input! Please enter a valid number.");
                scan.nextLine();
            }
        }

        System.out.println("\n--- ANNOUNCEMENT TO DELETE ---");
        showAnnouncementDetails(announcementId);

        System.out.print("ARE YOU SURE you want to PERMANENTLY DELETE this announcement? (yes/no): ");
        String confirm = scan.nextLine().trim().toLowerCase();

        if (confirm.equals("yes")) {
            System.out.print("THIS ACTION CANNOT BE UNDONE! Type 'DELETE' to confirm: ");
            String finalConfirm = scan.nextLine().trim();

            if (finalConfirm.equalsIgnoreCase("DELETE")) {
                String sql = "DELETE FROM tbl_announcement WHERE announcement_id = ?";
                con.deleteRecord(sql, announcementId);
                System.out.println("Announcement deleted successfully!");
            } else {
                System.out.println("Deletion cancelled. Confirmation text did not match.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

// ----------------------------------------------------
// HELPER METHOD TO SHOW ANNOUNCEMENT DETAILS
// ----------------------------------------------------
    private void showAnnouncementDetails(int announcementId) {
        String query = "SELECT title, message, announcement_date, is_active FROM tbl_announcement WHERE announcement_id = ?";
        try (Connection conn = con.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, announcementId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Title: " + rs.getString("title"));
                System.out.println("Date: " + rs.getString("announcement_date"));
                System.out.println("Status: " + (rs.getInt("is_active") == 1 ? "ACTIVE" : "INACTIVE"));
                System.out.println("Message: " + rs.getString("message").substring(0, Math.min(100, rs.getString("message").length())) + "...");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving announcement details: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
// EDIT ANNOUNCEMENT
// ----------------------------------------------------
    public void editAnnouncement(Scanner scan) {
        viewAllAnnouncements();

        int announcementId;
        while (true) {
            System.out.print("Enter Announcement ID to edit (0 to cancel): ");
            if (scan.hasNextInt()) {
                announcementId = scan.nextInt();
                scan.nextLine();

                if (announcementId == 0) {
                    System.out.println("Edit cancelled.");
                    return;
                }

                if (announcementExists(announcementId)) {
                    break;
                } else {
                    System.out.println(" Announcement ID " + announcementId + " does not exist. Please try again.");
                }
            } else {
                System.out.println("Invalid input! Please enter a valid number.");
                scan.nextLine();
            }
        }

        String getSQL = "SELECT title, message FROM tbl_announcement WHERE announcement_id = ?";
        try (Connection conn = con.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(getSQL)) {

            pstmt.setInt(1, announcementId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String currentTitle = rs.getString("title");
                String currentMessage = rs.getString("message");

                System.out.println("\nCurrent Title: " + currentTitle);
                System.out.print("Enter new title (or press Enter to keep current, or type '0' to cancel): ");
                String newTitle = scan.nextLine();

                if (newTitle.equals("0")) {
                    System.out.println("Edit cancelled.");
                    return;
                }
                if (newTitle.isEmpty()) {
                    newTitle = currentTitle;
                }

                System.out.println("\nCurrent Message:");
                System.out.println(currentMessage);
                System.out.println("Enter new message (type 'END' on a new line to finish, or press Enter to keep current, or type '0' to cancel):");
                StringBuilder messageBuilder = new StringBuilder();
                String line = scan.nextLine();

                if (line.equals("0")) {
                    System.out.println("Edit cancelled.");
                    return;
                }

                if (line.isEmpty()) {
                    System.out.print("Confirm update announcement #" + announcementId + "? (yes/no): ");
                    String confirm = scan.nextLine().trim().toLowerCase();

                    if (confirm.equals("yes")) {
                        String updateSQL = "UPDATE tbl_announcement SET title = ? WHERE announcement_id = ?";
                        con.updateRecord(updateSQL, newTitle, announcementId);
                        System.out.println("Announcement updated successfully!");
                    } else {
                        System.out.println("Update cancelled.");
                    }
                } else {
                    messageBuilder.append(line).append("\n");
                    while (true) {
                        line = scan.nextLine();

                        if (line.equalsIgnoreCase("END")) {
                            break;
                        }

                        if (line.equals("0")) {
                            System.out.println("Edit cancelled.");
                            return;
                        }

                        messageBuilder.append(line).append("\n");
                    }

                    String newMessage = messageBuilder.toString().trim();
                    if (newMessage.isEmpty()) {
                        System.out.println("Message cannot be empty! Edit cancelled.");
                        return;
                    }

                    System.out.println("\n--- CONFIRM CHANGES ---");
                    System.out.println("New Title: " + newTitle);
                    System.out.println("New Message: " + newMessage);
                    System.out.print("Update this announcement? (yes/no): ");
                    String confirm = scan.nextLine().trim().toLowerCase();

                    if (confirm.equals("yes")) {
                        String updateSQL = "UPDATE tbl_announcement SET title = ?, message = ? WHERE announcement_id = ?";
                        con.updateRecord(updateSQL, newTitle, newMessage, announcementId);
                        System.out.println(" Announcement updated successfully!");
                    } else {
                        System.out.println(" Update cancelled.");
                    }
                }
            } else {
                System.out.println(" Announcement not found!");
            }

        } catch (SQLException e) {
            System.out.println("Error editing announcement: " + e.getMessage());
        }
    }

// ----------------------------------------------------
// HELPER METHOD TO CHECK IF ANNOUNCEMENT EXISTS
// ----------------------------------------------------
    private boolean announcementExists(int announcementId) {
        String sql = "SELECT announcement_id FROM tbl_announcement WHERE announcement_id = ?";
        List<Map<String, Object>> result = con.fetchRecords(sql, announcementId);
        return result != null && !result.isEmpty();
    }
}
