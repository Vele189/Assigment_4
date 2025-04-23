package com.northwind.ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.northwind.db.DatabaseConnection;
import com.northwind.model.Employee;
import com.northwind.model.Customer;
import com.northwind.model.Product;
import com.northwind.model.Order;

public class MainWindow {
    
    private Stage stage;
    private TabPane tabPane;
    private TableView<Customer> customersTable;
    private TableView<Product> productsTable;
    private TableView<Order> ordersTable;
    
    public MainWindow(Stage stage) {
        this.stage = stage;
        initialize();
    }
    
    private void initialize() {
        // Create main layout
        BorderPane root = new BorderPane();
        
        // Create tab pane
        tabPane = new TabPane();
        
        // Create tabs
        Tab customersTab = new Tab("Customers");
        customersTab.setClosable(false);
        
        Tab productsTab = new Tab("Products");
        productsTab.setClosable(false);
        
        Tab ordersTab = new Tab("Orders");
        ordersTab.setClosable(false);
        
        Tab reportsTab = new Tab("Reports");
        reportsTab.setClosable(false);
        
        // Add tabs to tab pane
        tabPane.getTabs().addAll(customersTab, productsTab, ordersTab, reportsTab);
        
        // Setup tab content
        setupCustomersTab(customersTab);
        setupProductsTab(productsTab);
        setupOrdersTab(ordersTab);
        setupReportsTab(reportsTab);
        setupEmployeesTab();
        
        // Add tab pane to root
        root.setCenter(tabPane);
        
        // Create scene
        Scene scene = new Scene(root, 800, 600);
        
        // Setup stage
        stage.setTitle("Northwind Database Application");
        stage.setScene(scene);
    }
    
    private void setupCustomersTab(Tab tab) {
        // Create customers tab content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Add search functionality
        HBox searchBox = new HBox(10);
        Label searchLabel = new Label("Search Customers:");
        TextField searchField = new TextField();
        searchField.setPromptText("Enter customer name");
        Button searchButton = new Button("Search");
        Button viewAllButton = new Button("View All");
        searchBox.getChildren().addAll(searchLabel, searchField, searchButton, viewAllButton);
        
        // Create customers table
        customersTable = new TableView<>();
        
        // Add table columns
        TableColumn<Customer, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Customer, String> companyColumn = new TableColumn<>("Company");
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("company"));
        
        TableColumn<Customer, String> contactNameColumn = new TableColumn<>("Contact Name");
        contactNameColumn.setCellValueFactory(new PropertyValueFactory<>("contactName"));
        
        TableColumn<Customer, String> contactTitleColumn = new TableColumn<>("Contact Title");
        contactTitleColumn.setCellValueFactory(new PropertyValueFactory<>("contactTitle"));
        
        TableColumn<Customer, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        customersTable.getColumns().addAll(idColumn, companyColumn, contactNameColumn, contactTitleColumn, phoneColumn);
        
        // Add components to content
        content.getChildren().addAll(searchBox, customersTable);
        
        // Set tab content
        tab.setContent(content);
        
        // Load customer data
        // loadCustomerData();
        
        // Setup search functionality
        searchButton.setOnAction(e -> {
            // searchCustomers(searchField.getText());
        });
        
        viewAllButton.setOnAction(e -> {
            // loadCustomerData();
        });
    }
    
    private void setupProductsTab(Tab tab) {
        // Create products tab content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Create products table
        productsTable = new TableView<>();
        
        // Add components to content
        content.getChildren().add(productsTable);
        
        // Set tab content
        tab.setContent(content);
        
        // Load product data
        // loadProductData();
    }
    
    private void setupOrdersTab(Tab tab) {
        // Create orders tab content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Create orders table
        ordersTable = new TableView<>();
        
        // Add components to content
        content.getChildren().add(ordersTable);
        
        // Set tab content
        tab.setContent(content);
        
        // Load order data
        // loadOrderData();
    }
    
    private void setupReportsTab(Tab tab) {
        // Create reports tab content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Add report generation controls
        
        // Set tab content
        tab.setContent(content);
    }
    
    private void setupEmployeesTab() {
        Tab employeesTab = new Tab("Employees");
        employeesTab.setClosable(false);
        tabPane.getTabs().add(employeesTab);

        // Create the employees tab content
        VBox employeesContent = new VBox(10);
        employeesContent.setPadding(new Insets(10));

        // Add filter textbox
        HBox filterBox = new HBox(10);
        Label filterLabel = new Label("Filter Employees:");
        TextField filterField = new TextField();
        filterField.setPromptText("Enter name or city");
        Button filterButton = new Button("Filter");
        filterBox.getChildren().addAll(filterLabel, filterField, filterButton);

        // Create table for employees
        TableView<Employee> employeesTable = new TableView<>();
        
        // Add columns for required employee details
        TableColumn<Employee, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<Employee, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<Employee, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<Employee, String> address2Col = new TableColumn<>("Address Line 2");
        address2Col.setCellValueFactory(new PropertyValueFactory<>("addressLine2"));

        TableColumn<Employee, String> cityCol = new TableColumn<>("City");
        cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));

        TableColumn<Employee, String> regionCol = new TableColumn<>("Region");
        regionCol.setCellValueFactory(new PropertyValueFactory<>("region"));

        TableColumn<Employee, String> postalCodeCol = new TableColumn<>("Postal Code");
        postalCodeCol.setCellValueFactory(new PropertyValueFactory<>("postalCode"));

        TableColumn<Employee, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Employee, String> officeCol = new TableColumn<>("Office");
        officeCol.setCellValueFactory(new PropertyValueFactory<>("office"));

        TableColumn<Employee, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));

        employeesTable.getColumns().addAll(
            firstNameCol, lastNameCol, addressCol, address2Col, cityCol, 
            regionCol, postalCodeCol, phoneCol, officeCol, activeCol
        );

        // Add components to the employees tab
        employeesContent.getChildren().addAll(filterBox, employeesTable);
        employeesTab.setContent(employeesContent);

        // Load employee data
        loadEmployeeData(employeesTable);

        // Filter functionality
        filterButton.setOnAction(e -> {
            String filterText = filterField.getText().toLowerCase();
            filterEmployeeData(employeesTable, filterText);
        });
    }
    
    private void loadEmployeeData(TableView<Employee> table) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                String query = "SELECT * FROM employees";
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                
                ObservableList<Employee> employees = FXCollections.observableArrayList();
                
                while (rs.next()) {
                    Employee employee = new Employee(
                        rs.getInt("EmployeeID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Address"),
                        rs.getString("Address2"),
                        rs.getString("City"),
                        rs.getString("Region"),
                        rs.getString("PostalCode"),
                        rs.getString("HomePhone"),
                        rs.getString("Office"),
                        rs.getBoolean("Active")
                    );
                    employees.add(employee);
                }
                
                table.setItems(employees);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load employee data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterEmployeeData(TableView<Employee> table, String filter) {
        ObservableList<Employee> allEmployees = table.getItems();
        ObservableList<Employee> filteredEmployees = FXCollections.observableArrayList();
        
        for (Employee employee : allEmployees) {
            if (employee.getFirstName().toLowerCase().contains(filter) ||
                employee.getLastName().toLowerCase().contains(filter) ||
                employee.getCity().toLowerCase().contains(filter)) {
                filteredEmployees.add(employee);
            }
        }
        
        table.setItems(filteredEmployees);
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void show() {
        stage.show();
    }
}