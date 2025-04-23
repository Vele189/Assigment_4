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
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.northwind.db.DatabaseConnection;
import com.northwind.model.Order;
import com.northwind.model.OrderDetail;

public class OrdersTab extends Tab {

    private TableView<Order> orderTable;
    private TableView<OrderDetail> orderDetailsTable;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Button searchButton;
    private Button viewAllButton;

    public OrdersTab() {
        setText("Orders");

        // Create the main layout for this tab
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));

        // Create search controls
        HBox searchBox = createSearchBox();

        // Create order table
        orderTable = createOrderTable();

        // Create order details table
        orderDetailsTable = createOrderDetailsTable();

        // Create a split pane for orders and order details
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitPane.getItems().addAll(orderTable, orderDetailsTable);
        splitPane.setDividerPositions(0.6);

        // Add components to the layout
        borderPane.setTop(searchBox);
        borderPane.setCenter(splitPane);

        // Set the content of the tab
        setContent(borderPane);

        // Set up event handler for order selection
        orderTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    loadOrderDetails(newValue.getId());
                }
            }
        );

        // Load initial data
        loadOrders();
    }

    private HBox createSearchBox() {
        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(10));

        Label startDateLabel = new Label("Start Date:");
        startDatePicker = new DatePicker();

        Label endDateLabel = new Label("End Date:");
        endDatePicker = new DatePicker();

        searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchOrdersByDate());

        viewAllButton = new Button("View All");
        viewAllButton.setOnAction(e -> loadOrders());

        searchBox.getChildren().addAll(startDateLabel, startDatePicker, endDateLabel, endDatePicker, searchButton, viewAllButton);

        return searchBox;
    }

    private TableView<Order> createOrderTable() {
        TableView<Order> table = new TableView<>();

        // Create columns
        TableColumn<Order, Integer> idColumn = new TableColumn<>("Order ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Order, String> customerColumn = new TableColumn<>("Customer");
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerColumn.setPrefWidth(200);

        TableColumn<Order, LocalDate> orderDateColumn = new TableColumn<>("Order Date");
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        orderDateColumn.setPrefWidth(120);

        TableColumn<Order, LocalDate> shippedDateColumn = new TableColumn<>("Shipped Date");
        shippedDateColumn.setCellValueFactory(new PropertyValueFactory<>("shippedDate"));
        shippedDateColumn.setPrefWidth(120);

        TableColumn<Order, Double> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalColumn.setPrefWidth(100);

        // Add columns to table
        table.getColumns().addAll(idColumn, customerColumn, orderDateColumn, shippedDateColumn, totalColumn);

        return table;
    }

    private TableView<OrderDetail> createOrderDetailsTable() {
        TableView<OrderDetail> table = new TableView<>();

        // Create columns
        TableColumn<OrderDetail, Integer> orderIdColumn = new TableColumn<>("Order ID");
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        TableColumn<OrderDetail, String> productColumn = new TableColumn<>("Product");
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productColumn.setPrefWidth(200);

        TableColumn<OrderDetail, Double> unitPriceColumn = new TableColumn<>("Unit Price");
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        unitPriceColumn.setPrefWidth(100);

        TableColumn<OrderDetail, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setPrefWidth(80);

        TableColumn<OrderDetail, Double> discountColumn = new TableColumn<>("Discount");
        discountColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
        discountColumn.setPrefWidth(80);

        TableColumn<OrderDetail, Double> subtotalColumn = new TableColumn<>("Subtotal");
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        subtotalColumn.setPrefWidth(100);

        // Add columns to table
        table.getColumns().addAll(orderIdColumn, productColumn, unitPriceColumn, quantityColumn, discountColumn, subtotalColumn);

        return table;
    }

    private void loadOrders() {
        // Clear existing data
        orderTable.getItems().clear();
        orderDetailsTable.getItems().clear();

        ObservableList<Order> orderList = FXCollections.observableArrayList();
        System.out.println("Loading orders...");

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert("Database Error", "Could not establish database connection. Please check your database settings.");
                return;
            }

            // First check if the orders table has any data
            String countQuery = "SELECT COUNT(*) FROM orders";
            PreparedStatement countStmt = conn.prepareStatement(countQuery);
            ResultSet countRs = countStmt.executeQuery();
            int orderCount = 0;
            if (countRs.next()) {
                orderCount = countRs.getInt(1);
            }
            countRs.close();
            countStmt.close();

            System.out.println("Found " + orderCount + " orders in database");

            // If no orders exist, create some sample data
            if (orderCount == 0) {
                System.out.println("Creating sample order data...");
                createSampleOrders(conn);
            }

            // Get date range from date pickers
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            // Build the query based on whether date filters are applied
            String query;
            PreparedStatement stmt;

            if (startDate != null && endDate != null) {
                // Convert LocalDate to java.sql.Date
                java.sql.Date sqlStartDate = java.sql.Date.valueOf(startDate);
                java.sql.Date sqlEndDate = java.sql.Date.valueOf(endDate);

                query = "SELECT o.id, c.company, o.order_date, o.shipped_date, " +
                        "(SELECT SUM(od.quantity * od.unit_price * (1 - od.discount)) FROM order_details od WHERE od.order_id = o.id) AS total " +
                        "FROM orders o " +
                        "JOIN customers c ON o.customer_id = c.id " +
                        "WHERE o.order_date BETWEEN ? AND ? " +
                        "ORDER BY o.order_date DESC";

                stmt = conn.prepareStatement(query);
                stmt.setDate(1, sqlStartDate);
                stmt.setDate(2, sqlEndDate);
            } else {
                // No date filters, show all orders
                query = "SELECT o.id, c.company, o.order_date, o.shipped_date, " +
                        "(SELECT SUM(od.quantity * od.unit_price * (1 - od.discount)) FROM order_details od WHERE od.order_id = o.id) AS total " +
                        "FROM orders o " +
                        "JOIN customers c ON o.customer_id = c.id " +
                        "ORDER BY o.order_date DESC";

                stmt = conn.prepareStatement(query);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("id"),
                    rs.getString("company"),
                    rs.getDate("order_date") != null ? rs.getDate("order_date").toLocalDate() : null,
                    rs.getDate("shipped_date") != null ? rs.getDate("shipped_date").toLocalDate() : null,
                    rs.getDouble("total")
                );
                orderList.add(order);
                System.out.println("Loaded order: " + order.getId() + " for " + order.getCustomerName());
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading orders: " + e.getMessage());
        }

        System.out.println("Setting " + orderList.size() + " orders to table");
        orderTable.setItems(orderList);
    }

    private void searchOrdersByDate() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            showAlert("Invalid Date Range", "Please select both start and end dates.");
            return;
        }

        if (startDate.isAfter(endDate)) {
            showAlert("Invalid Date Range", "Start date must be before end date.");
            return;
        }

        ObservableList<Order> orderList = FXCollections.observableArrayList();

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT o.id, c.company, o.order_date, o.shipped_date, " +
                          "(SELECT SUM(od.quantity * od.unit_price * (1 - od.discount)) FROM order_details od WHERE od.order_id = o.id) AS total " +
                          "FROM orders o " +
                          "JOIN customers c ON o.customer_id = c.id " +
                          "WHERE o.order_date BETWEEN ? AND ? " +
                          "ORDER BY o.order_date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("id"),
                    rs.getString("company"),
                    rs.getDate("order_date") != null ? rs.getDate("order_date").toLocalDate() : null,
                    rs.getDate("shipped_date") != null ? rs.getDate("shipped_date").toLocalDate() : null,
                    rs.getDouble("total")
                );
                orderList.add(order);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error searching orders: " + e.getMessage());
        }

        orderTable.setItems(orderList);
    }

    private void loadOrderDetails(int orderId) {
        ObservableList<OrderDetail> orderDetailsList = FXCollections.observableArrayList();

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT od.order_id, p.product_name, od.unit_price, od.quantity, od.discount, " +
                          "(od.quantity * od.unit_price * (1 - od.discount)) AS subtotal " +
                          "FROM order_details od " +
                          "JOIN products p ON od.product_id = p.id " +
                          "WHERE od.order_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, orderId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                OrderDetail orderDetail = new OrderDetail(
                    rs.getInt("order_id"),
                    rs.getString("product_name"),
                    rs.getDouble("unit_price"),
                    rs.getInt("quantity"),
                    rs.getDouble("discount"),
                    rs.getDouble("subtotal")
                );
                orderDetailsList.add(orderDetail);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading order details: " + e.getMessage());
        }

        orderDetailsTable.setItems(orderDetailsList);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void createSampleOrders(Connection conn) {
        try {
            // First, make sure we have customers
            String checkCustomersQuery = "SELECT COUNT(*) FROM customers";
            PreparedStatement checkCustomersStmt = conn.prepareStatement(checkCustomersQuery);
            ResultSet checkCustomersRs = checkCustomersStmt.executeQuery();
            int customerCount = 0;
            if (checkCustomersRs.next()) {
                customerCount = checkCustomersRs.getInt(1);
            }
            checkCustomersRs.close();
            checkCustomersStmt.close();

            if (customerCount == 0) {
                System.out.println("No customers found. Cannot create sample orders.");
                return;
            }

            // Get customer IDs
            String getCustomersQuery = "SELECT id FROM customers LIMIT 10";
            PreparedStatement getCustomersStmt = conn.prepareStatement(getCustomersQuery);
            ResultSet getCustomersRs = getCustomersStmt.executeQuery();

            List<Integer> customerIds = new ArrayList<>();
            while (getCustomersRs.next()) {
                customerIds.add(getCustomersRs.getInt("id"));
            }
            getCustomersRs.close();
            getCustomersStmt.close();

            if (customerIds.isEmpty()) {
                System.out.println("No customer IDs found. Cannot create sample orders.");
                return;
            }

            // Next, make sure we have products
            String checkProductsQuery = "SELECT COUNT(*) FROM products";
            PreparedStatement checkProductsStmt = conn.prepareStatement(checkProductsQuery);
            ResultSet checkProductsRs = checkProductsStmt.executeQuery();
            int productCount = 0;
            if (checkProductsRs.next()) {
                productCount = checkProductsRs.getInt(1);
            }
            checkProductsRs.close();
            checkProductsStmt.close();

            if (productCount == 0) {
                System.out.println("No products found. Cannot create sample orders.");
                return;
            }

            // Get product IDs and prices
            String getProductsQuery = "SELECT id, list_price FROM products LIMIT 20";
            PreparedStatement getProductsStmt = conn.prepareStatement(getProductsQuery);
            ResultSet getProductsRs = getProductsStmt.executeQuery();

            List<Integer> productIds = new ArrayList<>();
            Map<Integer, Double> productPrices = new HashMap<>();
            while (getProductsRs.next()) {
                int productId = getProductsRs.getInt("id");
                double price = getProductsRs.getDouble("list_price");
                productIds.add(productId);
                productPrices.put(productId, price);
            }
            getProductsRs.close();
            getProductsStmt.close();

            if (productIds.isEmpty()) {
                System.out.println("No product IDs found. Cannot create sample orders.");
                return;
            }

            // Create sample orders
            Random random = new Random();
            LocalDate today = LocalDate.now();

            // Create 15 sample orders over the last 60 days
            for (int i = 0; i < 15; i++) {
                // Random customer
                int customerId = customerIds.get(random.nextInt(customerIds.size()));

                // Random date in the last 60 days
                LocalDate orderDate = today.minusDays(random.nextInt(60));
                LocalDate shippedDate = random.nextBoolean() ? orderDate.plusDays(random.nextInt(5) + 1) : null;

                // First, check if we need to create order status entries
                String checkStatusQuery = "SELECT COUNT(*) FROM orders_status";
                PreparedStatement checkStatusStmt = conn.prepareStatement(checkStatusQuery);
                ResultSet checkStatusRs = checkStatusStmt.executeQuery();
                int statusCount = 0;
                if (checkStatusRs.next()) {
                    statusCount = checkStatusRs.getInt(1);
                }
                checkStatusRs.close();
                checkStatusStmt.close();

                // Create order statuses if needed
                if (statusCount == 0) {
                    String[] statuses = {"New", "Approved", "Shipped", "Closed"};
                    for (int statusIndex = 0; statusIndex < statuses.length; statusIndex++) {
                        String insertStatusQuery = "INSERT INTO orders_status (id, status_name) VALUES (?, ?)";
                        PreparedStatement insertStatusStmt = conn.prepareStatement(insertStatusQuery);
                        insertStatusStmt.setInt(1, statusIndex + 1);
                        insertStatusStmt.setString(2, statuses[statusIndex]);
                        insertStatusStmt.executeUpdate();
                        insertStatusStmt.close();
                    }
                    System.out.println("Created order statuses");
                }

                // Get a random status ID (1-4)
                int statusId = random.nextInt(4) + 1;

                // Insert order
                String insertOrderQuery = "INSERT INTO orders (customer_id, order_date, shipped_date, status_id) VALUES (?, ?, ?, ?)";
                PreparedStatement insertOrderStmt = conn.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS);
                insertOrderStmt.setInt(1, customerId);
                insertOrderStmt.setDate(2, java.sql.Date.valueOf(orderDate));
                if (shippedDate != null) {
                    insertOrderStmt.setDate(3, java.sql.Date.valueOf(shippedDate));
                } else {
                    insertOrderStmt.setNull(3, java.sql.Types.DATE);
                }
                insertOrderStmt.setInt(4, statusId);

                insertOrderStmt.executeUpdate();

                // Get the generated order ID
                ResultSet generatedKeys = insertOrderStmt.getGeneratedKeys();
                int orderId = 0;
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1);
                }
                generatedKeys.close();
                insertOrderStmt.close();

                if (orderId == 0) {
                    System.out.println("Failed to get order ID. Skipping order details.");
                    continue;
                }

                // Add 1-5 products to the order
                int numProducts = random.nextInt(5) + 1;
                Set<Integer> usedProductIds = new HashSet<>();

                for (int j = 0; j < numProducts; j++) {
                    // Get a random product that hasn't been used in this order yet
                    int productId;
                    do {
                        productId = productIds.get(random.nextInt(productIds.size()));
                    } while (usedProductIds.contains(productId));

                    usedProductIds.add(productId);

                    // Random quantity between 1 and 10
                    int quantity = random.nextInt(10) + 1;

                    // Get the product price
                    double unitPrice = productPrices.get(productId);

                    // Random discount between 0% and 20%
                    double discount = Math.round(random.nextDouble() * 0.2 * 100) / 100.0;

                    // Insert order detail
                    String insertDetailQuery = "INSERT INTO order_details (order_id, product_id, quantity, unit_price, discount) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement insertDetailStmt = conn.prepareStatement(insertDetailQuery);
                    insertDetailStmt.setInt(1, orderId);
                    insertDetailStmt.setInt(2, productId);
                    insertDetailStmt.setInt(3, quantity);
                    insertDetailStmt.setDouble(4, unitPrice);
                    insertDetailStmt.setDouble(5, discount);

                    insertDetailStmt.executeUpdate();
                    insertDetailStmt.close();
                }
            }

            System.out.println("Created 15 sample orders with details");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error creating sample orders: " + e.getMessage());
        }
    }
}
