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

    // ----------------------------------------------------
    // 1. OFFER ITEM
    // ----------------------------------------------------
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

        String sql = "INSERT INTO tbl_items (item_Name, item_Brand, item_Condition, item_Date, item_Description, trader_id) VALUES (?, ?, ?, ?, ?, ?)";
        int newId = con.addRecordAndReturnId(sql, itemName, brand, condition, dateBought, description, traderId);

        if (newId > 0) {
            System.out.println("✅ Item offered successfully! (Item ID: " + newId + ")");
        } else {
            System.out.println("⚠ Failed to add item.");
        }
    }

    // ----------------------------------------------------
    // 2. VIEW MY ITEMS
    // ----------------------------------------------------
    public void ViewMyItems() {
        String query = "SELECT * FROM tbl_items WHERE trader_id = " + traderId;
        String[] headers = {"Item ID", "Item Name", "Brand", "Condition", "Date Bought", "Description"};
        String[] columns = {"items_id", "item_Name", "item_Brand", "item_Condition", "item_Date", "item_Description"};
        con.viewRecords(query, headers, columns);
    }

    // ----------------------------------------------------
    // 3. UPDATE MY ITEM
    // ----------------------------------------------------
    public void UpdateMyItem(Scanner scan) {
        ViewMyItems();
        System.out.print("Enter Item ID to Update: ");
        int itemId = scan.nextInt();
        scan.nextLine();

        System.out.print("Enter New Item Name: ");
        String newName = scan.nextLine();

        System.out.print("Enter New Brand: ");
        String newBrand = scan.nextLine();

        System.out.print("Enter New Condition: ");
        String newCondition = scan.nextLine();

        System.out.print("Enter New Date Bought: ");
        String newDate = scan.nextLine();

        System.out.print("Enter New Description: ");
        String newDesc = scan.nextLine();

        String sqlUpdate = "UPDATE tbl_items SET item_Name = ?, item_Brand = ?, item_Condition = ?, item_Date = ?, item_Description = ? WHERE items_id = ? AND trader_id = ?";
        con.updateRecord(sqlUpdate, newName, newBrand, newCondition, newDate, newDesc, itemId, traderId);
        System.out.println("✅ Item updated successfully!");
    }

    // ----------------------------------------------------
    // 4. DELETE MY ITEM
    // ----------------------------------------------------
    public void DeleteMyItem(Scanner scan) {
        ViewMyItems();
        System.out.print("Enter Item ID to Delete: ");
        int itemId = scan.nextInt();
        scan.nextLine();

        String sqlDelete = "DELETE FROM tbl_items WHERE items_id = ? AND trader_id = ?";
        con.deleteRecord(sqlDelete, itemId, traderId);
        System.out.println("✅ Item deleted successfully!");
    }

    // ----------------------------------------------------
    // 5. VIEW OTHER TRADERS' ITEMS (FULL DETAILS)
    // ----------------------------------------------------
    public void viewOtherItems() {
        System.out.println("\n--- OTHER TRADERS' ITEMS ---");

        String query = "SELECT i.items_id, i.item_Name, i.item_Brand, i.item_Condition, "
                + "i.item_Date, i.item_Description, "
                + "t.tbl_FullName AS trader_name, t.tbl_Location, t.tbl_Contact "
                + "FROM tbl_items i "
                + "JOIN tbl_trader t ON i.trader_id = t.trader_id "
                + "WHERE i.trader_id != ?";

        try (Connection conn = config.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, traderId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-5s | %-15s | %-10s | %-10s | %-12s | %-20s | %-15s | %-15s | %-15s |\n",
                    "ID", "Item Name", "Brand", "Condition", "Date", "Description", "Trader", "Location", "Contact");
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("| %-5d | %-15s | %-10s | %-10s | %-12s | %-20s | %-15s | %-15s | %-15s |\n",
                        rs.getInt("items_id"),
                        rs.getString("item_Name"),
                        rs.getString("item_Brand"),
                        rs.getString("item_Condition"),
                        rs.getString("item_Date"),
                        rs.getString("item_Description"),
                        rs.getString("trader_name"),
                        rs.getString("tbl_Location"),
                        rs.getString("tbl_Contact"));
            }
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("⚠ Error retrieving other traders' items: " + e.getMessage());
        }
    }

// ----------------------------------------------------
// 6. REQUEST TRADE
// ----------------------------------------------------
    public void requestTrade(Scanner scan) {
        System.out.println("\n--- REQUEST TRADE ---");

        viewOtherItems();

        System.out.print("\nEnter the Item ID you want to trade for: ");
        int targetItemId = scan.nextInt();
        scan.nextLine();

        String getTraderSQL = "SELECT trader_id, item_Name FROM tbl_items WHERE items_id = ?";
        int targetTraderId = -1;
        String targetItemName = "";

        try (Connection conn = config.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(getTraderSQL)) {

            pstmt.setInt(1, targetItemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                targetTraderId = rs.getInt("trader_id");
                targetItemName = rs.getString("item_Name");
            } else {
                System.out.println(" No item found with that ID.");
                return;
            }

        } catch (SQLException e) {
            System.out.println("⚠ Error retrieving item owner: " + e.getMessage());
            return;
        }

        if (targetTraderId == traderId) {
            System.out.println(" You cannot trade with yourself.");
            return;
        }

        System.out.println("\nSelect one of your own items to offer in trade:");
        ViewMyItems();
        System.out.print("Enter your Item ID to offer: ");
        int myItemId = scan.nextInt();
        scan.nextLine();

        System.out.println("\nConfirm Trade Request:");
        System.out.println("You are offering your Item ID: " + myItemId
                + " for Item ID: " + targetItemId + " (" + targetItemName + ")");
        System.out.print("Proceed with trade? (yes/no): ");
        String confirm = scan.nextLine().trim().toLowerCase();

        if (!confirm.equals("yes")) {
            System.out.println("❌ Trade request cancelled.");
            return;
        }

        String insertTradeSQL = "INSERT INTO tbl_trade (offer_trader_id, target_trader_id, offer_item_id, target_item_id, trade_status, trade_DateRequest) "
                + "VALUES (?, ?, ?, ?, ?, datetime('now'))";

        con.addRecord(insertTradeSQL, traderId, targetTraderId, myItemId, targetItemId, "pending");
        System.out.println("✅ Trade request sent successfully!");
    }

// ----------------------------------------------------
// 7. VIEW TRADE REQUESTS (Show both INCOMING + OUTGOING)
// ----------------------------------------------------
    public void viewTradeRequests(Scanner scan) {
        System.out.println("\n--- VIEW TRADE REQUESTS ---");
        System.out.println("🧩 Logged in Trader ID: " + traderId);

        String query = "SELECT tr.trade_id, "
                + "ot.tbl_FullName AS offer_trader, "
                + "tt.tbl_FullName AS target_trader, "
                + "oi.item_Name AS offer_item, "
                + "ti.item_Name AS target_item, "
                + "tr.trade_status, tr.trade_DateRequest, "
                + "CASE "
                + "    WHEN tr.target_trader_id = ? THEN 'INCOMING' "
                + "    WHEN tr.offer_trader_id = ? THEN 'OUTGOING' "
                + "END AS direction "
                + "FROM tbl_trade tr "
                + "JOIN tbl_trader ot ON tr.offer_trader_id = ot.trader_id "
                + "JOIN tbl_trader tt ON tr.target_trader_id = tt.trader_id "
                + "JOIN tbl_items oi ON tr.offer_item_id = oi.items_id "
                + "JOIN tbl_items ti ON tr.target_item_id = ti.items_id "
                + "WHERE tr.target_trader_id = ? OR tr.offer_trader_id = ? "
                + "ORDER BY tr.trade_DateRequest DESC";

        try (Connection conn = config.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set traderId four times for placeholders
            pstmt.setInt(1, traderId);
            pstmt.setInt(2, traderId);
            pstmt.setInt(3, traderId);
            pstmt.setInt(4, traderId);

            ResultSet rs = pstmt.executeQuery();

            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-5s | %-10s | %-15s | %-15s | %-15s | %-10s | %-20s |\n",
                    "ID", "Type", "Offered By", "Their Item", "Target Item", "Status", "Date Requested");
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");

            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                System.out.printf("| %-5d | %-10s | %-15s | %-15s | %-15s | %-10s | %-20s |\n",
                        rs.getInt("trade_id"),
                        rs.getString("direction"),
                        rs.getString("offer_trader"),
                        rs.getString("offer_item"),
                        rs.getString("target_item"),
                        rs.getString("trade_status"),
                        rs.getString("trade_DateRequest"));
            }

            if (!hasRecords) {
                System.out.println("⚠ No trade requests found (incoming or outgoing).");
            }

            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("⚠ Error retrieving trade requests: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
// 8. RESPOND TO TRADE 
// ----------------------------------------------------
    public void respondTrade(Scanner scan) {
        System.out.println("\n--- RESPOND TO TRADE REQUESTS ---");

        String query = "SELECT tr.trade_id, ot.tbl_FullName AS offer_trader, "
                + "oi.item_Name AS offer_item, ti.item_Name AS target_item, "
                + "tr.trade_status, tr.trade_DateRequest, tr.offer_trader_id, tr.target_trader_id, "
                + "tr.offer_item_id, tr.target_item_id "
                + "FROM tbl_trade tr "
                + "JOIN tbl_trader ot ON tr.offer_trader_id = ot.trader_id "
                + "JOIN tbl_items oi ON tr.offer_item_id = oi.items_id "
                + "JOIN tbl_items ti ON tr.target_item_id = ti.items_id "
                + "WHERE tr.target_trader_id = ? AND tr.trade_status = 'pending'";

        try (Connection conn = config.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, traderId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-5s | %-15s | %-20s | %-20s | %-10s | %-20s |\n",
                    "ID", "Offered By", "Their Item", "Your Item", "Status", "Date Requested");
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");

            boolean hasPending = false;
            while (rs.next()) {
                hasPending = true;
                System.out.printf("| %-5d | %-15s | %-20s | %-20s | %-10s | %-20s |\n",
                        rs.getInt("trade_id"),
                        rs.getString("offer_trader"),
                        rs.getString("offer_item"),
                        rs.getString("target_item"),
                        rs.getString("trade_status"),
                        rs.getString("trade_DateRequest"));
            }

            if (!hasPending) {
                System.out.println("⚠ No pending trade requests to respond to.");
                System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
                return;
            }

            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
            System.out.print("Enter Trade ID to respond: ");
            int tradeId = scan.nextInt();
            scan.nextLine();

            System.out.println("Choose response:");
            System.out.println("1. Accept");
            System.out.println("2. Decline");
            System.out.println("3. Cancel");
            System.out.print("Enter choice: ");
            int choice = scan.nextInt();
            scan.nextLine();

            String newStatus = "";
            switch (choice) {
                case 1:
                    newStatus = "accepted";
                    break;
                case 2:
                    newStatus = "declined";
                    break;
                case 3:
                    System.out.println("❌ Action cancelled.");
                    return;
                default:
                    System.out.println("⚠ Invalid choice.");
                    return;
            }

            // === Update trade status ===
            String updateSQL = "UPDATE tbl_trade SET trade_status = ? WHERE trade_id = ?";
            con.updateRecord(updateSQL, newStatus, tradeId);

            if (newStatus.equals("accepted")) {
                // === Move trade to history ===
                String selectTrade = "SELECT * FROM tbl_trade WHERE trade_id = ?";
                try (PreparedStatement getTrade = conn.prepareStatement(selectTrade)) {
                    getTrade.setInt(1, tradeId);
                    ResultSet tradeData = getTrade.executeQuery();

                    if (tradeData.next()) {
                        String insertHistory = "INSERT INTO tbl_trade_history (trade_id, offer_trader_id, target_trader_id, "
                                + "offer_item_id, target_item_id, trade_status, trade_DateRequest, trade_DateCompleted) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now'))";

                        con.addRecord(insertHistory,
                                tradeData.getInt("trade_id"),
                                tradeData.getInt("offer_trader_id"),
                                tradeData.getInt("target_trader_id"),
                                tradeData.getInt("offer_item_id"),
                                tradeData.getInt("target_item_id"),
                                "completed",
                                tradeData.getString("trade_DateRequest"));

                        System.out.println("✅ Trade accepted and moved to history!");
                    }
                }
            } else {
                System.out.println("❌ Trade declined.");
            }

        } catch (SQLException e) {
            System.out.println("⚠ Error responding to trade: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
// 9. VIEW TRADE HISTORY
// ----------------------------------------------------
    public void viewTradeHistory() {
        System.out.println("\n--- TRADE HISTORY ---");

        String query = "SELECT h.history_id, "
                + "ot.tbl_FullName AS offer_trader, "
                + "tt.tbl_FullName AS target_trader, "
                + "oi.item_Name AS offer_item, "
                + "ti.item_Name AS target_item, "
                + "h.trade_status, h.trade_DateCompleted "
                + "FROM tbl_trade_history h "
                + "JOIN tbl_trader ot ON h.offer_trader_id = ot.trader_id "
                + "JOIN tbl_trader tt ON h.target_trader_id = tt.trader_id "
                + "LEFT JOIN tbl_items oi ON h.offer_item_id = oi.items_id "
                + "LEFT JOIN tbl_items ti ON h.target_item_id = ti.items_id "
                + "WHERE h.offer_trader_id = ? OR h.target_trader_id = ? "
                + "ORDER BY h.trade_DateCompleted DESC";

        try (Connection conn = config.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, traderId);
            pstmt.setInt(2, traderId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("-------------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-5s | %-15s | %-15s | %-20s | %-20s | %-10s | %-20s |\n",
                    "ID", "Offer By", "Trade With", "Offered Item", "Target Item", "Status", "Date Completed");
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------");

            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                System.out.printf("| %-5d | %-15s | %-15s | %-20s | %-20s | %-10s | %-20s |\n",
                        rs.getInt("history_id"),
                        rs.getString("offer_trader"),
                        rs.getString("target_trader"),
                        rs.getString("offer_item"),
                        rs.getString("target_item"),
                        rs.getString("trade_status"),
                        rs.getString("trade_DateCompleted"));
            }

            if (!hasRecords) {
                System.out.println("⚠ No trade history found.");
            }

            System.out.println("-------------------------------------------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("⚠ Error retrieving trade history: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
    // TRADER MENU
    // ----------------------------------------------------
    public void TraderMenu(Scanner scan) {
        int choice;
        do {
            System.out.println("\n========== TRADER MENU ==========");
            System.out.println("1. Offer Item");
            System.out.println("2. View My Items");
            System.out.println("3. Update My Item");
            System.out.println("4. Delete My Item");
            System.out.println("5. View Other Traders' Items");
            System.out.println("6. Request Trade");
            System.out.println("7. View Trade Requests");
            System.out.println("8. Respond to Trade");
            System.out.println("9. View Trade History");
            System.out.println("10. Logout");
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
                    ViewMyItems();
                    break;
                case 3:
                    UpdateMyItem(scan);
                    break;
                case 4:
                    DeleteMyItem(scan);
                    break;
                case 5:
                    viewOtherItems();
                    break;
                case 6:
                    requestTrade(scan);
                    break;
                case 7:
                    viewTradeRequests(scan);
                    break;
                case 8:
                    respondTrade(scan);
                    break;
                case 9:
                    viewTradeHistory();
                    break;

                case 10:
                    System.out.println("Logging out...");
                default:
                    System.out.println("Invalid option!");
            }

        } while (choice != 10);
    }
}
