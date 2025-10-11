package main;

import config.config;
import java.util.Scanner;
import java.sql.*;

public class TraderOption {

    private config con;
    private int traderId;

    public TraderOption(config con, int traderId) {
        this.con = con;
        this.traderId = traderId;
    }

    public void offerItem(Scanner scan) {
        System.out.println("\n--- Offer Item ---");
        System.out.print("Enter Item Name: ");
        String itemName = scan.nextLine();

        System.out.print("Enter Brand: ");
        String brand = scan.nextLine();

        System.out.print("Enter Condition (new/old): ");
        String condition = scan.nextLine();

        System.out.print("Enter Date Bought (YYYY-MM-DD): ");
        String dateBought = scan.nextLine();

        System.out.print("Enter Description: ");
        String description = scan.nextLine();

        String sql = "INSERT INTO tbl_items (trader_id, item_Name, item_Brand, item_Condition, item_Date, item_Description) VALUES (?, ?, ?, ?, ?, ?)";
        con.addRecord(sql, traderId, itemName, brand, condition, dateBought, description);
        System.out.println(" Item offered successfully!");
    }

    public void viewOwnItems() {
        String query = "SELECT * FROM tbl_items WHERE trader_id = " + traderId;
        String[] headers = {"Item ID", "Item Name", "Brand", "Condition", "Date Bought", "Description"};
        String[] columns = {"item_id", "item_Name", "item_Brand", "item_Condition", "item_Date", "item_Description"};
        con.viewRecords(query, headers, columns);
    }

    public void viewOtherItems() {
        String query =
                "SELECT i.item_id, i.item_Name, i.item_Brand, i.item_Condition, " +
                "i.item_Date, i.item_Description, t.tbl_FullName " +
                "FROM tbl_items i " +
                "JOIN tbl_trader t ON i.trader_id = t.trader_id " +
                "WHERE i.trader_id != " + traderId;

        String[] headers = {"Item ID", "Item Name", "Brand", "Condition", "Date Bought", "Description", "Owner"};
        String[] columns = {"item_id", "item_Name", "item_Brand", "item_Condition", "item_Date", "item_Description", "tbl_FullName"};
        con.viewRecords(query, headers, columns);
    }

    public void requestTrade(Scanner scan) {
        viewOtherItems();
        System.out.print("\nEnter Item ID you want to trade for: ");
        int itemId = scan.nextInt();
        scan.nextLine();

        String getTraderSQL = "SELECT trader_id FROM tbl_items WHERE item_id = ?";
        int targetTraderId = -1;

        try (Connection conn = con.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(getTraderSQL)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                targetTraderId = rs.getInt("trader_id");
            }
        } catch (Exception e) {
            System.out.println("⚠ Error finding target trader: " + e.getMessage());
            return;
        }

        if (targetTraderId == -1) {
            System.out.println("❌ Item not found.");
            return;
        }

        String sqlrequest = "INSERT INTO tbl_trades (offer_trader_id, target_trader_id, item_id, status, date_requested) VALUES (?, ?, ?, ?, datetime('now'))";
        con.addRecord(sqlrequest, traderId, targetTraderId, itemId, "pending");
        System.out.println("✅ Trade request sent successfully!");
    }

    public void viewTradeRequests() {
        String query =
                "SELECT tr.trade_id, t.tbl_FullName AS Offerer, i.item_Name, tr.status " +
                "FROM tbl_trades tr " +
                "JOIN tbl_trader t ON tr.offer_trader_id = t.trader_id " +
                "JOIN tbl_items i ON tr.item_id = i.item_id " +
                "WHERE tr.target_trader_id = " + traderId;

        String[] headers = {"Trade ID", "Offerer", "Item Name", "Status"};
        String[] columns = {"trade_id", "Offerer", "item_Name", "status"};
        con.viewRecords(query, headers, columns);
    }

    public void respondTrade(Scanner scan) {
        viewTradeRequests();
        System.out.print("Enter Trade ID to respond: ");
        int tradeId = scan.nextInt();
        scan.nextLine();

        System.out.print("Accept or Decline? (approved/declined): ");
        String newStatus = scan.nextLine();

        String sqlrespond = "UPDATE tbl_trades SET status = ? WHERE trade_id = ?";
        con.updateRecord(sqlrespond, newStatus, tradeId);
        System.out.println(" Trade " + newStatus + "!");
    }

    public void showTraderMenu(Scanner scan) {
        int choice;
        do {
            System.out.println("\n========== TRADER MENU ==========");
            System.out.println("1. Offer Item");
            System.out.println("2. View My Items");
            System.out.println("3. View Other Traders' Items");
            System.out.println("4. Request Trade");
            System.out.println("5. View Trade Requests");
            System.out.println("6. Respond to Trade");
            System.out.println("7. Logout");
            System.out.print("Select option: ");

            while (!scan.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scan.next();
                System.out.print("Select option: ");
            }

            choice = scan.nextInt();
            scan.nextLine();

            switch (choice) {
                case 1:
                    offerItem(scan);
                    break;
                case 2:
                    viewOwnItems();
                    break;
                case 3:
                    viewOtherItems();
                    break;
                case 4:
                    requestTrade(scan);
                    break;
                case 5:
                    viewTradeRequests();
                    break;
                case 6:
                    respondTrade(scan);
                    break;
                case 7:
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid option!");
                    break;
            }

        } while (choice != 7);
    }
}
