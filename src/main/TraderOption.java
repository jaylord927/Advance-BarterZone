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
        System.out.println("\n--- OFFER ITEM ---");

        String itemName = "";
        String brand = "";
        String condition = "";
        String dateBought = "";
        String description = "";

        while (true) {
            System.out.print("Enter Item Name: ");
            itemName = scan.nextLine().trim();
            if (!itemName.isEmpty()) {
                break;
            }
            System.out.println(" Item name cannot be empty. Please try again.");
        }

        while (true) {
            System.out.print("Enter Brand: ");
            brand = scan.nextLine().trim();
            if (!brand.isEmpty()) {
                break;
            }
            System.out.println(" Brand cannot be empty. Please try again.");
        }

        while (true) {
            System.out.print("Enter Condition (e.g., new, old, very good, fair): ");
            condition = scan.nextLine().trim();
            if (!condition.isEmpty()) {
                break;
            }
            System.out.println(" Condition cannot be empty. Please try again.");
        }

        while (true) {
            System.out.print("Enter Date Bought (e.g., 2025-09-26 or September 26 2025): ");
            dateBought = scan.nextLine().trim();
            if (dateBought.matches("\\d{4}-\\d{2}-\\d{2}") || dateBought.matches("[A-Za-z]+\\s+\\d{1,2}\\s+\\d{4}")) {
                break;
            }
            System.out.println(" Invalid date format. Example: 2025-09-26 or September 26 2025.");
        }

        while (true) {
            System.out.print("Enter Description: ");
            description = scan.nextLine().trim();
            if (!description.isEmpty()) {
                break;
            }
            System.out.println(" Description cannot be empty. Please try again.");
        }

        System.out.println("\nPlease confirm your item details:");
        System.out.println("------------------------------------------------");
        System.out.println("Item Name   : " + itemName);
        System.out.println("Brand       : " + brand);
        System.out.println("Condition   : " + condition);
        System.out.println("Date Bought : " + dateBought);
        System.out.println("Description : " + description);
        System.out.println("------------------------------------------------");
        System.out.print("Confirm add this item? (yes/no): ");
        String confirm = scan.nextLine().trim().toLowerCase();

        if (!confirm.equals("yes")) {
            System.out.println(" Item offering cancelled.");
            return;
        }

        String sql = "INSERT INTO tbl_items (item_Name, item_Brand, item_Condition, item_Date, item_Description, trader_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        int newId = con.addRecordAndReturnId(sql, itemName, brand, condition, dateBought, description, traderId);

        if (newId > 0) {
            System.out.println(" Item offered successfully! (Item ID: " + newId + ")");
        } else {
            System.out.println(" Failed to add item. Please try again.");
        }
    }

    // ----------------------------------------------------
// 2. VIEW MY ITEMS 
// ----------------------------------------------------
    public void ViewMyItems() {
        System.out.println("\n--- MY ITEMS ---");

        String query = "SELECT items_id, item_Name, item_Brand, item_Condition, item_Date, item_Description "
                + "FROM tbl_items WHERE trader_id = ? ORDER BY items_id ASC";

        try (Connection conn = config.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, traderId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-6s | %-20s | %-15s | %-12s | %-15s | %-25s |\n",
                    "ID", "Item Name", "Brand", "Condition", "Date Bought", "Description");
            System.out.println("------------------------------------------------------------------------------------------------------");

            boolean hasItems = false;
            while (rs.next()) {
                hasItems = true;
                String desc = rs.getString("item_Description");
                if (desc != null && desc.length() > 22) {
                    desc = desc.substring(0, 22) + "...";
                }

                System.out.printf("| %-6d | %-20s | %-15s | %-12s | %-15s | %-25s |\n",
                        rs.getInt("items_id"),
                        rs.getString("item_Name"),
                        rs.getString("item_Brand"),
                        rs.getString("item_Condition"),
                        rs.getString("item_Date"),
                        desc != null ? desc : "(no description)");
            }

            if (!hasItems) {
                System.out.println("|                                    No items found for your account.                                 |");
            }

            System.out.println("------------------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println(" Error retrieving items: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
// 3. UPDATE MY ITEM (with validation & confirmation)
// ----------------------------------------------------
    public void UpdateMyItem(Scanner scan) {
        System.out.println("\n--- UPDATE MY ITEM ---");

        ViewMyItems();

        System.out.print("\nEnter Item ID to Update (or 0 to cancel): ");
        int itemId = scan.nextInt();
        scan.nextLine();

        if (itemId == 0) {
            System.out.println(" Update cancelled.");
            return;
        }

        String checkSQL = "SELECT * FROM tbl_items WHERE items_id = ? AND trader_id = ?";
        try (Connection conn = config.connectDB();
                PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {

            checkStmt.setInt(1, itemId);
            checkStmt.setInt(2, traderId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("⚠ No item found with that ID or it doesn’t belong to you.");
                return;
            }

            String oldName = rs.getString("item_Name");
            String oldBrand = rs.getString("item_Brand");
            String oldCondition = rs.getString("item_Condition");
            String oldDate = rs.getString("item_Date");
            String oldDesc = rs.getString("item_Description");

            System.out.println("\nLeave field blank to keep current value.");

            System.out.print("Enter New Item Name [" + oldName + "]: ");
            String newName = scan.nextLine();
            if (newName.isEmpty()) {
                newName = oldName;
            }

            System.out.print("Enter New Brand [" + oldBrand + "]: ");
            String newBrand = scan.nextLine();
            if (newBrand.isEmpty()) {
                newBrand = oldBrand;
            }

            System.out.print("Enter New Condition (new/old/very good/etc.) [" + oldCondition + "]: ");
            String newCondition = scan.nextLine();
            if (newCondition.isEmpty()) {
                newCondition = oldCondition;
            }

            System.out.print("Enter New Date Bought (e.g., September 26 2025) [" + oldDate + "]: ");
            String newDate = scan.nextLine();
            if (newDate.isEmpty()) {
                newDate = oldDate;
            }

            System.out.print("Enter New Description [" + oldDesc + "]: ");
            String newDesc = scan.nextLine();
            if (newDesc.isEmpty()) {
                newDesc = oldDesc;
            }

            System.out.println("\nPlease confirm the update:");
            System.out.println("----------------------------------------------");
            System.out.println("Item Name     : " + newName);
            System.out.println("Brand         : " + newBrand);
            System.out.println("Condition     : " + newCondition);
            System.out.println("Date Bought   : " + newDate);
            System.out.println("Description   : " + newDesc);
            System.out.println("----------------------------------------------");
            System.out.print("Proceed with update? (yes/no): ");
            String confirm = scan.nextLine().trim().toLowerCase();

            if (!confirm.equals("yes")) {
                System.out.println(" Update cancelled.");
                return;
            }

            String sqlUpdate = "UPDATE tbl_items "
                    + "SET item_Name = ?, item_Brand = ?, item_Condition = ?, item_Date = ?, item_Description = ? "
                    + "WHERE items_id = ? AND trader_id = ?";

            con.updateRecord(sqlUpdate, newName, newBrand, newCondition, newDate, newDesc, itemId, traderId);
            System.out.println(" Item updated successfully!");

        } catch (SQLException e) {
            System.out.println(" Error updating item: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
// 4. DELETE MY ITEM 
// ----------------------------------------------------
    public void DeleteMyItem(Scanner scan) {
        System.out.println("\n--- DELETE MY ITEM ---");

        ViewMyItems();

        System.out.print("\nEnter Item ID to delete (or 0 to cancel): ");
        int itemId = scan.nextInt();
        scan.nextLine();

        if (itemId == 0) {
            System.out.println(" Deletion cancelled.");
            return;
        }

        String checkSQL = "SELECT item_Name, item_Brand, item_Condition, item_Date, item_Description "
                + "FROM tbl_items WHERE items_id = ? AND trader_id = ?";

        try (Connection conn = config.connectDB();
                PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {

            checkStmt.setInt(1, itemId);
            checkStmt.setInt(2, traderId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println(" No item found with that ID or it doesn’t belong to you.");
                return;
            }

            System.out.println("\nItem details to be deleted:");
            System.out.println("-------------------------------------------");
            System.out.println("Item Name    : " + rs.getString("item_Name"));
            System.out.println("Brand        : " + rs.getString("item_Brand"));
            System.out.println("Condition    : " + rs.getString("item_Condition"));
            System.out.println("Date Bought  : " + rs.getString("item_Date"));
            System.out.println("Description  : " + rs.getString("item_Description"));
            System.out.println("-------------------------------------------");

            System.out.print("Are you sure you want to delete this item? (yes/no): ");
            String confirm = scan.nextLine().trim().toLowerCase();

            if (!confirm.equals("yes")) {
                System.out.println(" Deletion cancelled.");
                return;
            }

            String sqlDelete = "DELETE FROM tbl_items WHERE items_id = ? AND trader_id = ?";
            con.deleteRecord(sqlDelete, itemId, traderId);

            System.out.println(" Item deleted successfully!");

        } catch (SQLException e) {
            System.out.println(" Error deleting item: " + e.getMessage());
        }
    }

// ----------------------------------------------------
// 5. VIEW OTHER TRADERS ITEMS 
// ----------------------------------------------------
    public void viewOtherItems() {
        System.out.println("\n--- VIEW OTHER TRADERS' ITEMS ---");
        System.out.println(" You are logged in as Trader ID: " + traderId);
        System.out.println(" Note: Your own items are not shown here.\n");

        String query = "SELECT i.items_id, i.item_Name, i.item_Brand, i.item_Condition, "
                + "i.item_Date, i.item_Description, "
                + "t.tbl_FullName AS trader_name, t.tbl_Location, t.tbl_Contact "
                + "FROM tbl_items i "
                + "JOIN tbl_trader t ON i.trader_id = t.trader_id "
                + "WHERE i.trader_id != ? "
                + "ORDER BY t.tbl_FullName ASC";

        try (Connection conn = config.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, traderId);
            ResultSet rs = pstmt.executeQuery();

            boolean hasRecords = false;

            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-5s | %-15s | %-12s | %-12s | %-15s | %-25s | %-15s | %-15s | %-15s |\n",
                    "ID", "Item Name", "Brand", "Condition", "Date Bought", "Description", "Trader Name", "Location", "Contact");
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");

            while (rs.next()) {
                hasRecords = true;
                System.out.printf("| %-5d | %-15s | %-12s | %-12s | %-15s | %-25s | %-15s | %-15s | %-15s |\n",
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

            if (!hasRecords) {
                System.out.println(" No items found from other traders at the moment.");
            }

            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");

            if (hasRecords) {
                System.out.println(" Tip: You can use Option 6 (Request Trade) to make an offer on one of these items.");
            }

        } catch (SQLException e) {
            System.out.println(" Error retrieving other traders' items: " + e.getMessage());
        }
    }

// ----------------------------------------------------
// 6. REQUEST TRADE 
// ----------------------------------------------------
    public void requestTrade(Scanner scan) {
        System.out.println("\n--- REQUEST TRADE ---");
        System.out.println(" You can browse other traders' items below:");

        viewOtherItems();

        int targetItemId = -1;
        while (true) {
            System.out.print("\nEnter the Item ID you want to trade for (or 0 to cancel): ");
            while (!scan.hasNextInt()) {
                System.out.print(" Invalid input. Please enter a numeric ID: ");
                scan.next();
            }
            targetItemId = scan.nextInt();
            scan.nextLine();

            if (targetItemId == 0) {
                System.out.println("Trade request cancelled.");
                return;
            }

            String getTraderSQL = "SELECT trader_id, item_Name FROM tbl_items WHERE items_id = ?";
            try (Connection conn = config.connectDB();
                    PreparedStatement pstmt = conn.prepareStatement(getTraderSQL)) {

                pstmt.setInt(1, targetItemId);
                ResultSet rs = pstmt.executeQuery();

                if (!rs.next()) {
                    System.out.println(" No item found with that ID. Please try again.");
                    continue;
                }

                int targetTraderId = rs.getInt("trader_id");
                String targetItemName = rs.getString("item_Name");

                if (targetTraderId == traderId) {
                    System.out.println(" You cannot trade for your own item. Choose another item.");
                    continue;
                }

                System.out.println("\nSelect one of your own items to offer in trade:");
                ViewMyItems();

                System.out.print("Enter your Item ID to offer (or 0 to cancel): ");
                int myItemId = scan.nextInt();
                scan.nextLine();

                if (myItemId == 0) {
                    System.out.println(" Trade cancelled.");
                    return;
                }

                String checkOwnerSQL = "SELECT item_Name FROM tbl_items WHERE items_id = ? AND trader_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkOwnerSQL)) {
                    checkStmt.setInt(1, myItemId);
                    checkStmt.setInt(2, traderId);
                    ResultSet checkRS = checkStmt.executeQuery();

                    if (!checkRS.next()) {
                        System.out.println(" You can only offer your own items. Try again.");
                        continue;
                    }

                    String myItemName = checkRS.getString("item_Name");

                    System.out.println("\n Trade Confirmation:");
                    System.out.println("You are offering your item: " + myItemName + " (ID: " + myItemId + ")");
                    System.out.println("For item: " + targetItemName + " (ID: " + targetItemId + ")");
                    System.out.print("Proceed with trade? (yes/no): ");
                    String confirm = scan.nextLine().trim().toLowerCase();

                    if (!confirm.equals("yes")) {
                        System.out.println(" Trade request cancelled.");
                        return;
                    }

                    String insertTradeSQL = "INSERT INTO tbl_trade (offer_trader_id, target_trader_id, offer_item_id, target_item_id, trade_status, trade_DateRequest) "
                            + "VALUES (?, ?, ?, ?, ?, datetime('now'))";

                    int result = con.addRecordAndReturnId(insertTradeSQL, traderId, targetTraderId, myItemId, targetItemId, "pending");

                    if (result > 0) {
                        System.out.println(" Trade request sent successfully!");
                    } else {
                        System.out.println(" Failed to send trade request. Please try again.");
                    }

                    return; 

                } catch (SQLException e) {
                    System.out.println(" Error verifying item ownership: " + e.getMessage());
                }

            } catch (SQLException e) {
                System.out.println(" Error retrieving item details: " + e.getMessage());
            }
        }
    }

// ----------------------------------------------------
// 7. VIEW TRADE REQUESTS 
// ----------------------------------------------------
public void viewTradeRequests(Scanner scan) {
    while (true) {
        System.out.println("\n--- VIEW TRADE REQUESTS ---");
        System.out.println(" Logged in Trader ID: " + traderId);

        String query = "SELECT tr.trade_id, "
                + "ot.tbl_FullName AS offer_trader, "
                + "tt.tbl_FullName AS target_trader, "
                + "oi.item_Name AS offer_item, "
                + "ti.item_Name AS target_item, "
                + "tr.trade_status, tr.trade_DateRequest, "
                + "CASE "
                + "    WHEN tr.target_trader_id = ? THEN 'Incoming Request' "
                + "    WHEN tr.offer_trader_id = ? THEN 'Your Request' "
                + "END AS request_type "
                + "FROM tbl_trade tr "
                + "JOIN tbl_trader ot ON tr.offer_trader_id = ot.trader_id "
                + "JOIN tbl_trader tt ON tr.target_trader_id = tt.trader_id "
                + "JOIN tbl_items oi ON tr.offer_item_id = oi.items_id "
                + "JOIN tbl_items ti ON tr.target_item_id = ti.items_id "
                + "WHERE tr.target_trader_id = ? OR tr.offer_trader_id = ? "
                + "ORDER BY tr.trade_DateRequest DESC";

        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, traderId);
            pstmt.setInt(2, traderId);
            pstmt.setInt(3, traderId);
            pstmt.setInt(4, traderId);

            ResultSet rs = pstmt.executeQuery();

            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-5s | %-18s | %-15s | %-18s | %-18s | %-10s | %-20s |\n",
                    "ID", "Type", "From (Trader)", "Offered Item", "Target Item", "Status", "Date Requested");
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");

            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                System.out.printf("| %-5d | %-18s | %-15s | %-18s | %-18s | %-10s | %-20s |\n",
                        rs.getInt("trade_id"),
                        rs.getString("request_type"),
                        rs.getString("offer_trader"),
                        rs.getString("offer_item"),
                        rs.getString("target_item"),
                        rs.getString("trade_status"),
                        rs.getString("trade_DateRequest"));
            }

            if (!hasRecords) {
                System.out.println(" No trade requests found (incoming or outgoing).");
            }

            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println(" Error retrieving trade requests: " + e.getMessage());
        }

        System.out.print("\nDo you want to view again? (yes/no): ");
        String again = scan.nextLine().trim().toLowerCase();
        if (!again.equals("yes")) {
            break; 
        }
    }
}


// ----------------------------------------------------
// 8. RESPOND TO TRADE 
// ----------------------------------------------------
public void respondTrade(Scanner scan) {
    while (true) {
        System.out.println("\n--- RESPOND TO TRADE REQUESTS ---");

        String query = "SELECT tr.trade_id, ot.tbl_FullName AS offer_trader, "
                + "oi.item_Name AS offer_item, ti.item_Name AS target_item, "
                + "tr.trade_status, tr.trade_DateRequest, tr.offer_trader_id, tr.target_trader_id "
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
                System.out.println(" No pending trade requests to respond to.");
                break;
            }

            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
            System.out.print("Enter Trade ID to respond (or 0 to cancel): ");
            int tradeId = scan.nextInt();
            scan.nextLine();

            if (tradeId == 0) {
                System.out.println("Action cancelled.");
                break;
            }

            String checkSQL = "SELECT trade_id FROM tbl_trade WHERE trade_id = ? AND target_trader_id = ? AND trade_status = 'pending'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
                checkStmt.setInt(1, tradeId);
                checkStmt.setInt(2, traderId);
                ResultSet validTrade = checkStmt.executeQuery();
                if (!validTrade.next()) {
                    System.out.println(" Invalid Trade ID or not your trade request.");
                    continue;
                }
            }

            System.out.println("\nChoose response:");
            System.out.println("1. Accept Trade");
            System.out.println("2. Decline Trade");
            System.out.println("3. Send Message to Trader");
            System.out.println("4. Confirm Item Received");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            int choice = scan.nextInt();
            scan.nextLine();

            switch (choice) {
                case 1: 
                    System.out.print("Are you sure you want to accept this trade? (yes/no): ");
                    String confirmAccept = scan.nextLine().trim().toLowerCase();
                    if (confirmAccept.equals("yes")) {
                        String updateSQL = "UPDATE tbl_trade SET trade_status = 'accepted' WHERE trade_id = ?";
                        con.updateRecord(updateSQL, tradeId);
                        moveTradeToHistory(conn, tradeId);
                        System.out.println(" Trade accepted and moved to history!");
                    } else {
                        System.out.println(" Trade acceptance cancelled.");
                    }
                    break;

                case 2: 
                    System.out.print("Are you sure you want to decline this trade? (yes/no): ");
                    String confirmDecline = scan.nextLine().trim().toLowerCase();
                    if (confirmDecline.equals("yes")) {
                        con.updateRecord("UPDATE tbl_trade SET trade_status = 'declined' WHERE trade_id = ?", tradeId);
                        System.out.println(" Trade declined.");
                    } else {
                        System.out.println(" Decline cancelled.");
                    }
                    break;

                case 3: 
                    System.out.print("Enter your message: ");
                    String message = scan.nextLine();

                    String getTradeSQL = "SELECT offer_trader_id, target_trader_id FROM tbl_trade WHERE trade_id = ?";
                    try (PreparedStatement getTrade = conn.prepareStatement(getTradeSQL)) {
                        getTrade.setInt(1, tradeId);
                        ResultSet tradeData = getTrade.executeQuery();
                        if (tradeData.next()) {
                            int sender = traderId;
                            int receiver = (tradeData.getInt("offer_trader_id") == traderId)
                                    ? tradeData.getInt("target_trader_id")
                                    : tradeData.getInt("offer_trader_id");

                            String insertMsg = "INSERT INTO tbl_trade_messages (trade_id, sender_id, receiver_id, message_text) VALUES (?, ?, ?, ?)";
                            con.addRecord(insertMsg, tradeId, sender, receiver, message);
                            System.out.println(" Message sent successfully!");
                        }
                    }
                    break;

                case 4: 
                    System.out.print("Confirm item received? (yes/no): ");
                    String confirm = scan.nextLine().trim().toLowerCase();
                    if (confirm.equals("yes")) {
                        String checkTrade = "SELECT offer_trader_id, target_trader_id FROM tbl_trade WHERE trade_id = ?";
                        try (PreparedStatement checkStmt = conn.prepareStatement(checkTrade)) {
                            checkStmt.setInt(1, tradeId);
                            ResultSet data = checkStmt.executeQuery();
                            if (data.next()) {
                                String confirmSQL;
                                if (data.getInt("offer_trader_id") == traderId) {
                                    confirmSQL = "UPDATE tbl_trade SET offer_received = 1 WHERE trade_id = ?";
                                } else {
                                    confirmSQL = "UPDATE tbl_trade SET target_received = 1 WHERE trade_id = ?";
                                }
                                con.updateRecord(confirmSQL, tradeId);
                                System.out.println(" Item marked as received!");
                            }
                        }
                    } else {
                        System.out.println("Cancelled confirmation.");
                    }
                    break;

                case 5:
                    System.out.println("Returning to menu...");
                    return;

                default:
                    System.out.println(" Invalid choice. Try again.");
                    break;
            }

            System.out.print("\nDo you want to respond to another trade? (yes/no): ");
            String again = scan.nextLine().trim().toLowerCase();
            if (!again.equals("yes")) {
                break;
            }

        } catch (SQLException e) {
            System.out.println(" Error responding to trade: " + e.getMessage());
        }
    }
}


    private void moveTradeToHistory(Connection conn, int tradeId) throws SQLException {
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
            }
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
        System.out.printf("| %-5s | %-18s | %-18s | %-20s | %-20s | %-12s | %-20s |\n",
                "ID", "Offered By", "Traded With", "Offered Item", "Target Item", "Status", "Date Completed");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------");

        boolean hasRecords = false;
        while (rs.next()) {
            hasRecords = true;

            String offerItem = rs.getString("offer_item") != null ? rs.getString("offer_item") : "[Deleted Item]";
            String targetItem = rs.getString("target_item") != null ? rs.getString("target_item") : "[Deleted Item]";
            String dateCompleted = rs.getString("trade_DateCompleted") != null ? rs.getString("trade_DateCompleted") : "N/A";

            System.out.printf("| %-5d | %-18s | %-18s | %-20s | %-20s | %-12s | %-20s |\n",
                    rs.getInt("history_id"),
                    rs.getString("offer_trader"),
                    rs.getString("target_trader"),
                    offerItem,
                    targetItem,
                    rs.getString("trade_status"),
                    dateCompleted);
        }

        if (!hasRecords) {
            System.out.println(" No trade history found. You haven’t completed any trades yet.");
        }

        System.out.println("-------------------------------------------------------------------------------------------------------------------------------");

    } catch (SQLException e) {
        System.out.println(" Error retrieving trade history: " + e.getMessage());
    }
}

// ----------------------------------------------------
// 10. SEND MESSAGE 
// ----------------------------------------------------
public void sendMessage(Scanner scan) {
    System.out.println("\n--- SEND MESSAGE TO ANOTHER TRADER ---");

    String listTraders = "SELECT trader_id, tbl_FullName, tbl_Location, tbl_Contact "
            + "FROM tbl_trader WHERE trader_id != ?";

    try (Connection conn = config.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(listTraders)) {

        pstmt.setInt(1, traderId);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("--------------------------------------------------------------------------------------");
        System.out.printf("| %-5s | %-25s | %-20s | %-15s |\n",
                "ID", "Trader Name", "Location", "Contact");
        System.out.println("--------------------------------------------------------------------------------------");

        boolean hasTraders = false;
        while (rs.next()) {
            hasTraders = true;
            System.out.printf("| %-5d | %-25s | %-20s | %-15s |\n",
                    rs.getInt("trader_id"),
                    rs.getString("tbl_FullName"),
                    rs.getString("tbl_Location"),
                    rs.getString("tbl_Contact"));
        }

        if (!hasTraders) {
            System.out.println(" No other traders available to message at the moment.");
            System.out.println("--------------------------------------------------------------------------------------");
            return;
        }

        System.out.println("--------------------------------------------------------------------------------------");

        int receiverId = -1;
        while (true) {
            System.out.print("Enter Trader ID to message: ");
            if (!scan.hasNextInt()) {
                System.out.println(" Invalid input. Please enter a numeric ID.");
                scan.nextLine();
                continue;
            }
            receiverId = scan.nextInt();
            scan.nextLine();

            if (receiverId == traderId) {
                System.out.println(" You cannot send a message to yourself!");
                continue;
            }

            String checkTraderSQL = "SELECT COUNT(*) AS count FROM tbl_trader WHERE trader_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkTraderSQL)) {
                checkStmt.setInt(1, receiverId);
                ResultSet checkRS = checkStmt.executeQuery();
                if (checkRS.next() && checkRS.getInt("count") > 0) {
                    break; 
                } else {
                    System.out.println(" No trader found with that ID. Try again.");
                }
            }
        }

        System.out.print("Enter your message: ");
        String message = scan.nextLine().trim();

        if (message.isEmpty()) {
            System.out.println(" Message cannot be empty. Operation cancelled.");
            return;
        }

        System.out.print("Send this message? (yes/no): ");
        String confirm = scan.nextLine().trim().toLowerCase();

        if (!confirm.equals("yes")) {
            System.out.println(" Message cancelled.");
            return;
        }

        String sql = "INSERT INTO tbl_trade_messages (trade_id, sender_id, receiver_id, message_text, message_date) "
                   + "VALUES (NULL, ?, ?, ?, datetime('now'))";

        try {
            con.addRecord(sql, traderId, receiverId, message);
            System.out.println(" Message sent successfully!");
        } catch (Exception e) {
            System.out.println(" Failed to send message: " + e.getMessage());
        }

    } catch (SQLException e) {
        System.out.println("⚠ Error sending message: " + e.getMessage());
    }
}

// ----------------------------------------------------
// 11. VIEW & REPLY TO MESSAGES
// ----------------------------------------------------
public void viewAllMessages(Scanner scan) {
    System.out.println("\n--- VIEW MESSAGES ---");

    String listTraders = "SELECT DISTINCT "
            + "CASE WHEN sender_id = ? THEN receiver_id ELSE sender_id END AS chat_partner_id, "
            + "t.tbl_FullName "
            + "FROM tbl_trade_messages m "
            + "JOIN tbl_trader t ON t.trader_id = CASE WHEN m.sender_id = ? THEN m.receiver_id ELSE m.sender_id END "
            + "WHERE sender_id = ? OR receiver_id = ?";

    try (Connection conn = config.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(listTraders)) {

        pstmt.setInt(1, traderId);
        pstmt.setInt(2, traderId);
        pstmt.setInt(3, traderId);
        pstmt.setInt(4, traderId);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("-------------------------------------------------------------");
        System.out.printf("| %-5s | %-25s |\n", "ID", "Chat Partner");
        System.out.println("-------------------------------------------------------------");

        boolean hasChats = false;
        while (rs.next()) {
            hasChats = true;
            System.out.printf("| %-5d | %-25s |\n",
                    rs.getInt("chat_partner_id"),
                    rs.getString("tbl_FullName"));
        }

        if (!hasChats) {
            System.out.println(" No messages yet. Start chatting using 'Message Another Trader' first.");
            return;
        }

        System.out.println("-------------------------------------------------------------");
        int partnerId = -1;
        while (true) {
            System.out.print("Enter Trader ID to open chat: ");
            if (!scan.hasNextInt()) {
                System.out.println(" Invalid input. Please enter a valid numeric ID.");
                scan.nextLine();
                continue;
            }
            partnerId = scan.nextInt();
            scan.nextLine();

            String checkChatSQL = "SELECT COUNT(*) AS cnt FROM tbl_trade_messages "
                                + "WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkChatSQL)) {
                checkStmt.setInt(1, traderId);
                checkStmt.setInt(2, partnerId);
                checkStmt.setInt(3, partnerId);
                checkStmt.setInt(4, traderId);
                ResultSet checkRS = checkStmt.executeQuery();
                if (checkRS.next() && checkRS.getInt("cnt") > 0) {
                    break; 
                } else {
                    System.out.println(" No chat history found with that Trader ID. Try again.");
                }
            }
        }

        showChatHistory(scan, conn, partnerId);

    } catch (SQLException e) {
        System.out.println("⚠ Error viewing messages: " + e.getMessage());
    }
}


// ----------------------------------------------------
// SHOW CHAT HISTORY & REPLY
// ----------------------------------------------------
private void showChatHistory(Scanner scan, Connection conn, int partnerId) {
    System.out.println("\n--- CHAT HISTORY ---");

    String chatQuery = "SELECT m.message_text, m.message_date, "
            + "s.tbl_FullName AS sender, r.tbl_FullName AS receiver "
            + "FROM tbl_trade_messages m "
            + "JOIN tbl_trader s ON m.sender_id = s.trader_id "
            + "JOIN tbl_trader r ON m.receiver_id = r.trader_id "
            + "WHERE (m.sender_id = ? AND m.receiver_id = ?) "
            + "   OR (m.sender_id = ? AND m.receiver_id = ?) "
            + "ORDER BY m.message_date ASC";

    try (PreparedStatement chatStmt = conn.prepareStatement(chatQuery)) {
        chatStmt.setInt(1, traderId);
        chatStmt.setInt(2, partnerId);
        chatStmt.setInt(3, partnerId);
        chatStmt.setInt(4, traderId);
        ResultSet rs = chatStmt.executeQuery();

        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.printf("| %-15s | %-45s | %-20s |\n", "Sender", "Message", "Date");
        System.out.println("---------------------------------------------------------------------------------------------");

        boolean hasMsgs = false;
        while (rs.next()) {
            hasMsgs = true;
            String sender = rs.getString("sender");
            String msg = rs.getString("message_text");
            String date = rs.getString("message_date");

            if (sender.equalsIgnoreCase(getTraderName(conn, traderId))) {
                System.out.printf("| %-15s | %-45s | %-20s |\n", " You", msg, date);
            } else {
                System.out.printf("| %-15s | %-45s | %-20s |\n", sender, msg, date);
            }
        }

        if (!hasMsgs) {
            System.out.println(" No chat messages found.");
        }

        System.out.println("---------------------------------------------------------------------------------------------");

        while (true) {
            System.out.print("Reply to this chat? (yes/no): ");
            String reply = scan.nextLine().trim().toLowerCase();

            if (reply.equals("no")) {
                System.out.println(" Returning to message list...");
                return;
            } else if (reply.equals("yes")) {
                System.out.print("Enter your message: ");
                String msg = scan.nextLine().trim();

                if (msg.isEmpty()) {
                    System.out.println(" Message cannot be empty.");
                    continue;
                }

                String insertMsg = "INSERT INTO tbl_trade_messages (trade_id, sender_id, receiver_id, message_text, message_date) "
                                 + "VALUES (NULL, ?, ?, ?, datetime('now'))";
                try {
                    con.addRecord(insertMsg, traderId, partnerId, msg);
                    System.out.println("Message sent successfully!");
                } catch (Exception e) {
                    System.out.println(" Failed to send message: " + e.getMessage());
                }

                System.out.print("Send another message? (yes/no): ");
                if (!scan.nextLine().trim().equalsIgnoreCase("yes")) {
                    break;
                }

            } else {
                System.out.println(" Invalid input. Please type 'yes' or 'no'.");
            }
        }

    } catch (SQLException e) {
        System.out.println("⚠ Error showing chat history: " + e.getMessage());
    }
}


// ----------------------------------------------------
// HELPER: Get Trader Name by ID
// ----------------------------------------------------
private String getTraderName(Connection conn, int traderId) throws SQLException {
    String name = "";
    String sql = "SELECT tbl_FullName FROM tbl_trader WHERE trader_id = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, traderId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            name = rs.getString("tbl_FullName");
        }
    }
    return name;
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
            System.out.println("10. Message Another Trader");
            System.out.println("11. View Messages");
            System.out.println("12. Logout");
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
                    sendMessage(scan);
                    break;
                case 11:
                    viewAllMessages(scan);
                    break;
                case 12:
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid option!");
            }

        } while (choice != 12);
    }
}
