package main;

import config.config;
import java.util.Scanner;

public class TraderOption {
    private config con;

    public TraderOption(config con) {
        this.con = con;
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

        String sql = "INSERT INTO tbl_items (item_Name, item_Brand, item_Condition, item_Date, item_Description) VALUES (?, ?, ?, ?, ?)";
        con.addRecord(sql, itemName, brand, condition, dateBought, description);
        System.out.println(" Item offer submitted successfully!");
    }

    public void viewItems() {
        String query = "SELECT * FROM tbl_items";
        String[] headers = {"ID", "Item Name", "Brand", "Condition", "Date Bought", "Description"};
        String[] columns = {"item_id", "item_Name", "item_Brand", "item_Condition", "item_Date", "item_Description"};
        con.viewRecords(query, headers, columns);
    }

    public void updateItem(Scanner scan) {
        viewItems();
        System.out.print("Enter Item ID: ");
        int itemId = scan.nextInt();
        scan.nextLine();

        System.out.print("Enter New Item Name: ");
        String newItem = scan.nextLine();

        System.out.print("Enter New Brand: ");
        String newBrand = scan.nextLine();

        System.out.print("Enter New Condition: ");
        String newCondition = scan.nextLine();

        System.out.print("Enter New Date Bought: ");
        String newDate = scan.nextLine();

        System.out.print("Enter New Item Description: ");
        String newDescription = scan.nextLine();

        String sql = "UPDATE tbl_items SET item_Name = ?, item_Brand = ?, item_Condition = ?, item_Date = ?, item_Description = ? WHERE item_id = ?";
        con.updateRecord(sql, newItem, newBrand, newCondition, newDate, newDescription, itemId);
        System.out.println("✅ Item updated successfully!");
    }

    public void deleteItem(Scanner scan) {
        viewItems();
        System.out.print("Enter Item ID: ");
        int itemId = scan.nextInt();

        String sql = "DELETE FROM tbl_items WHERE item_id = ?";
        con.deleteRecord(sql, itemId);
        System.out.println("🗑 Item deleted successfully!");
    }

    public void showTraderMenu(Scanner scan) {
        int traderChoice;
        do {
            System.out.println("\nTRADER MENU");
            System.out.println("1. Offer Item");
            System.out.println("2. View Items");
            System.out.println("3. Update Item");
            System.out.println("4. Delete Item");
            System.out.println("5. Logout");
            System.out.print("Select option: ");
            traderChoice = scan.nextInt();
            scan.nextLine();

            switch (traderChoice) {
                case 1:
                    offerItem(scan);
                    break;
                case 2:
                    viewItems();
                    break;
                case 3:
                    updateItem(scan);
                    break;
                case 4:
                    deleteItem(scan);
                    break;
                case 5:
                    System.out.println("Logging out.");
                    break;
                default:
                    System.out.println("Invalid option!");
                    break;
            }
        } while (traderChoice != 5);
    }
}
