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

            System.out.print("Enter Date Bought (YYYY-MM-DD) or Month DD, YYYY : ");
            String dateBought = scan.nextLine();

            System.out.print("Enter Description: ");
            String description = scan.nextLine();

            String sql = "INSERT INTO tbl_items (item_Name, item_Brand, item_Condition, item_Date, item_Description) VALUES (?, ?, ?, ?, ?)";
            con.addRecord(sql, itemName, brand, condition, dateBought, description);
            System.out.println(" Item offered successfully!");
        }

        public void ViewMyItems() {
            String query = "SELECT * FROM tbl_items";
            String[] headers = {"Item ID", "Item Name", "Brand", "Condition", "Date Bought", "Description"};
            String[] columns = {"items_id", "item_Name", "item_Brand", "item_Condition", "item_Date", "item_Description"};
            con.viewRecords(query, headers, columns);
        }

        public void UpdateMyItem(Scanner scan) {
            ViewMyItems();
            System.out.print("Enter Item ID: ");
            int item_id = scan.nextInt();

            System.out.print("\nEnter New Item name: ");
            String new_item = scan.next();

            System.out.print("Enter New Brand: ");
            String new_brand = scan.next();

            System.out.print("Enter New Condition: ");
            String new_condition = scan.next();

            System.out.print("Enter New date Bought: ");
            String new_date = scan.next();

            System.out.print("Enter New Item Description: ");
            String new_description = scan.next();

            String sqlUpdate = "UPDATE tbl_items SET item_Name = ?, item_Brand = ?, item_Condition = ?, item_Date = ?, item_Description = ? WHERE items_id = ? ";
            con.updateRecord(sqlUpdate, new_item, new_brand, new_condition, new_date, new_description, item_id);
            System.out.println(" Item updated successfully!");

        }

        public void DeleteMyItem(Scanner scan) {
            ViewMyItems();

            System.out.print("Enter Item ID: ");
            int item_id = scan.nextInt();

            String SqlDlete = "DELETE FROM tbl_items WHERE items_id = ?";
            con.deleteRecord(SqlDlete, item_id);

        }

        public void viewOtherItems() {
            System.out.println("VIEW OTHER ITEM");
        }

        public void requestTrade(Scanner scan) {
            System.out.println("Find an Item");
        }

        public void viewTradeRequests(Scanner scan) {
            viewOtherItems();
            System.out.println("Check your trade Requests");
            System.out.println("Enter Item ID");
            int id = scan.nextInt();
        }

        public void respondTrade(Scanner scan) {
            viewTradeRequests(scan);
            System.out.print("Enter Trade ID to respond: ");
            int tradeId = scan.nextInt();
            scan.nextLine();

            System.out.print("Accept or Decline? (approved/declined): ");
            String newStatus = scan.nextLine();

            String sqlrespond = "UPDATE tbl_trades SET status = ? WHERE trade_id = ?";
            con.updateRecord(sqlrespond, newStatus, tradeId);
            System.out.println(" Trade " + newStatus + "!");
        }

        public void TraderMenu(Scanner scan) {
            int choice;
            do {
                System.out.println("\n========== TRADER MENU ==========");
                System.out.println("1. Offer Item");
                System.out.println("2. View your Items");
                System.out.println("3. Update your item");
                System.out.println("4. Delete your item");
                System.out.println("5. View Other Traders' Items");
                System.out.println("6. Request Trade");
                System.out.println("7. View Trade Requests");
                System.out.println("8. Respond to Trade");
                System.out.println("9. Logout");
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
                        System.out.println("Logging out...");
                        break;               
                    default:
                        System.out.println("Invalid option!");
                        break;
                }

            } while (choice != 9);
        }
    }
