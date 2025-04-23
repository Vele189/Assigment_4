package com.northwind.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
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
import java.util.HashMap;
import java.util.Map;

import com.northwind.db.DatabaseConnection;

public class ReportsTab extends Tab {

    private ComboBox<String> reportTypeComboBox;
    private BorderPane chartPane;

    public ReportsTab() {
        setText("Reports");

        // Create the main layout for this tab
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));

        // Create report selection controls
        HBox controlBox = createControlBox();

        // Create chart pane
        chartPane = new BorderPane();
        chartPane.setPadding(new Insets(10));

        // Add components to the layout
        borderPane.setTop(controlBox);
        borderPane.setCenter(chartPane);

        // Set the content of the tab
        setContent(borderPane);

        // Generate initial report
        generateSalesByCategory();
    }

    private HBox createControlBox() {
        HBox controlBox = new HBox(10);
        controlBox.setPadding(new Insets(10));

        Label reportTypeLabel = new Label("Report Type:");

        reportTypeComboBox = new ComboBox<>();
        reportTypeComboBox.getItems().addAll(
            "Sales by Category",
            "Sales by Customer",
            "Monthly Sales",
            "Product Inventory",
            "Warehouse Inventory"
        );
        reportTypeComboBox.setValue("Sales by Category");

        Button generateButton = new Button("Generate Report");
        generateButton.setOnAction(e -> generateReport());

        controlBox.getChildren().addAll(reportTypeLabel, reportTypeComboBox, generateButton);

        return controlBox;
    }

    private void generateReport() {
        String reportType = reportTypeComboBox.getValue();

        switch (reportType) {
            case "Sales by Category":
                generateSalesByCategory();
                break;
            case "Sales by Customer":
                generateSalesByCustomer();
                break;
            case "Monthly Sales":
                generateMonthlySales();
                break;
            case "Product Inventory":
                generateProductInventory();
                break;
            case "Warehouse Inventory":
                generateWarehouseInventory();
                break;
            default:
                generateSalesByCategory();
                break;
        }
    }

    private void generateSalesByCategory() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT p.category, SUM(od.quantity * od.unit_price * (1 - od.discount)) AS total_sales " +
                          "FROM order_details od " +
                          "JOIN products p ON od.product_id = p.id " +
                          "GROUP BY p.category " +
                          "ORDER BY total_sales DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Create pie chart data
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            while (rs.next()) {
                String category = rs.getString("category");
                double totalSales = rs.getDouble("total_sales");

                if (category != null && !category.isEmpty()) {
                    pieChartData.add(new PieChart.Data(category, totalSales));
                }
            }

            rs.close();
            stmt.close();

            // Create pie chart
            PieChart pieChart = new PieChart(pieChartData);
            pieChart.setTitle("Sales by Category");
            pieChart.setLabelsVisible(true);

            // Clear previous chart and add new one
            chartPane.setCenter(pieChart);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error generating sales by category report: " + e.getMessage());
        }
    }

    private void generateSalesByCustomer() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT c.company, SUM(od.quantity * od.unit_price * (1 - od.discount)) AS total_sales " +
                          "FROM orders o " +
                          "JOIN customers c ON o.customer_id = c.id " +
                          "JOIN order_details od ON o.id = od.order_id " +
                          "GROUP BY c.company " +
                          "ORDER BY total_sales DESC " +
                          "LIMIT 10";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Create bar chart
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

            barChart.setTitle("Top 10 Customers by Sales");
            xAxis.setLabel("Customer");
            yAxis.setLabel("Sales ($)");

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Total Sales");

            while (rs.next()) {
                String customer = rs.getString("company");
                double totalSales = rs.getDouble("total_sales");

                if (customer != null && !customer.isEmpty()) {
                    series.getData().add(new XYChart.Data<>(customer, totalSales));
                }
            }

            rs.close();
            stmt.close();

            barChart.getData().add(series);

            // Clear previous chart and add new one
            chartPane.setCenter(barChart);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error generating sales by customer report: " + e.getMessage());
        }
    }

    private void generateMonthlySales() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT YEAR(o.order_date) AS year, MONTH(o.order_date) AS month, " +
                          "SUM(od.quantity * od.unit_price * (1 - od.discount)) AS total_sales " +
                          "FROM orders o " +
                          "JOIN order_details od ON o.id = od.order_id " +
                          "WHERE o.order_date IS NOT NULL " +
                          "GROUP BY YEAR(o.order_date), MONTH(o.order_date) " +
                          "ORDER BY year, month";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Create line chart
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

            barChart.setTitle("Monthly Sales");
            xAxis.setLabel("Month");
            yAxis.setLabel("Sales ($)");

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Total Sales");

            // Month names
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

            while (rs.next()) {
                int year = rs.getInt("year");
                int month = rs.getInt("month");
                double totalSales = rs.getDouble("total_sales");

                String monthYear = months[month - 1] + " " + year;
                series.getData().add(new XYChart.Data<>(monthYear, totalSales));
            }

            rs.close();
            stmt.close();

            barChart.getData().add(series);

            // Clear previous chart and add new one
            chartPane.setCenter(barChart);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error generating monthly sales report: " + e.getMessage());
        }
    }

    private void generateProductInventory() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT category, SUM(quantity_per_unit) AS total_inventory " +
                          "FROM products " +
                          "GROUP BY category " +
                          "ORDER BY total_inventory DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Create bar chart
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

            barChart.setTitle("Product Inventory by Category");
            xAxis.setLabel("Category");
            yAxis.setLabel("Inventory");

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Total Inventory");

            while (rs.next()) {
                String category = rs.getString("category");
                int totalInventory = rs.getInt("total_inventory");

                if (category != null && !category.isEmpty()) {
                    series.getData().add(new XYChart.Data<>(category, totalInventory));
                }
            }

            rs.close();
            stmt.close();

            barChart.getData().add(series);

            // Clear previous chart and add new one
            chartPane.setCenter(barChart);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error generating product inventory report: " + e.getMessage());
        }
    }

    private void generateWarehouseInventory() {
        try {
            Connection conn = DatabaseConnection.getConnection();

            // First check if warehouses table exists, if not create it
            boolean warehousesExist = false;
            try {
                String checkQuery = "SELECT 1 FROM warehouses LIMIT 1";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.executeQuery();
                warehousesExist = true;
                checkStmt.close();
            } catch (SQLException e) {
                // Table doesn't exist, we'll create it
                warehousesExist = false;
            }

            if (!warehousesExist) {
                // Create warehouses table
                Statement createStmt = conn.createStatement();
                String createTableSQL = "CREATE TABLE warehouses (" +
                                       "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                       "warehouse_name VARCHAR(100) NOT NULL)";
                createStmt.executeUpdate(createTableSQL);

                // Insert sample warehouses
                String insertSQL = "INSERT INTO warehouses (warehouse_name) VALUES " +
                                  "('North Warehouse'), " +
                                  "('South Warehouse'), " +
                                  "('East Warehouse'), " +
                                  "('West Warehouse')";
                createStmt.executeUpdate(insertSQL);

                // Add warehouse_id column to products table if it doesn't exist
                try {
                    String alterSQL = "ALTER TABLE products ADD COLUMN warehouse_id INT";
                    createStmt.executeUpdate(alterSQL);

                    // Assign random warehouses to products
                    String updateSQL = "UPDATE products SET warehouse_id = FLOOR(1 + RAND() * 4)";
                    createStmt.executeUpdate(updateSQL);
                } catch (SQLException ex) {
                    // Column might already exist
                    System.out.println("warehouse_id column might already exist: " + ex.getMessage());
                }

                createStmt.close();
            }

            String query = "SELECT w.warehouse_name, p.category, COUNT(p.id) AS product_count " +
                          "FROM products p " +
                          "JOIN warehouses w ON p.warehouse_id = w.id " +
                          "GROUP BY w.warehouse_name, p.category " +
                          "ORDER BY w.warehouse_name, p.category";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Create a table view for the report
            TableView<WarehouseInventory> tableView = new TableView<>();

            // Create columns
            TableColumn<WarehouseInventory, String> warehouseColumn = new TableColumn<>("Warehouse");
            warehouseColumn.setCellValueFactory(new PropertyValueFactory<>("warehouse"));
            warehouseColumn.setPrefWidth(150);

            TableColumn<WarehouseInventory, String> categoryColumn = new TableColumn<>("Category");
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            categoryColumn.setPrefWidth(150);

            TableColumn<WarehouseInventory, Integer> countColumn = new TableColumn<>("Product Count");
            countColumn.setCellValueFactory(new PropertyValueFactory<>("productCount"));
            countColumn.setPrefWidth(100);

            tableView.getColumns().addAll(warehouseColumn, categoryColumn, countColumn);

            // Create data list
            ObservableList<WarehouseInventory> data = FXCollections.observableArrayList();

            while (rs.next()) {
                String warehouse = rs.getString("warehouse_name");
                String category = rs.getString("category");
                int count = rs.getInt("product_count");

                data.add(new WarehouseInventory(warehouse, category, count));
            }

            rs.close();
            stmt.close();

            tableView.setItems(data);

            // Create a label for the report title
            Label titleLabel = new Label("Warehouse Inventory Report");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            // Create a VBox to hold the title and table
            VBox reportBox = new VBox(10);
            reportBox.setPadding(new Insets(10));
            reportBox.getChildren().addAll(titleLabel, tableView);

            // Clear previous chart and add the report
            chartPane.setCenter(reportBox);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error generating warehouse inventory report: " + e.getMessage());
        }
    }

    // Inner class to represent warehouse inventory data
    private static class WarehouseInventory {
        private final String warehouse;
        private final String category;
        private final int productCount;

        public WarehouseInventory(String warehouse, String category, int productCount) {
            this.warehouse = warehouse;
            this.category = category;
            this.productCount = productCount;
        }

        public String getWarehouse() {
            return warehouse;
        }

        public String getCategory() {
            return category;
        }

        public int getProductCount() {
            return productCount;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
