package Trader;

import config.config;
import java.sql.*;
import java.util.Scanner;

public class ManageRespond {

    private config con;
    private int traderId;
    private int tradeId;

    public ManageRespond(config con, int traderId, int tradeId) {
        this.con = con;
        this.traderId = traderId;
        this.tradeId = tradeId;
    }

    // ----------------------------------------------------
    // MAIN RESPOND MENU
    // ----------------------------------------------------
    public void respondTrade(Scanner scan) {
        int respondChoice;
        do {
            System.out.println("\n--- MANAGE TRADE RESPONSE ---");
            System.out.println("Trade ID: " + tradeId);
            System.out.println("1. Accept/Decline Trade");
            System.out.println("2. Arrangement and Negotiating Details");
            System.out.println("3. View Trade Status");
            System.out.println("4. Send Message to Trader");
            System.out.println("5. Back to Trade Menu");
            System.out.print("Select option: ");

            while (!scan.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scan.next();
                System.out.print("Select option: ");
            }

            respondChoice = scan.nextInt();
            scan.nextLine();

            switch (respondChoice) {
                case 1:
                    acceptDeclineTrade(scan);
                    break;
                case 2:
                    arrangementDetails(scan);
                    break;
                case 3:
                    viewTradeStatus();
                    break;
                case 4:
                    sendTradeMessage(scan);
                    break;
                case 5:
                    System.out.println("Returning to Trade Menu...");
                    break;
                default:
                    System.out.println("Invalid option! Please choose 1-5.");
            }

        } while (respondChoice != 5);
    }

    // ----------------------------------------------------
    // 1. ACCEPT/DECLINE TRADE
    // ----------------------------------------------------
    private void acceptDeclineTrade(Scanner scan) {
        System.out.println("\n--- ACCEPT/DECLINE TRADE ---");

        String tradeQuery = "SELECT tr.trade_id, ot.tbl_FullName AS offer_trader, "
                + "tt.tbl_FullName AS target_trader, "
                + "oi.item_Name AS offer_item, ti.item_Name AS target_item, "
                + "tr.trade_status, tr.offer_trader_id, tr.target_trader_id "
                + "FROM tbl_trade tr "
                + "JOIN tbl_trader ot ON tr.offer_trader_id = ot.trader_id "
                + "JOIN tbl_trader tt ON tr.target_trader_id = tt.trader_id "
                + "JOIN tbl_items oi ON tr.offer_item_id = oi.items_id "
                + "JOIN tbl_items ti ON tr.target_item_id = ti.items_id "
                + "WHERE tr.trade_id = ?";

        try (Connection conn = con.connectDB();
                PreparedStatement pstmt = conn.prepareStatement(tradeQuery)) {

            pstmt.setInt(1, tradeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int offerTraderId = rs.getInt("offer_trader_id");
                int targetTraderId = rs.getInt("target_trader_id");

                System.out.println("Trade Details:");
                System.out.println("----------------------------------------");
                System.out.println("Trade ID: " + rs.getInt("trade_id"));
                System.out.println("From: " + rs.getString("offer_trader"));
                System.out.println("Their Item: " + rs.getString("offer_item"));
                System.out.println("Your Item: " + rs.getString("target_item"));
                System.out.println("Current Status: " + rs.getString("trade_status"));
                System.out.println("----------------------------------------");

                if (offerTraderId == traderId) {
                    System.out.println(" You cannot accept your own trade request!");
                    System.out.println(" You are the one who sent this trade request.");
                    System.out.println(" Please wait for " + rs.getString("target_trader") + " to respond.");
                    return;
                }

                if (targetTraderId != traderId) {
                    System.out.println(" You are not the target of this trade request!");
                    System.out.println(" Only the receiver can accept or decline trades.");
                    return;
                }

                if (!"pending".equals(rs.getString("trade_status"))) {
                    System.out.println(" This trade is no longer pending!");
                    System.out.println(" Current status: " + rs.getString("trade_status"));
                    System.out.println(" You can only accept/decline pending trades.");
                    return;
                }

                System.out.println("\nChoose action:");
                System.out.println("1. Accept Trade");
                System.out.println("2. Decline Trade");
                System.out.println("3. Back");
                System.out.print("Enter choice: ");

                int choice = scan.nextInt();
                scan.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("Are you sure you want to ACCEPT this trade? (yes/no): ");
                        String confirm = scan.nextLine().trim().toLowerCase();
                        if (confirm.equals("yes")) {
                            String updateSQL = "UPDATE tbl_trade SET trade_status = 'negotiating' WHERE trade_id = ?";
                            con.updateRecord(updateSQL, tradeId);

                            initializeManageRespondForBothTraders(conn);
                            System.out.println(" Trade accepted! You can now proceed with arrangement details.");
                        } else {
                            System.out.println("Trade acceptance cancelled.");
                        }
                        break;

                    case 2:
                        System.out.print("Are you sure you want to DECLINE this trade? (yes/no): ");
                        String confirmDecline = scan.nextLine().trim().toLowerCase();
                        if (confirmDecline.equals("yes")) {
                            String declineSQL = "UPDATE tbl_trade SET trade_status = 'declined' WHERE trade_id = ?";
                            con.updateRecord(declineSQL, tradeId);
                            System.out.println(" Trade declined.");
                        } else {
                            System.out.println("Trade decline cancelled.");
                        }
                        break;

                    case 3:
                        System.out.println("Returning to menu...");
                        break;

                    default:
                        System.out.println("Invalid choice.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving trade details: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
    // HELPER: INITIALIZE MANAGE RESPOND FOR BOTH TRADERS
    // ----------------------------------------------------
    private void initializeManageRespondForBothTraders(Connection conn) throws SQLException {
        String tradeQuery = "SELECT offer_trader_id, target_trader_id FROM tbl_trade WHERE trade_id = ?";
        try (PreparedStatement tradeStmt = conn.prepareStatement(tradeQuery)) {
            tradeStmt.setInt(1, tradeId);
            ResultSet tradeRs = tradeStmt.executeQuery();

            if (tradeRs.next()) {
                int offerTraderId = tradeRs.getInt("offer_trader_id");
                int targetTraderId = tradeRs.getInt("target_trader_id");

                initializeManageRespondForTrader(conn, offerTraderId);
                initializeManageRespondForTrader(conn, targetTraderId);
            }
        }
    }

    private void initializeManageRespondForTrader(Connection conn, int specificTraderId) throws SQLException {
        String checkSQL = "SELECT COUNT(*) FROM tbl_ManageRespond WHERE trade_id = ? AND trader_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
            checkStmt.setInt(1, tradeId);
            checkStmt.setInt(2, specificTraderId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                String insertSQL = "INSERT INTO tbl_ManageRespond (trade_id, trader_id, details_confirmed, item_received, exchange_method_agreed) "
                        + "VALUES (?, ?, 0, 0, 0)";
                con.addRecord(insertSQL, tradeId, specificTraderId);
            }
        }
    }

    // ----------------------------------------------------
// 2. ARRANGEMENT AND NEGOTIATING DETAILS
// ----------------------------------------------------
    private void arrangementDetails(Scanner scan) {
        System.out.println("\n--- ARRANGEMENT AND NEGOTIATING DETAILS ---");

        if (!isTradeStatus("negotiating") && !isTradeStatus("arrangements_confirmed")) {
            System.out.println(" You must accept the trade first before setting up arrangements.");
            return;
        }

        int arrangementChoice;
        do {
            System.out.println("\n1. Propose Exchange Method");
            System.out.println("2. Respond to Exchange Method Proposal");
            System.out.println("3. Set Delivery Details");
            System.out.println("4. Set Meetup Details");
            System.out.println("5. Review Delivery/Meetup Details");
            System.out.println("6. Confirm/Decline Arrangements");
            System.out.println("7. Confirm Item Received");
            System.out.println("8. Back");
            System.out.print("Select option: ");

            while (!scan.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scan.next();
                System.out.print("Select option: ");
            }

            arrangementChoice = scan.nextInt();
            scan.nextLine();

            switch (arrangementChoice) {
                case 1:
                    proposeExchangeMethod(scan);
                    break;
                case 2:
                    respondToExchangeMethod(scan);
                    break;
                case 3:
                    if (isExchangeMethodAgreed() && isExchangeMethod("delivery")) {
                        setDeliveryDetails(scan);
                    } else {
                        System.out.println(" You can only set delivery details after both traders agree on 'Delivery' as the exchange method.");
                    }
                    break;
                case 4:
                    if (isExchangeMethodAgreed() && isExchangeMethod("meet_up")) {
                        setMeetupDetails(scan);
                    } else {
                        System.out.println(" You can only set meetup details after both traders agree on 'Meet Up' as the exchange method.");
                    }
                    break;
                case 5:
                    reviewDetails();
                    break;
                case 6:
                    confirmArrangements(scan);
                    break;
                case 7:
                    confirmItemReceived(scan);
                    break;
                case 8:
                    System.out.println("Returning to menu...");
                    break;
                default:
                    System.out.println("Invalid option!");
            }

        } while (arrangementChoice != 8);
    }

    // ----------------------------------------------------
    // STEP 1: PROPOSE EXCHANGE METHOD
    // ----------------------------------------------------
    private void proposeExchangeMethod(Scanner scan) {
        System.out.println("\n--- PROPOSE EXCHANGE METHOD ---");

        if (isExchangeMethodAgreed()) {
            System.out.println(" Exchange method already agreed upon!");
            return;
        }

        String checkProposalSQL = "SELECT exchange_method FROM tbl_ManageRespond WHERE trade_id = ? AND trader_id = ?";

        try (Connection conn = con.connectDB();
                PreparedStatement checkStmt = conn.prepareStatement(checkProposalSQL)) {

            checkStmt.setInt(1, tradeId);
            checkStmt.setInt(2, traderId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String currentMethod = rs.getString("exchange_method");
                if (currentMethod != null) {
                    System.out.println(" You have already proposed: " + currentMethod.replace("_", " ").toUpperCase());
                    System.out.println(" Waiting for the other trader to respond...");
                    return;
                }
            }

            System.out.println("1. Meet Up");
            System.out.println("2. Delivery");
            System.out.print("Choose exchange method to propose (1-2): ");

            int method = scan.nextInt();
            scan.nextLine();

            if (method < 1 || method > 2) {
                System.out.println("Invalid choice! Please select 1 or 2.");
                return;
            }

            String exchangeMethod = (method == 1) ? "meet_up" : "delivery";

            String updateSQL = "UPDATE tbl_ManageRespond SET exchange_method = ?, exchange_method_agreed = 0 WHERE trade_id = ? AND trader_id = ?";
            con.updateRecord(updateSQL, exchangeMethod, tradeId, traderId);

            System.out.println(" Exchange method proposed: " + (method == 1 ? "Meet Up" : "Delivery"));
            System.out.println(" Waiting for the other trader to respond...");

            notifyOtherTraderAboutProposal(conn, exchangeMethod);

        } catch (Exception e) {
            System.out.println("Error proposing exchange method: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
    // STEP 2: RESPOND TO EXCHANGE METHOD PROPOSAL
    // ----------------------------------------------------
    private void respondToExchangeMethod(Scanner scan) {
        System.out.println("\n--- RESPOND TO EXCHANGE METHOD PROPOSAL ---");

        try (Connection conn = con.connectDB()) {
            String getProposalSQL = "SELECT mr.exchange_method, t.tbl_FullName "
                    + "FROM tbl_ManageRespond mr "
                    + "JOIN tbl_trader t ON mr.trader_id = t.trader_id "
                    + "WHERE mr.trade_id = ? AND mr.trader_id != ? AND mr.exchange_method IS NOT NULL AND mr.exchange_method_agreed = 0";

            PreparedStatement proposalStmt = conn.prepareStatement(getProposalSQL);
            proposalStmt.setInt(1, tradeId);
            proposalStmt.setInt(2, traderId);
            ResultSet proposalRs = proposalStmt.executeQuery();

            if (!proposalRs.next()) {
                System.out.println(" No pending exchange method proposals from the other trader.");
                return;
            }

            String proposedMethod = proposalRs.getString("exchange_method");
            String proposerName = proposalRs.getString("tbl_FullName");

            System.out.println(proposerName + " has proposed: " + proposedMethod.replace("_", " ").toUpperCase());
            System.out.println("\nDo you agree with this exchange method?");
            System.out.println("1. Yes, I agree");
            System.out.println("2. No, I want to propose a different method");
            System.out.print("Enter your choice (1-2): ");

            int response = scan.nextInt();
            scan.nextLine();

            if (response == 1) {
                String agreeSQL = "UPDATE tbl_ManageRespond SET exchange_method = ?, exchange_method_agreed = 1 WHERE trade_id = ? AND trader_id = ?";
                con.updateRecord(agreeSQL, proposedMethod, tradeId, traderId);

                String updateProposerSQL = "UPDATE tbl_ManageRespond SET exchange_method_agreed = 1 WHERE trade_id = ? AND trader_id != ?";
                con.updateRecord(updateProposerSQL, tradeId, traderId);

                System.out.println(" You have agreed to " + proposedMethod.replace("_", " ").toUpperCase());

                if ("delivery".equals(proposedMethod)) {
                    System.out.println(" Both traders agreed on Delivery! You can now proceed to set delivery details.");
                } else {
                    System.out.println(" Both traders agreed on Meet Up! You can proceed to set meetup details.");
                }

            } else if (response == 2) {
                String clearSQL = "UPDATE tbl_ManageRespond SET exchange_method = NULL, exchange_method_agreed = 0 WHERE trade_id = ?";
                con.updateRecord(clearSQL, tradeId);

                System.out.println(" Proposal declined. You can now propose a different exchange method.");

            } else {
                System.out.println("Invalid choice!");
            }

        } catch (Exception e) {
            System.out.println("Error responding to exchange method: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
    // NOTIFY OTHER TRADER ABOUT PROPOSAL
    // ----------------------------------------------------
    private void notifyOtherTraderAboutProposal(Connection conn, String exchangeMethod) throws SQLException {
        String otherTraderSQL = "SELECT trader_id FROM tbl_ManageRespond WHERE trade_id = ? AND trader_id != ?";
        PreparedStatement otherStmt = conn.prepareStatement(otherTraderSQL);
        otherStmt.setInt(1, tradeId);
        otherStmt.setInt(2, traderId);
        ResultSet otherRs = otherStmt.executeQuery();

        if (otherRs.next()) {
            int otherTraderId = otherRs.getInt("trader_id");

            String message = "I have proposed " + exchangeMethod.replace("_", " ") + " as our exchange method. Please respond in the 'Respond to Exchange Method' option.";
            String insertMsg = "INSERT INTO tbl_trade_messages (trade_id, sender_id, receiver_id, message_text, message_date) "
                    + "VALUES (?, ?, ?, ?, datetime('now'))";
            con.addRecord(insertMsg, tradeId, traderId, otherTraderId, message);
        }
    }

    // ----------------------------------------------------
    // STEP 3: SET DELIVERY DETAILS
    // ----------------------------------------------------
    private void setDeliveryDetails(Scanner scan) {
        System.out.println("\n--- SET DELIVERY DETAILS ---");

        if (!isExchangeMethodAgreed() || !isExchangeMethod("delivery")) {
            System.out.println(" Delivery details can only be set if both traders agree on 'Delivery' as exchange method.");
            return;
        }

        System.out.println("My Delivery Details:");
        System.out.println("=====================");

        System.out.print("My Delivery Address (where I want to receive items): ");
        String myAddress = scan.nextLine();

        System.out.print("My Courier Service: ");
        String myCourier = scan.nextLine();

        System.out.print("Expected Delivery Date: ");
        String expectedDate = scan.nextLine();

        System.out.print("My Tracking Number: ");
        String myTracking = scan.nextLine();

        System.out.print("Special Instructions: ");
        String instructions = scan.nextLine();

        try (Connection conn = con.connectDB()) {
            String sql = "UPDATE tbl_ManageRespond SET "
                    + "my_delivery_address = ?, my_courier_service = ?, "
                    + "expected_delivery_date = ?, my_tracking_number = ?, "
                    + "special_instructions = ?, details_confirmed = 0 "
                    + "WHERE trade_id = ? AND trader_id = ?";

            con.updateRecord(sql, myAddress, myCourier, expectedDate, myTracking, instructions, tradeId, traderId);
            System.out.println(" Your delivery details have been saved!");

        } catch (Exception e) {
            System.out.println("Error saving delivery details: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
    // STEP 4: SET MEETUP DETAILS
    // ----------------------------------------------------
    private void setMeetupDetails(Scanner scan) {
        System.out.println("\n--- SET MEETUP DETAILS ---");

        if (!isExchangeMethodAgreed() || !isExchangeMethod("meet_up")) {
            System.out.println(" Meetup details can only be set if both traders agree on 'Meet Up' as exchange method.");
            return;
        }

        System.out.println("My Meetup Details:");
        System.out.println("===================");

        System.out.print("Meetup Location: ");
        String meetupLocation = scan.nextLine();

        System.out.print("Meetup Date: ");
        String meetupDate = scan.nextLine();

        System.out.print("Meetup Time: ");
        String meetupTime = scan.nextLine();

        System.out.print("Contact Person: ");
        String contactPerson = scan.nextLine();

        System.out.print("Contact Number: ");
        String contactNumber = scan.nextLine();

        System.out.print("Special Instructions: ");
        String instructions = scan.nextLine();

        try (Connection conn = con.connectDB()) {
            String sql = "UPDATE tbl_ManageRespond SET "
                    + "meetup_location = ?, meetup_date = ?, meetup_time = ?, "
                    + "contact_person = ?, contact_number = ?, special_instructions = ?, details_confirmed = 0 "
                    + "WHERE trade_id = ? AND trader_id = ?";

            con.updateRecord(sql, meetupLocation, meetupDate, meetupTime, contactPerson, contactNumber, instructions, tradeId, traderId);
            System.out.println(" Your meetup details have been saved!");

        } catch (Exception e) {
            System.out.println("Error saving meetup details: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
    // STEP 5: REVIEW DELIVERY/MEETUP DETAILS
    // ----------------------------------------------------
    private void reviewDetails() {
        System.out.println("\n--- REVIEW DETAILS ---");

        try (Connection conn = con.connectDB()) {
            String myQuery = "SELECT * FROM tbl_ManageRespond WHERE trade_id = ? AND trader_id = ?";
            PreparedStatement myStmt = conn.prepareStatement(myQuery);
            myStmt.setInt(1, tradeId);
            myStmt.setInt(2, traderId);
            ResultSet myRs = myStmt.executeQuery();

            if (myRs.next()) {
                System.out.println("\nMY DETAILS:");
                System.out.println("============");
                System.out.println("Exchange Method: " + (myRs.getString("exchange_method") != null
                        ? myRs.getString("exchange_method").replace("_", " ").toUpperCase() : "Not set"));
                System.out.println("Exchange Method Agreed: " + (myRs.getInt("exchange_method_agreed") == 1 ? "[YES]" : "[NO]"));

                if ("delivery".equals(myRs.getString("exchange_method"))) {
                    System.out.println("My Delivery Address: " + (myRs.getString("my_delivery_address") != null
                            ? myRs.getString("my_delivery_address") : "Not set"));
                    System.out.println("My Courier Service: " + (myRs.getString("my_courier_service") != null
                            ? myRs.getString("my_courier_service") : "Not set"));
                    System.out.println("Expected Delivery Date: " + (myRs.getString("expected_delivery_date") != null
                            ? myRs.getString("expected_delivery_date") : "Not set"));
                    System.out.println("My Tracking Number: " + (myRs.getString("my_tracking_number") != null
                            ? myRs.getString("my_tracking_number") : "Not set"));
                } else if ("meet_up".equals(myRs.getString("exchange_method"))) {
                    System.out.println("Meetup Location: " + (myRs.getString("meetup_location") != null
                            ? myRs.getString("meetup_location") : "Not set"));
                    System.out.println("Meetup Date: " + (myRs.getString("meetup_date") != null
                            ? myRs.getString("meetup_date") : "Not set"));
                    System.out.println("Meetup Time: " + (myRs.getString("meetup_time") != null
                            ? myRs.getString("meetup_time") : "Not set"));
                    System.out.println("Contact Person: " + (myRs.getString("contact_person") != null
                            ? myRs.getString("contact_person") : "Not set"));
                    System.out.println("Contact Number: " + (myRs.getString("contact_number") != null
                            ? myRs.getString("contact_number") : "Not set"));
                }

                System.out.println("Special Instructions: " + (myRs.getString("special_instructions") != null
                        ? myRs.getString("special_instructions") : "Not set"));
                System.out.println("Details Confirmed: " + (myRs.getInt("details_confirmed") == 1 ? "[YES]" : "[NO]"));
            }

            String otherQuery = "SELECT mr.*, t.tbl_FullName FROM tbl_ManageRespond mr "
                    + "JOIN tbl_trader t ON mr.trader_id = t.trader_id "
                    + "WHERE mr.trade_id = ? AND mr.trader_id != ?";
            PreparedStatement otherStmt = conn.prepareStatement(otherQuery);
            otherStmt.setInt(1, tradeId);
            otherStmt.setInt(2, traderId);
            ResultSet otherRs = otherStmt.executeQuery();

            if (otherRs.next()) {
                System.out.println("\n" + otherRs.getString("tbl_FullName") + "'s DETAILS:");
                System.out.println("============");
                System.out.println("Exchange Method: " + (otherRs.getString("exchange_method") != null
                        ? otherRs.getString("exchange_method").replace("_", " ").toUpperCase() : "Not set"));
                System.out.println("Exchange Method Agreed: " + (otherRs.getInt("exchange_method_agreed") == 1 ? "[YES]" : "[NO]"));

                if ("delivery".equals(otherRs.getString("exchange_method"))) {
                    System.out.println("Their Delivery Address: " + (otherRs.getString("my_delivery_address") != null
                            ? otherRs.getString("my_delivery_address") : "Not set"));
                    System.out.println("Their Courier Service: " + (otherRs.getString("my_courier_service") != null
                            ? otherRs.getString("my_courier_service") : "Not set"));
                    System.out.println("Expected Delivery Date: " + (otherRs.getString("expected_delivery_date") != null
                            ? otherRs.getString("expected_delivery_date") : "Not set"));
                    System.out.println("Their Tracking Number: " + (otherRs.getString("my_tracking_number") != null
                            ? otherRs.getString("my_tracking_number") : "Not set"));
                } else if ("meet_up".equals(otherRs.getString("exchange_method"))) {
                    System.out.println("Meetup Location: " + (otherRs.getString("meetup_location") != null
                            ? otherRs.getString("meetup_location") : "Not set"));
                    System.out.println("Meetup Date: " + (otherRs.getString("meetup_date") != null
                            ? otherRs.getString("meetup_date") : "Not set"));
                    System.out.println("Meetup Time: " + (otherRs.getString("meetup_time") != null
                            ? otherRs.getString("meetup_time") : "Not set"));
                    System.out.println("Contact Person: " + (otherRs.getString("contact_person") != null
                            ? otherRs.getString("contact_person") : "Not set"));
                    System.out.println("Contact Number: " + (otherRs.getString("contact_number") != null
                            ? otherRs.getString("contact_number") : "Not set"));
                }

                System.out.println("Special Instructions: " + (otherRs.getString("special_instructions") != null
                        ? otherRs.getString("special_instructions") : "Not set"));
                System.out.println("Details Confirmed: " + (otherRs.getInt("details_confirmed") == 1 ? "[YES]" : "[NO]"));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving details: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
// CONFIRM ARRANGEMENTS
// ----------------------------------------------------
    private void confirmArrangements(Scanner scan) {
        System.out.println("\n--- CONFIRM ARRANGEMENTS ---");

        try (Connection conn = con.connectDB()) {
            if (!isExchangeMethodAgreed()) {
                System.out.println(" Both traders need to agree on an exchange method first.");
                return;
            }

            displayArrangementDetailsForConfirmation(conn);

            int choice = 0;
            boolean validInput = false;

            while (!validInput) {
                System.out.println("\nDo you confirm these arrangements?");
                System.out.println("1. Yes, I confirm and agree with these arrangements");
                System.out.println("2. No, I want to suggest different arrangements");
                System.out.print("Enter your choice (1-2): ");

                if (scan.hasNextInt()) {
                    choice = scan.nextInt();
                    scan.nextLine();

                    if (choice == 1 || choice == 2) {
                        validInput = true;
                    } else {
                        System.out.println("Invalid choice! Please enter 1 or 2.");
                    }
                } else {
                    System.out.println("Invalid input! Please enter a number (1 or 2).");
                    scan.next();
                }
            }

            if (choice == 1) {
                String updateSQL = "UPDATE tbl_ManageRespond SET details_confirmed = 1 WHERE trade_id = ? AND trader_id = ?";
                con.updateRecord(updateSQL, tradeId, traderId);
                System.out.println(" Your arrangements have been confirmed!");

                if (areDetailsConfirmed()) {
                    updateTradeStatus("arrangements_confirmed");
                    System.out.println(" Both traders have confirmed arrangements! You can now proceed with the exchange.");
                }

            } else if (choice == 2) {
                System.out.println(" You can modify your details and ask the other trader to confirm again.");

                if (isExchangeMethod("meet_up")) {
                    String suggestNew = "";
                    boolean validYesNo = false;

                    while (!validYesNo) {
                        System.out.print("Do you want to suggest a different meetup location? (yes/no): ");
                        suggestNew = scan.nextLine().trim().toLowerCase();

                        if (suggestNew.equals("yes") || suggestNew.equals("no")) {
                            validYesNo = true;
                        } else {
                            System.out.println("Invalid input! Please enter 'yes' or 'no'.");
                        }
                    }

                    if (suggestNew.equals("yes")) {
                        setMeetupDetails(scan);
                        String resetSQL = "UPDATE tbl_ManageRespond SET details_confirmed = 0 WHERE trade_id = ?";
                        con.updateRecord(resetSQL, tradeId);
                        System.out.println(" New meetup details set. Waiting for the other trader to confirm.");
                    }
                }

            }

        } catch (SQLException e) {
            System.out.println("Error confirming arrangements: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
    // DISPLAY ARRANGEMENT DETAILS FOR CONFIRMATION
    // ----------------------------------------------------
    private void displayArrangementDetailsForConfirmation(Connection conn) throws SQLException {
        System.out.println("\nCURRENT ARRANGEMENT DETAILS:");
        System.out.println("=============================");

        String detailsQuery = "SELECT mr.*, t.tbl_FullName FROM tbl_ManageRespond mr "
                + "JOIN tbl_trader t ON mr.trader_id = t.trader_id "
                + "WHERE mr.trade_id = ? ORDER BY mr.trader_id";

        PreparedStatement detailsStmt = conn.prepareStatement(detailsQuery);
        detailsStmt.setInt(1, tradeId);
        ResultSet detailsRs = detailsStmt.executeQuery();

        while (detailsRs.next()) {
            String traderName = detailsRs.getString("tbl_FullName");
            String exchangeMethod = detailsRs.getString("exchange_method");
            boolean isCurrentUser = detailsRs.getInt("trader_id") == traderId;

            System.out.println("\n" + (isCurrentUser ? "MY" : traderName + "'s") + " ARRANGEMENTS:");
            System.out.println("----------------------------------------");

            if ("delivery".equals(exchangeMethod)) {
                System.out.println("Exchange Method: DELIVERY");
                System.out.println("Delivery Address: " + (detailsRs.getString("my_delivery_address") != null
                        ? detailsRs.getString("my_delivery_address") : "Not set"));
                System.out.println("Courier Service: " + (detailsRs.getString("my_courier_service") != null
                        ? detailsRs.getString("my_courier_service") : "Not set"));
                System.out.println("Expected Date: " + (detailsRs.getString("expected_delivery_date") != null
                        ? detailsRs.getString("expected_delivery_date") : "Not set"));
                System.out.println("Tracking Number: " + (detailsRs.getString("my_tracking_number") != null
                        ? detailsRs.getString("my_tracking_number") : "Not set"));
            } else if ("meet_up".equals(exchangeMethod)) {
                System.out.println("Exchange Method: MEET UP");
                System.out.println("Meetup Location: " + (detailsRs.getString("meetup_location") != null
                        ? detailsRs.getString("meetup_location") : "Not set"));
                System.out.println("Meetup Date: " + (detailsRs.getString("meetup_date") != null
                        ? detailsRs.getString("meetup_date") : "Not set"));
                System.out.println("Meetup Time: " + (detailsRs.getString("meetup_time") != null
                        ? detailsRs.getString("meetup_time") : "Not set"));
                System.out.println("Contact Person: " + (detailsRs.getString("contact_person") != null
                        ? detailsRs.getString("contact_person") : "Not set"));
                System.out.println("Contact Number: " + (detailsRs.getString("contact_number") != null
                        ? detailsRs.getString("contact_number") : "Not set"));
            }

            System.out.println("Special Instructions: " + (detailsRs.getString("special_instructions") != null
                    ? detailsRs.getString("special_instructions") : "None"));
            System.out.println("Confirmed: " + (detailsRs.getInt("details_confirmed") == 1 ? "[YES]" : "[NO]"));
            System.out.println("----------------------------------------");
        }
    }

    // ----------------------------------------------------
// STEP 7: CONFIRM ITEM RECEIVED
// ----------------------------------------------------
    private void confirmItemReceived(Scanner scan) {
        System.out.println("\n--- CONFIRM ITEM RECEIVED ---");

        if (!isTradeStatus("arrangements_confirmed")) {
            System.out.println(" Both traders must confirm arrangements first before marking items as received.");
            return;
        }

        try (Connection conn = con.connectDB()) {
            String statusQuery = "SELECT mr.item_received, t.tbl_FullName, mr.exchange_method "
                    + "FROM tbl_ManageRespond mr "
                    + "JOIN tbl_trader t ON mr.trader_id = t.trader_id "
                    + "WHERE mr.trade_id = ?";

            PreparedStatement statusStmt = conn.prepareStatement(statusQuery);
            statusStmt.setInt(1, tradeId);
            ResultSet statusRs = statusStmt.executeQuery();

            System.out.println("\nCURRENT ITEM RECEIVED STATUS:");
            System.out.println("==============================");

            while (statusRs.next()) {
                String traderName = statusRs.getString("tbl_FullName");
                int itemReceived = statusRs.getInt("item_received");
                String exchangeMethod = statusRs.getString("exchange_method");
                boolean isCurrentUser = traderName.equals(getTraderName(conn, traderId));

                System.out.println((isCurrentUser ? "You" : traderName) + ": "
                        + (itemReceived == 1 ? "[RECEIVED]" : "[NOT RECEIVED]")
                        + " (" + exchangeMethod.replace("_", " ").toUpperCase() + ")");
            }

            String checkSQL = "SELECT item_received FROM tbl_ManageRespond WHERE trade_id = ? AND trader_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
            checkStmt.setInt(1, tradeId);
            checkStmt.setInt(2, traderId);
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next() && checkRs.getInt("item_received") == 1) {
                System.out.println("\n You have already confirmed that you received the item.");
                return;
            }

            String confirm = "";
            boolean validInput = false;

            while (!validInput) {
                System.out.print("\nHave you received the item from the other trader? (yes/no): ");
                confirm = scan.nextLine().trim().toLowerCase();

                if (confirm.equals("yes") || confirm.equals("no")) {
                    validInput = true;
                } else {
                    System.out.println("Invalid input! Please enter 'yes' or 'no'.");
                }
            }

            if (confirm.equals("yes")) {
                String updateSQL = "UPDATE tbl_ManageRespond SET item_received = 1 WHERE trade_id = ? AND trader_id = ?";
                con.updateRecord(updateSQL, tradeId, traderId);
                System.out.println(" Item received confirmed! Waiting for the other trader to confirm.");

                if (areItemsReceived()) {
                    updateTradeStatus("completed");
                    moveTradeToHistory(conn);
                    System.out.println(" Both traders have received their items! Trade completed successfully!");
                }

            } else {
                System.out.println(" Item receipt not confirmed. Please confirm when you receive the item.");
            }

        } catch (SQLException e) {
            System.out.println("Error confirming item received: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
// HELPER: GET TRADER NAME
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
    // 3. VIEW TRADE STATUS
    // ----------------------------------------------------
    private void viewTradeStatus() {
        System.out.println("\n--- TRADE STATUS ---");

        try (Connection conn = con.connectDB()) {
            String tradeQuery = "SELECT tr.trade_status, ot.tbl_FullName AS other_trader "
                    + "FROM tbl_trade tr "
                    + "JOIN tbl_trader ot ON (tr.offer_trader_id = ? AND tr.target_trader_id = ot.trader_id) OR "
                    + "(tr.target_trader_id = ? AND tr.offer_trader_id = ot.trader_id) "
                    + "WHERE tr.trade_id = ?";
            PreparedStatement tradeStmt = conn.prepareStatement(tradeQuery);
            tradeStmt.setInt(1, traderId);
            tradeStmt.setInt(2, traderId);
            tradeStmt.setInt(3, tradeId);
            ResultSet tradeRs = tradeStmt.executeQuery();

            if (tradeRs.next()) {
                System.out.println("Trade ID: " + tradeId);
                System.out.println("Trading with: " + tradeRs.getString("other_trader"));
                System.out.println("Overall Status: " + tradeRs.getString("trade_status").toUpperCase());
                System.out.println("\nPROGRESS:");

                String myQuery = "SELECT details_confirmed, item_received, exchange_method, exchange_method_agreed FROM tbl_ManageRespond WHERE trade_id = ? AND trader_id = ?";
                PreparedStatement myStmt = conn.prepareStatement(myQuery);
                myStmt.setInt(1, tradeId);
                myStmt.setInt(2, traderId);
                ResultSet myRs = myStmt.executeQuery();

                if (myRs.next()) {
                    System.out.println("\nMY STATUS:");
                    System.out.println((myRs.getInt("exchange_method_agreed") == 1 ? "[YES]" : "[NO]") + " Exchange Method Agreed");
                    System.out.println((myRs.getInt("details_confirmed") == 1 ? "[YES]" : "[NO]") + " Details Confirmed");
                    System.out.println((myRs.getInt("item_received") == 1 ? "[YES]" : "[NO]") + " Item Received");
                }

                String otherQuery = "SELECT details_confirmed, item_received, exchange_method_agreed FROM tbl_ManageRespond WHERE trade_id = ? AND trader_id != ?";
                PreparedStatement otherStmt = conn.prepareStatement(otherQuery);
                otherStmt.setInt(1, tradeId);
                otherStmt.setInt(2, traderId);
                ResultSet otherRs = otherStmt.executeQuery();

                if (otherRs.next()) {
                    System.out.println("\n" + tradeRs.getString("other_trader") + "'s STATUS:");
                    System.out.println((otherRs.getInt("exchange_method_agreed") == 1 ? "[YES]" : "[NO]") + " Exchange Method Agreed");
                    System.out.println((otherRs.getInt("details_confirmed") == 1 ? "[YES]" : "[NO]") + " Details Confirmed");
                    System.out.println((otherRs.getInt("item_received") == 1 ? "[YES]" : "[NO]") + " Item Received");
                }

                System.out.println("\nNEXT STEPS:");
                if (areItemsReceived()) {
                    System.out.println("Trade Completed! Both items have been received.");
                    updateTradeStatus("completed");
                    moveTradeToHistory(conn);
                } else if (areDetailsConfirmed()) {
                    System.out.println(" Proceed with item exchange and mark as received when you get the item.");
                } else if (isExchangeMethodAgreed()) {
                    System.out.println(" Complete arrangement details and confirm with the other trader.");
                } else {
                    System.out.println(" Agree on an exchange method with the other trader.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error viewing trade status: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
    // 4. SEND MESSAGE TO TRADER
    // ----------------------------------------------------
    private void sendTradeMessage(Scanner scan) {
        System.out.println("\n--- SEND MESSAGE TO TRADER ---");

        try (Connection conn = con.connectDB()) {
            String traderQuery = "SELECT ot.trader_id, ot.tbl_FullName "
                    + "FROM tbl_trade tr "
                    + "JOIN tbl_trader ot ON (tr.offer_trader_id = ? AND tr.target_trader_id = ot.trader_id) OR "
                    + "                     (tr.target_trader_id = ? AND tr.offer_trader_id = ot.trader_id) "
                    + "WHERE tr.trade_id = ?";
            PreparedStatement traderStmt = conn.prepareStatement(traderQuery);
            traderStmt.setInt(1, traderId);
            traderStmt.setInt(2, traderId);
            traderStmt.setInt(3, tradeId);
            ResultSet traderRs = traderStmt.executeQuery();

            if (traderRs.next()) {
                int receiverId = traderRs.getInt("trader_id");
                String receiverName = traderRs.getString("tbl_FullName");

                System.out.println("Sending message to: " + receiverName);
                System.out.print("Enter your message: ");
                String message = scan.nextLine();

                if (message.trim().isEmpty()) {
                    System.out.println("Message cannot be empty.");
                    return;
                }

                String insertMsg = "INSERT INTO tbl_trade_messages (trade_id, sender_id, receiver_id, message_text, message_date) "
                        + "VALUES (?, ?, ?, ?, datetime('now'))";
                con.addRecord(insertMsg, tradeId, traderId, receiverId, message);
                System.out.println(" Message sent successfully!");

            } else {
                System.out.println("Error: Could not find the other trader.");
            }

        } catch (SQLException e) {
            System.out.println("Error sending message: " + e.getMessage());
        }
    }

    // ----------------------------------------------------
    // HELPER METHODS
    // ----------------------------------------------------
    private boolean isTradeStatus(String status) {
        try (Connection conn = con.connectDB()) {
            String sql = "SELECT COUNT(*) FROM tbl_trade WHERE trade_id = ? AND trade_status = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, tradeId);
            pstmt.setString(2, status);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean isExchangeMethod(String method) {
        try (Connection conn = con.connectDB()) {
            String sql = "SELECT COUNT(*) FROM tbl_ManageRespond WHERE trade_id = ? AND trader_id = ? AND exchange_method = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, tradeId);
            pstmt.setInt(2, traderId);
            pstmt.setString(3, method);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean isExchangeMethodAgreed() {
        try (Connection conn = con.connectDB()) {
            String sql = "SELECT COUNT(*) FROM tbl_ManageRespond WHERE trade_id = ? AND exchange_method_agreed = 1";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, tradeId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) == 2;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean areDetailsConfirmed() {
        try (Connection conn = con.connectDB()) {
            String sql = "SELECT COUNT(*) FROM tbl_ManageRespond WHERE trade_id = ? AND details_confirmed = 1";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, tradeId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) == 2;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean areItemsReceived() {
        try (Connection conn = con.connectDB()) {
            String sql = "SELECT COUNT(*) FROM tbl_ManageRespond WHERE trade_id = ? AND item_received = 1";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, tradeId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) == 2;
        } catch (SQLException e) {
            return false;
        }
    }

    private void updateTradeStatus(String status) {
        try (Connection conn = con.connectDB()) {
            String sql = "UPDATE tbl_trade SET trade_status = ? WHERE trade_id = ?";
            con.updateRecord(sql, status, tradeId);
        } catch (Exception e) {
            System.out.println("Error updating trade status: " + e.getMessage());
        }
    }

    private void moveTradeToHistory(Connection conn) throws SQLException {
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

                System.out.println(" Trade completed and moved to history!");
            }
        }
    }
}
