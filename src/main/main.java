package main;

import config.config;
import java.util.Scanner;
import java.sql.*;

public class main {

    public static void main(String[] args) {
        config con = new config();
        Scanner scan = new Scanner(System.in);

        int choice = 0;

        while (choice != 4) {
            System.out.println("\nWELCOME TO BARTERZONE");
            System.out.println("1. Register as Trader");
            System.out.println("2. Login as Trader");
            System.out.println("3. Admin");
            System.out.println("4. Exit");
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

                        try (Connection connection = con.connectDB()) {
                            String usernameSQL = "SELECT COUNT(*) FROM tbl_trader WHERE tbl_Username = ?";
                            PreparedStatement pstmt = connection.prepareStatement(usernameSQL);
                            pstmt.setString(1, username);
                            ResultSet rs = pstmt.executeQuery();

                            if (rs.next() && rs.getInt(1) > 0) {
                                System.out.println(" Username is already used. Enter another username.\n");
                            } else {
                                break;
                            }
                        } catch (Exception e) {
                            System.out.println("⚠ Error checking username: " + e.getMessage());
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

                    String sqlRegister = "INSERT INTO tbl_trader (tbl_Username, tbl_Password, tbl_FullName, tbl_Email, tbl_Contact, tbl_Location, tbl_Status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    con.addRecord(sqlRegister, username, password, fullName, email, contact, location, "pending");
                    System.out.println("Trader registered successfully! (Status: pending, wait for admin approval)");

                    break;

                case 2:
                    System.out.println("\n--- Trader Login ---");
                    System.out.print("Enter Username: ");
                    String loginUser = scan.nextLine();

                    System.out.print("Enter Password: ");
                    String loginPass = scan.nextLine();

                    try (Connection connection = con.connectDB()) {
                        String loginSQL = "SELECT * FROM tbl_trader WHERE tbl_Username = ? AND tbl_Password = ?";
                        PreparedStatement pstmt = connection.prepareStatement(loginSQL);
                        pstmt.setString(1, loginUser);
                        pstmt.setString(2, loginPass);
                        ResultSet rs = pstmt.executeQuery();

                        if (rs.next()) {
                            String status = rs.getString("tbl_Status");
                            if ("approved".equalsIgnoreCase(status)) {
                                int traderId = rs.getInt("trader_id"); 
                                System.out.println(" Login successful! Welcome, " + rs.getString("tbl_FullName"));

                                TraderOption traderOption = new TraderOption(con, traderId); 
                                traderOption.TraderMenu(scan);
                            } else {
                                System.out.println("⚠ Your account is still '" + status + "'. Please wait for admin approval.");
                            }
                        } else {
                            System.out.println(" Invalid username or password!");
                        }
                    } catch (Exception e) {
                        System.out.println("⚠ Error during login: " + e.getMessage());
                    }
                    break;

                case 3:
                    System.out.println("\n--- ADMIN LOGIN ---");
                    System.out.print("Enter Admin Username: ");
                    String adminUser = scan.nextLine();

                    System.out.print("Enter Admin Password: ");
                    String adminPass = scan.nextLine();

                    if (adminUser.equals("admin") && adminPass.equals("jaylord")) {
                        System.out.println(" Admin login successful!");
                        AdminOption adminOption = new AdminOption(con);
                        adminOption.showAdminMenu(scan);
                    } else {
                        System.out.println(" Invalid admin account! Returning to Main Menu...");
                    }
                    break;

                case 4:
                    System.out.println("Exiting system... Goodbye!");
                    break;

                default:
                    System.out.println(" INVALID SELECTION. Please choose 1-4.");
            }
        }
        scan.close();
    }
}
