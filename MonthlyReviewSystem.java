package Collector;

import java.sql.*;
import java.util.Scanner;

public class MonthlyReviewSystem {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/monthly_review";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Root@1234";

    private static final String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS users (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "username VARCHAR(50) NOT NULL," +
            "password VARCHAR(50) NOT NULL," +
            "role VARCHAR(10) NOT NULL)";

    private static final String CREATE_QUERY_TABLE = "CREATE TABLE IF NOT EXISTS queries (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "user_id INT NOT NULL," +
            "query_text VARCHAR(255) NOT NULL," +
            "status VARCHAR(20) NOT NULL DEFAULT 'Pending'," +
            "reply_text VARCHAR(255)," +
            "FOREIGN KEY (user_id) REFERENCES users(id))";

    public static void main(String[] args) {

        // Create database and tables
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_USER_TABLE);
            statement.executeUpdate(CREATE_QUERY_TABLE);
            System.out.println("Database connection Established");
        } catch (SQLException e) {
            System.out.println("Database connection Failed!!");
            e.printStackTrace();
            return;
        }

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("******************************************************");
            System.out.println("\tWelcome to collector Monthly Review System");
            System.out.println("******************************************************\n");
            System.out.println("1)Register as Admin.");
            System.out.println("2)Register as User.");
            System.out.println("3)Login to the System.");
            System.out.println("4)Exit.\n");

            int option = sc.nextInt();

            switch (option) {
                case 1:
                    registerUser(sc, "Admin");
                    break;
                case 2:
                    registerUser(sc, "User");
                    break;
                case 3:
                    loginUser(sc);
                    break;
                case 4:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void registerUser(Scanner sc, String role) {
        System.out.println("\nEnter username:");
        String username = sc.next();

        System.out.println("\nEnter password:");
        String password = sc.next();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, role);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("\nUser registered successfully.\n");
    }

    private static void loginUser(Scanner sc) {
        System.out.println("\nEnter username:");
        String username = sc.next();

        System.out.println("\nEnter password:");
        String password = sc.next();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, role FROM users WHERE username = ? AND password = ?")) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                int userId = resultSet.getInt("id");
                String role = resultSet.getString("role");

                if (role.equals("Admin")) {
                    adminPage(sc, userId);
                } else {
                    userPage(sc, userId);
                }
            } else {
                System.out.println("\nInvalid username or password.\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    private static void adminPage(Scanner sc, int userId) {
        while (true) {
        	System.out.println("******************************************************");
            System.out.println("\tWelcome Admin :");
            System.out.println("******************************************************\n");
            System.out.println("1)view Query.");
            System.out.println("2)Reply to Query.");
            System.out.println("3)Update Current status of the Query.");
            System.out.println("4)Exit\n");

            int option = sc.nextInt();

            switch (option) {
                case 1:
                    viewQueries(userId);
                    break;
                case 2:
                    replyToQuery(sc, userId);
                    break;
                case 3:
                    updateQueryStatus(sc, userId);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void userPage(Scanner sc, int userId) {
        while (true) {
        	System.out.println("******************************************************");
            System.out.println("\tWelcome to User Page");
            System.out.println("******************************************************\n");
            System.out.println("1)Add Query.");
            System.out.println("2)Delete Query.");
            System.out.println("3)Edit Query.");
            System.out.println("4)View Query Status.");
            System.out.println("5)Exit\n");

            int option = sc.nextInt();

            switch (option) {
                case 1:
                    addQuery(sc, userId);
                    break;
                case 2:
                    deleteQuery(sc, userId);
                    break;
                case 3:
                    editQuery(sc, userId);
                    break;
                case 4:
                	viewQueriesStatus(userId);
                	break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void viewQueries(int adminId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT q.id, u.username, q.query_text, q.status, q.reply_text " +
                     "FROM queries q " +
                     "JOIN users u ON q.user_id = u.id " +
                     "WHERE q.status != 'Deleted'")) {
            ResultSet resultSet = statement.executeQuery();
            
            String queryText =null;
            while (resultSet.next()) {
                int queryId = resultSet.getInt("id");
                String username = resultSet.getString("username");
                queryText = resultSet.getString("query_text");
                String status = resultSet.getString("status");
                String replyText = resultSet.getString("reply_text");

                System.out.println("\nQuery ID: " + queryId);
                System.out.println("Username: " + username);
                System.out.println("Query Text: " + queryText);
                System.out.println("Status: " + status);
                System.out.println("Reply Text: " + (replyText != null ? replyText : "N/A"));
            }
            if(queryText ==null) {
            	System.out.println("\nNo queries found.\n");
            /*if (!resultSet.next()) {
                System.out.println("\nNo queries found.\n");*/
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

		private static void replyToQuery(Scanner sc, int adminId) {
		        System.out.println("\nEnter query ID:");
		    int queryId = sc.nextInt();
		
		    sc.nextLine(); // Consume newline
		
		    System.out.println("\nEnter reply text:");
		    String replyText = sc.nextLine();
		
		    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		         PreparedStatement statement = connection.prepareStatement(
		                 "UPDATE queries SET status = 'Replied', reply_text = ? WHERE id = ?")) {
		        statement.setString(1, replyText);
		        statement.setInt(2, queryId);
		
		        int rowsAffected = statement.executeUpdate();
		
		        if (rowsAffected > 0) {
		            System.out.println("\nQuery replied successfully.\n");
		        } else {
		            System.out.println("\nInvalid query ID.\n");
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
		}
		
		private static void updateQueryStatus(Scanner sc, int adminId) {
		    System.out.println("\nEnter query ID:");
		    int queryId = sc.nextInt();
		
		    sc.nextLine(); // Consume newline
		
		    System.out.println("\nEnter new status (Open, Replied, Closed, Deleted):");
		    String status = sc.nextLine();
		
		    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		         PreparedStatement statement = connection.prepareStatement(
		                 "UPDATE queries SET status = ? WHERE id = ?")) {
		        statement.setString(1, status);
		        statement.setInt(2, queryId);
		
		        int rowsAffected = statement.executeUpdate();
		
		        if (rowsAffected > 0) {
		            System.out.println("\nQuery status updated successfully.\n");
		        } else {
		            System.out.println("\nInvalid query ID.\n");
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
		}
		
		private static void addQuery(Scanner sc, int userId) {
		    sc.nextLine(); // Consume newline
		
		    System.out.println("\nEnter query text:");
		    String queryText = sc.nextLine();
		
		    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		         PreparedStatement statement = connection.prepareStatement(
		                 "INSERT INTO queries (user_id, query_text) VALUES (?, ?)")) {
		        statement.setInt(1, userId);
		        statement.setString(2, queryText);
		
		        int rowsAffected = statement.executeUpdate();
		
		        if (rowsAffected > 0) {
		            System.out.println("\nQuery added successfully.\n");
		        } else {
		            System.out.println("\nFailed to add query.\n");
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
		}
		
		private static void deleteQuery(Scanner sc, int userId) {
		    System.out.println("\nEnter query ID:");
		    int queryId = sc.nextInt();
		
		    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		         PreparedStatement statement = connection.prepareStatement(
		                 "UPDATE queries SET status = 'Deleted' WHERE id = ? AND user_id = ?")) {
		        statement.setInt(1, queryId);
		        statement.setInt(2, userId);
		
		        int rowsAffected = statement.executeUpdate();
		
		        if (rowsAffected > 0) {
		            System.out.println("\nQuery deleted successfully.\n");
		        } else {
		            System.out.println("\nInvalid query ID or you do not have permission to delete this query.\n");
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
		}
		
		private static void editQuery(Scanner sc, int userId) {
		    System.out.println("\nEnter query ID:");
		    int queryId = sc.nextInt();
		
		    sc.nextLine(); // Consume newline
		
		    System.out.println("\nEnter new query text:");
		   
		    String newQueryText = sc.nextLine();
		
		    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		         PreparedStatement statement = connection.prepareStatement(
		                 "UPDATE queries SET query_text = ? WHERE id = ? AND user_id = ?")) {
		        statement.setString(1, newQueryText);
		        statement.setInt(2, queryId);
		        statement.setInt(3, userId);
		
		        int rowsAffected = statement.executeUpdate();
		
		        if (rowsAffected > 0) {
		            System.out.println("\nQuery edited successfully.\n");
		        } else {
		            System.out.println("\nInvalid query ID or you do not have permission to edit this query.\n");
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
		}
		    private static void viewQueriesStatus(int userId) {
		        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		             PreparedStatement statement = connection.prepareStatement(
		                     "SELECT q.status, q.reply_text " +
		                     "FROM queries q " +
		                     "JOIN users u ON q.user_id = u.id " +
		                     "WHERE q.status != 'Deleted'")) {
		            ResultSet resultSet = statement.executeQuery();
		
		            while (resultSet.next()) {            		                
		                String status = resultSet.getString("status");
		                String replyText = resultSet.getString("reply_text");
		               
		                System.out.println("Status: " + status);
		                System.out.println("Reply Text: " + (replyText != null ? replyText : "N/A"));
		            		                }
		
		            		                
		        } catch (SQLException e) {
		        	e.printStackTrace();
		        }
		    }
}

