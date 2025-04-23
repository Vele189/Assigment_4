package com.northwind.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.northwind.db.DatabaseConnection;
import com.northwind.model.Customer;

public class CustomersTab extends Tab {

    private TableView<Customer> customerTable;
    private TextField searchField;
    private Button searchButton;
    private Button viewAllButton;

    public CustomersTab() {
        setText("Customers");

        // Create the main layout for this tab
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));

        // Create search controls
        HBox searchBox = createSearchBox();

        // Create customer table
        customerTable = createCustomerTable();

        // Add components to the layout
        borderPane.setTop(searchBox);
        borderPane.setCenter(customerTable);

        // Set the content of the tab
        setContent(borderPane);

        // Load initial data
        loadCustomers();
    }

    private HBox createSearchBox() {
        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(10));

        Label searchLabel = new Label("Search Customers:");
        searchField = new TextField();
        searchField.setPromptText("Enter customer name");
        searchField.setPrefWidth(200);

        searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchCustomers());

        viewAllButton = new Button("View All");
        viewAllButton.setOnAction(e -> loadCustomers());

        searchBox.getChildren().addAll(searchLabel, searchField, searchButton, viewAllButton);

        return searchBox;
    }

    private TableView<Customer> createCustomerTable() {
        TableView<Customer> table = new TableView<>();

        // Create columns
        TableColumn<Customer, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Customer, String> companyColumn = new TableColumn<>("Company");
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("company"));
        companyColumn.setPrefWidth(150);

        TableColumn<Customer, String> contactNameColumn = new TableColumn<>("Contact Name");
        contactNameColumn.setCellValueFactory(new PropertyValueFactory<>("contactName"));
        contactNameColumn.setPrefWidth(150);

        TableColumn<Customer, String> contactTitleColumn = new TableColumn<>("Contact Title");
        contactTitleColumn.setCellValueFactory(new PropertyValueFactory<>("contactTitle"));
        contactTitleColumn.setPrefWidth(150);

        TableColumn<Customer, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneColumn.setPrefWidth(120);

        // Add columns to table
        table.getColumns().addAll(idColumn, companyColumn, contactNameColumn, contactTitleColumn, phoneColumn);

        return table;
    }

    private void loadCustomers() {
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        System.out.println("Loading customers...");

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert("Database Error", "Could not establish database connection. Please check your database settings.");
                return;
            }

            // First check if the customers table has any data
            String countQuery = "SELECT COUNT(*) FROM customers";
            PreparedStatement countStmt = conn.prepareStatement(countQuery);
            ResultSet countRs = countStmt.executeQuery();
            int customerCount = 0;
            if (countRs.next()) {
                customerCount = countRs.getInt(1);
            }
            countRs.close();
            countStmt.close();

            System.out.println("Found " + customerCount + " customers in database");

            // If no customers exist, create some sample data
            if (customerCount == 0) {
                System.out.println("Creating sample customer data...");
                createSampleCustomers(conn);
            }

            String query = "SELECT id, company, last_name, first_name, job_title, business_phone FROM customers";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Customer customer = new Customer(
                    rs.getInt("id"),
                    rs.getString("company"),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("job_title"),
                    rs.getString("business_phone")
                );
                customerList.add(customer);
                System.out.println("Loaded customer: " + customer.getCompany());
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading customers: " + e.getMessage());
        }

        System.out.println("Setting " + customerList.size() + " customers to table");
        customerTable.setItems(customerList);
    }

    private void searchCustomers() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            loadCustomers();
            return;
        }

        ObservableList<Customer> customerList = FXCollections.observableArrayList();

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert("Database Error", "Could not establish database connection. Please check your database settings.");
                return;
            }

            String query = "SELECT id, company, last_name, first_name, job_title, business_phone FROM customers " +
                          "WHERE company LIKE ? OR last_name LIKE ? OR first_name LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(query);

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Customer customer = new Customer(
                    rs.getInt("id"),
                    rs.getString("company"),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("job_title"),
                    rs.getString("business_phone")
                );
                customerList.add(customer);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error searching customers: " + e.getMessage());
        }

        customerTable.setItems(customerList);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void createSampleCustomers(Connection conn) {
        try {
            String insertQuery = "INSERT INTO customers (company, last_name, first_name, job_title, business_phone) VALUES " +
                               "('Northwind Traders', 'Freehafer', 'Nancy', 'Sales Representative', '(123)555-0100'), " +
                               "('Company A', 'Cencini', 'Andrew', 'Vice President, Sales', '(123)555-0100'), " +
                               "('Company B', 'Kotas', 'Jan', 'Sales Representative', '(123)555-0100'), " +
                               "('Company C', 'Sergienko', 'Mariya', 'Sales Representative', '(123)555-0100'), " +
                               "('Company D', 'Thorpe', 'Steven', 'Sales Manager', '(123)555-0100'), " +
                               "('Company E', 'Neipper', 'Michael', 'Sales Representative', '(123)555-0100'), " +
                               "('Company F', 'Zare', 'Robert', 'Sales Representative', '(123)555-0100'), " +
                               "('Company G', 'Giussani', 'Laura', 'Sales Coordinator', '(123)555-0100'), " +
                               "('Company H', 'Hellung-Larsen', 'Anne', 'Sales Representative', '(123)555-0100'), " +
                               "('Company I', 'Litton', 'Tim', 'Sales Representative', '(123)555-0100')";

            PreparedStatement stmt = conn.prepareStatement(insertQuery);
            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            System.out.println("Created " + rowsAffected + " sample customers");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error creating sample customers: " + e.getMessage());
        }
    }
}
