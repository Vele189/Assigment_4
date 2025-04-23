package com.northwind.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.northwind.db.DatabaseConnection;
import com.northwind.model.Product;

public class ProductsTab extends Tab {

    private TableView<Product> productTable;
    private TextField searchField;
    private Button searchButton;
    private Button viewAllButton;
    private Button addProductButton;
    private ComboBox<String> categoryFilter;
    private ObservableList<String> categories;
    private ObservableList<String> suppliers;

    public ProductsTab() {
        setText("Products");

        // Create the main layout for this tab
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));

        // Create search and filter controls
        VBox topBox = new VBox(10);
        topBox.getChildren().addAll(createSearchBox(), createFilterBox());

        // Create product table
        productTable = createProductTable();

        // Add components to the layout
        borderPane.setTop(topBox);
        borderPane.setCenter(productTable);

        // Set the content of the tab
        setContent(borderPane);

        // Load initial data
        loadCategories();
        loadProducts();
    }

    private HBox createSearchBox() {
        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(10));

        Label searchLabel = new Label("Search Products:");
        searchField = new TextField();
        searchField.setPromptText("Enter product name");
        searchField.setPrefWidth(200);

        searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchProducts());

        viewAllButton = new Button("View All");
        viewAllButton.setOnAction(e -> loadProducts());

        addProductButton = new Button("Add New Product");
        addProductButton.setOnAction(e -> showAddProductDialog());

        searchBox.getChildren().addAll(searchLabel, searchField, searchButton, viewAllButton, addProductButton);

        return searchBox;
    }

    private HBox createFilterBox() {
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(0, 10, 10, 10));

        Label filterLabel = new Label("Filter by Category:");
        categoryFilter = new ComboBox<>();
        categoryFilter.setPrefWidth(200);

        // Add "All Categories" option
        categoryFilter.getItems().add("All Categories");
        categoryFilter.setValue("All Categories");

        // Set action for category filter
        categoryFilter.setOnAction(e -> filterProductsByCategory());

        filterBox.getChildren().addAll(filterLabel, categoryFilter);

        return filterBox;
    }

    private TableView<Product> createProductTable() {
        TableView<Product> table = new TableView<>();

        // Create columns
        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Product, String> nameColumn = new TableColumn<>("Product Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);

        TableColumn<Product, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryColumn.setPrefWidth(120);

        TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setPrefWidth(80);

        TableColumn<Product, Integer> stockColumn = new TableColumn<>("In Stock");
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockColumn.setPrefWidth(80);

        // Add columns to table
        table.getColumns().addAll(idColumn, nameColumn, categoryColumn, priceColumn, stockColumn);

        return table;
    }

    private void loadCategories() {
        categories = FXCollections.observableArrayList();
        categories.add("All Categories");

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT DISTINCT category FROM products ORDER BY category";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String category = rs.getString("category");
                if (category != null && !category.isEmpty()) {
                    categories.add(category);
                    categoryFilter.getItems().add(category);
                }
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading categories: " + e.getMessage());
        }

        // Load suppliers as well for the add product dialog
        loadSuppliers();
    }

    private void loadSuppliers() {
        suppliers = FXCollections.observableArrayList();

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT DISTINCT company FROM suppliers ORDER BY company";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String supplier = rs.getString("company");
                if (supplier != null && !supplier.isEmpty()) {
                    suppliers.add(supplier);
                }
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading suppliers: " + e.getMessage());
        }
    }

    private void loadProducts() {
        ObservableList<Product> productList = FXCollections.observableArrayList();
        System.out.println("Loading products...");

        try {
            Connection conn = DatabaseConnection.getConnection();

            // First check if the products table has any data
            String countQuery = "SELECT COUNT(*) FROM products";
            PreparedStatement countStmt = conn.prepareStatement(countQuery);
            ResultSet countRs = countStmt.executeQuery();
            int productCount = 0;
            if (countRs.next()) {
                productCount = countRs.getInt(1);
            }
            countRs.close();
            countStmt.close();

            System.out.println("Found " + productCount + " products in database");

            // If no products exist, create some sample data
            if (productCount == 0) {
                System.out.println("Creating sample product data...");
                createSampleProducts(conn);
            }

            String query = "SELECT id, product_name, category, list_price, quantity_per_unit FROM products";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getDouble("list_price"),
                    rs.getInt("quantity_per_unit")
                );
                productList.add(product);
                System.out.println("Loaded product: " + product.getName());
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading products: " + e.getMessage());
        }

        System.out.println("Setting " + productList.size() + " products to table");
        productTable.setItems(productList);
    }

    private void searchProducts() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            loadProducts();
            return;
        }

        ObservableList<Product> productList = FXCollections.observableArrayList();

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT id, product_name, category, list_price, quantity_per_unit FROM products " +
                          "WHERE product_name LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(query);

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getDouble("list_price"),
                    rs.getInt("quantity_per_unit")
                );
                productList.add(product);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error searching products: " + e.getMessage());
        }

        productTable.setItems(productList);
    }

    private void filterProductsByCategory() {
        String selectedCategory = categoryFilter.getValue();

        if (selectedCategory == null || selectedCategory.equals("All Categories")) {
            loadProducts();
            return;
        }

        ObservableList<Product> productList = FXCollections.observableArrayList();

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT id, product_name, category, list_price, quantity_per_unit FROM products " +
                          "WHERE category = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, selectedCategory);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getDouble("list_price"),
                    rs.getInt("quantity_per_unit")
                );
                productList.add(product);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error filtering products: " + e.getMessage());
        }

        productTable.setItems(productList);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void createSampleProducts(Connection conn) {
        try {
            // First, create a supplier if none exists
            String checkSupplierQuery = "SELECT COUNT(*) FROM suppliers";
            PreparedStatement checkStmt = conn.prepareStatement(checkSupplierQuery);
            ResultSet checkRs = checkStmt.executeQuery();
            int supplierCount = 0;
            if (checkRs.next()) {
                supplierCount = checkRs.getInt(1);
            }
            checkRs.close();
            checkStmt.close();

            int supplierId = 1;

            if (supplierCount == 0) {
                // Create multiple suppliers
                String[] supplierCompanies = {
                    "Exotic Liquids", "New Orleans Cajun Delights", "Grandma Kelly's Homestead",
                    "Tokyo Traders", "Cooperativa de Quesos 'Las Cabras'", "Mayumi's",
                    "Pavlova, Ltd.", "Specialty Biscuits, Ltd.", "PB Knäckebröd AB", "Refrescos Americanas LTDA"
                };

                String[] lastNames = {
                    "Schmitt", "Lebihan", "Roel", "Hashimoto", "Saavedra",
                    "Ohno", "Pavlova", "Brown", "Jansson", "Fernandez"
                };

                String[] firstNames = {
                    "Elizabeth", "Laurence", "Patricia", "Yoshi", "Antonio",
                    "Mayumi", "Ian", "Wendy", "Lars", "Carlos"
                };

                String[] jobTitles = {
                    "Purchasing Representative", "Owner", "Sales Agent", "Marketing Manager", "Marketing Representative",
                    "Sales Representative", "Marketing Assistant", "Order Administrator", "Owner", "Sales Agent"
                };

                // Insert suppliers
                for (int i = 0; i < supplierCompanies.length; i++) {
                    String insertSupplierQuery = "INSERT INTO suppliers (company, last_name, first_name, job_title) VALUES (?, ?, ?, ?)";
                    PreparedStatement supplierStmt = conn.prepareStatement(insertSupplierQuery);
                    supplierStmt.setString(1, supplierCompanies[i]);
                    supplierStmt.setString(2, lastNames[i]);
                    supplierStmt.setString(3, firstNames[i]);
                    supplierStmt.setString(4, jobTitles[i]);
                    supplierStmt.executeUpdate();
                    supplierStmt.close();
                }

                System.out.println("Created " + supplierCompanies.length + " sample suppliers");
            }

            // Get the first supplier ID
            String getSupplierIdQuery = "SELECT id FROM suppliers LIMIT 1";
            PreparedStatement getSupplierStmt = conn.prepareStatement(getSupplierIdQuery);
            ResultSet supplierRs = getSupplierStmt.executeQuery();
            if (supplierRs.next()) {
                supplierId = supplierRs.getInt("id");
            }
            supplierRs.close();
            getSupplierStmt.close();

            // Now insert sample products
            String[][] productData = {
                {"Chai", "Beverages", "18.00", "10"},
                {"Chang", "Beverages", "19.00", "24"},
                {"Aniseed Syrup", "Condiments", "10.00", "12"},
                {"Chef Anton's Cajun Seasoning", "Condiments", "22.00", "48"},
                {"Grandma's Boysenberry Spread", "Condiments", "25.00", "12"},
                {"Uncle Bob's Organic Dried Pears", "Produce", "30.00", "12"},
                {"Northwoods Cranberry Sauce", "Condiments", "40.00", "12"},
                {"Mishi Kobe Niku", "Meat/Poultry", "97.00", "18"},
                {"Ikura", "Seafood", "31.00", "12"},
                {"Queso Cabrales", "Dairy Products", "21.00", "12"},
                {"Queso Manchego La Pastora", "Dairy Products", "38.00", "12"},
                {"Konbu", "Seafood", "6.00", "24"},
                {"Tofu", "Produce", "23.25", "20"},
                {"Genen Shouyu", "Condiments", "15.50", "24"},
                {"Pavlova", "Confections", "17.45", "32"},
                {"Alice Mutton", "Meat/Poultry", "39.00", "20"},
                {"Carnarvon Tigers", "Seafood", "62.50", "16"},
                {"Teatime Chocolate Biscuits", "Confections", "9.20", "10"},
                {"Sir Rodney's Marmalade", "Confections", "81.00", "30"},
                {"Sir Rodney's Scones", "Confections", "10.00", "24"}
            };

            // Insert products
            for (String[] product : productData) {
                String insertQuery = "INSERT INTO products (product_name, category, list_price, quantity_per_unit) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(insertQuery);
                stmt.setString(1, product[0]);
                stmt.setString(2, product[1]);
                stmt.setDouble(3, Double.parseDouble(product[2]));
                stmt.setInt(4, Integer.parseInt(product[3]));
                stmt.executeUpdate();
                stmt.close();
            }

            System.out.println("Created " + productData.length + " sample products");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error creating sample products: " + e.getMessage());
        }
    }

    private void showAddProductDialog() {
        // Create the custom dialog
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Add New Product");
        dialog.setHeaderText("Enter new product details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create form fields
        TextField productNameField = new TextField();
        productNameField.setPromptText("Product name");

        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.setItems(categories);
        categoryComboBox.getItems().remove("All Categories");
        if (!categoryComboBox.getItems().isEmpty()) {
            categoryComboBox.setValue(categoryComboBox.getItems().get(0));
        }

        ComboBox<String> supplierComboBox = new ComboBox<>();
        supplierComboBox.setItems(suppliers);
        if (!supplierComboBox.getItems().isEmpty()) {
            supplierComboBox.setValue(supplierComboBox.getItems().get(0));
        }

        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        // Add fields to grid
        grid.add(new Label("Product Name:"), 0, 0);
        grid.add(productNameField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryComboBox, 1, 1);
        grid.add(new Label("Supplier:"), 0, 2);
        grid.add(supplierComboBox, 1, 2);
        grid.add(new Label("Price:"), 0, 3);
        grid.add(priceField, 1, 3);
        grid.add(new Label("Quantity:"), 0, 4);
        grid.add(quantityField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the product name field by default
        productNameField.requestFocus();

        // Convert the result to a product when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String productName = productNameField.getText().trim();
                    String category = categoryComboBox.getValue();
                    String supplier = supplierComboBox.getValue();
                    double price = Double.parseDouble(priceField.getText().trim());
                    int quantity = Integer.parseInt(quantityField.getText().trim());

                    // Validate input
                    if (productName.isEmpty()) {
                        showAlert("Validation Error", "Product name cannot be empty");
                        return null;
                    }

                    if (category == null || category.isEmpty()) {
                        showAlert("Validation Error", "Please select a category");
                        return null;
                    }

                    if (supplier == null || supplier.isEmpty()) {
                        showAlert("Validation Error", "Please select a supplier");
                        return null;
                    }

                    if (price <= 0) {
                        showAlert("Validation Error", "Price must be greater than zero");
                        return null;
                    }

                    if (quantity < 0) {
                        showAlert("Validation Error", "Quantity cannot be negative");
                        return null;
                    }

                    // Create a temporary product object (ID will be assigned by the database)
                    return new Product(0, productName, category, supplier, price, quantity);

                } catch (NumberFormatException e) {
                    showAlert("Validation Error", "Price and quantity must be valid numbers");
                    return null;
                }
            }
            return null;
        });

        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(product -> {
            saveNewProduct(product);
        });
    }

    private void saveNewProduct(Product product) {
        try {
            Connection conn = DatabaseConnection.getConnection();

            // First, get the supplier ID
            int supplierId = 0;
            String supplierQuery = "SELECT id FROM suppliers WHERE company = ?";
            PreparedStatement supplierStmt = conn.prepareStatement(supplierQuery);
            supplierStmt.setString(1, product.getSupplier());
            ResultSet supplierRs = supplierStmt.executeQuery();

            if (supplierRs.next()) {
                supplierId = supplierRs.getInt("id");
            } else {
                showAlert("Error", "Supplier not found");
                return;
            }

            supplierRs.close();
            supplierStmt.close();

            // Insert the new product
            String insertQuery = "INSERT INTO products (product_name, category, list_price, quantity_per_unit) " +
                               "VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, product.getName());
            insertStmt.setString(2, product.getCategory());
            insertStmt.setDouble(3, product.getPrice());
            insertStmt.setInt(4, product.getStock());

            int rowsAffected = insertStmt.executeUpdate();
            insertStmt.close();

            if (rowsAffected > 0) {
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Product added successfully!");
                alert.showAndWait();

                // Reload the products table
                loadProducts();
            } else {
                showAlert("Error", "Failed to add product");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error adding product: " + e.getMessage());
        }
    }
}
