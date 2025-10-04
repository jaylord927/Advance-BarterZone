package main;

import config.config;
import java.util.Scanner;

public class main {
    
    public static void viewitem() {
        String votersQuery = "SELECT * FROM tbl_items";
        String[] votersHeaders = {"ID", "Item Name", "Brand", "condition", "date bought", "Description"};
        String[] votersColumns = {"item_id", "item_Name", "item_Brand", "item_Condition", "item_Date", "item_Description"};
        config con = new config();
        con.viewRecords(votersQuery, votersHeaders, votersColumns);
    }
    
    public static void main(String[] args) {
        config con = new config();
        con.connectDB();
        Scanner scan = new Scanner(System.in);
        
        int choice;
        
        System.out.println("WELCOME BARTERZONE");
        System.out.println("1. TRADERS");
        System.out.println("2. ADMIN");
        System.out.print("Select user: ");
        choice = scan.nextInt();
        scan.nextLine();
        
        switch (choice) {
            case 1:
                int traderChoice;
                System.out.println("\nTRADERS MENU");
                System.out.println("1. Register as Trader");
                System.out.println("2. Offer Item");
                System.out.println("3. View Item");
                System.out.println("4. update Item");
                System.out.println("5. Delete Item");
                System.out.print("Select option: ");
                traderChoice = scan.nextInt();
                scan.nextLine();

                //while(traderChoice != 5)
                switch (traderChoice) {
                    
                    case 1:
                        
                        System.out.println("\n--- Register Trader ---");
                        
                        System.out.print("Enter Username: ");
                        String username = scan.nextLine();
                        
                        System.out.print("Enter Password: ");
                        String password = scan.nextLine();
                        
                        System.out.print("Enter Full Name: ");
                        String fullName = scan.nextLine();
                        
                        System.out.print("Enter Email: ");
                        String email = scan.nextLine();
                        
                        System.out.print("Enter Contact: ");
                        String contact = scan.nextLine();
                        
                        System.out.print("Enter Location: ");
                        String location = scan.nextLine();
                        
                        String sql = "INSERT INTO tbl_traders (tbl_Username, tbl_Password, tbl_FullName, tbl_Email, tbl_Contact, tbl_Location) VALUES (?, ?, ?, ?, ?, ?)";
                        System.out.println("✅ Trader registered successfully!");
                        con.addRecord(sql, username, password, fullName, email, contact, location);
                        break;
                    
                    case 2:
                        
                        System.out.println("\n--- Offer Item ---");
                        
                        System.out.print("Enter item_name: ");
                        String item_name = scan.nextLine();
                        
                        System.out.print("Enter Brand: ");
                        String brand = scan.nextLine();
                        
                        System.out.print("Enter Condition (new/old): ");
                        String condition = scan.nextLine();
                        
                        System.out.print("Enter Date Bought (YYYY-MM-DD): ");
                        String dateBought = scan.nextLine();
                        
                        System.out.print("Enter Description: ");
                        String description = scan.nextLine();
                        
                        String sqlitem = "INSERT INTO tbl_items (item_Name, item_Brand, item_Condition, item_Date, item_Description) VALUES (?, ?, ?, ?, ?)";
                        System.out.println("✅ Item offer submitted successfully!");
                        con.addRecord(sqlitem, item_name, brand, condition, dateBought, description);
                        break;
                    
                    case 3:
                        viewitem();
                        break;
                    
                    case 4:
                        viewitem();
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
                        
                        sqlitem = "UPDATE tbl_items SET item_Name = ?, item_Brand = ?, item_Condition = ?, item_Date = ?, item_Description = ? WHERE item_id = ? ";
                        con.updateRecord(sqlitem, new_item, new_brand, new_condition, new_date, new_description, item_id);
                    case 5:
                        viewitem();
                        
                        System.out.print("Enter Item ID: ");
                        item_id = scan.nextInt();
                        
                        sqlitem = "DELETE FROM tbl_items WHERE item_id = ?";
                        con.deleteRecord(sqlitem, item_id);
                    default:
                        System.out.println("Invalid selection!");
                        break;
                }
                
                break;
            
            case 2:
                System.out.println("ADMIN FUNCTIONALITY NOT IMPLEMENTED");
                break;
            
            default:
                System.out.println("❌ INVALID SELECTION");
        }
        
    }
}
