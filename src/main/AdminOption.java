package main;

import config.config;
import java.util.Scanner;

public class AdminOption {
    private config con;

    public AdminOption(config con) {
        this.con = con;
    }

    public void viewTraders() {
        String query = "SELECT * FROM tbl_trader";
        String[] headers = {"ID", "Username", "Full Name", "Email", "Contact", "Location", "Status"};
        String[] columns = {"trader_id", "tbl_Username", "tbl_FullName", "tbl_Email", "tbl_Contact", "tbl_Location", "tbl_Status"};
        con.viewRecords(query, headers, columns);
    }

    public void updateTraderStatus(Scanner scan) {
        viewTraders();
        System.out.print("Enter Trader ID to update status: ");
        int traderId = scan.nextInt();
        scan.nextLine();

        System.out.print("Enter new status (pending / approved / declined): ");
        String newStatus = scan.nextLine();

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

    public void AdminMenu(Scanner scan) {
        int adminChoice;
        do {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. View Traders");
            System.out.println("2. Update Trader Status (pending / approved / declined)");
            System.out.println("3. Delete Trader");
            System.out.println("4. Back to Main Menu");
            System.out.print("Select option: ");
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
                    System.out.println("Returning to Main Menu...");
                    break;
                default:
                    System.out.println("Invalid option! Please choose 1-4.");
                    break;
            }
        } while (adminChoice != 4);
    }
}
