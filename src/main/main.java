package main;

import Admin.AdminOption;
import Admin.Announcement;
import Admin.SuperAdmin;
import Trader.TraderOption;
import config.config;
import java.util.Scanner;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class main {

    public static void main(String[] args) {
        config con = new config();
        Scanner scan = new Scanner(System.in);

        int choice = 0;

        while (choice != 5) {
            System.out.println("\nWELCOME TO BARTERZONE");
            System.out.println("1. Register as Trader");
            System.out.println("2. Login as Trader");
            System.out.println("3. Admin");
            System.out.println("4. View Announcements");
            System.out.println("5. Exit");
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
                    System.out.println("\n--- Register Trader ---");
                    String username;

                    while (true) {
                        System.out.print("Enter Username: ");
                        username = scan.nextLine();

                        String usernamesql = "SELECT * FROM tbl_trader WHERE tbl_Username = ?";
                        List<Map<String, Object>> existingUser = con.fetchRecords(usernamesql, username);

                        if (existingUser != null && !existingUser.isEmpty()) {
                            System.out.println(" Username is already used. Enter another username.\n");
                        } else {
                            break;
                        }
                    }

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

                    String hashedPass = config.hashPassword(password);

                    String sqlRegister = "INSERT INTO tbl_trader (tbl_Username, tbl_Password, tbl_FullName, tbl_Email, tbl_Contact, tbl_Location, tbl_Status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    con.addRecord(sqlRegister, username, hashedPass, fullName, email, contact, location, "pending");
                    System.out.println("Trader registered successfully! (Status: pending, wait for admin approval)");

                    break;

                case 2:
                    System.out.println("\n--- Trader Login ---");
                    System.out.print("Enter Username: ");
                    String loginUser = scan.nextLine();

                    System.out.print("Enter Password: ");
                    String loginPass = scan.nextLine();

                    String hashedLoginPass = config.hashPassword(loginPass);

                    String sql = "SELECT * FROM tbl_trader WHERE tbl_Username = ? AND tbl_Password = ?";
                    List<Map<String, Object>> result = con.fetchRecords(sql, loginUser, hashedLoginPass);

                    if (!result.isEmpty()) {
                        Map<String, Object> trader = result.get(0);
                        String status = (String) trader.get("tbl_Status");

                        if ("approved".equalsIgnoreCase(status)) {
                            int traderId = (Integer) trader.get("trader_id");
                            String traderFullName = (String) trader.get("tbl_FullName");

                            System.out.println(" Login successful! Welcome, " + traderFullName);
                            TraderOption traderOption = new TraderOption(con, traderId);
                            traderOption.TraderMenu(scan);
                        } else {
                            System.out.println(" Your account is still '" + status + "'. Please wait for admin approval.");
                        }
                    } else {
                        System.out.println(" Invalid username or password!");
                    }
                    break;

                case 3:
                    SuperAdmin.ensureDefaultAdmin(con);

                    System.out.println("\n--- ADMIN LOGIN ---");
                    System.out.print("Enter Admin Username: ");
                    String adminUser = scan.nextLine();
                    System.out.print("Enter Admin Password: ");
                    String adminPass = scan.nextLine();

                    String adminSql = "SELECT * FROM tbl_admin WHERE admin_username = ? AND admin_password = ?";
                    List<Map<String, Object>> adminResult = con.fetchRecords(adminSql, adminUser, adminPass);

                    if (!adminResult.isEmpty()) {
                        Map<String, Object> admin = adminResult.get(0);
                        int adminId = (Integer) admin.get("admin_id");  
                        System.out.println(" Admin login successful! Welcome, " + adminUser);

                        Announcement announcement = new Announcement(con);
                        announcement.viewActiveAnnouncements();

                        AdminOption adminOption = new AdminOption(con);
                        adminOption.AdminMenu(scan, adminId);  
                    } else {
                        System.out.println(" Invalid admin credentials.");
                    }
                    break;

                case 4:  
                    System.out.println("\n--- SYSTEM ANNOUNCEMENTS ---");
                    Announcement announcement = new Announcement(con);
                    announcement.viewActiveAnnouncements();
                    break;
                case 5:  
                    System.out.println("Exiting system... Goodbye!");
                    break;
                default:
                    System.out.println(" INVALID SELECTION. Please choose 1-5.");  
            }
        }
        scan.close();
    }
}
