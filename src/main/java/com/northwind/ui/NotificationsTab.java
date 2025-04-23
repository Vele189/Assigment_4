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
import java.sql.Statement;
import java.time.LocalDate;

import com.northwind.db.DatabaseConnection;
import com.northwind.model.Client;

public class NotificationsTab extends Tab {

    private TableView<Client> clientsTable;
    private TableView<Client> inactiveClientsTable;
    private TextField searchField;
    private Button searchButton;
    private Button viewAllButton;
    private Button addClientButton;
    private Button editClientButton;
    private Button deleteClientButton;

    public NotificationsTab() {
        setText("Notifications");

        // Create the main layout for this tab
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));

        // Create top controls
        VBox topBox = new VBox(10);
        topBox.getChildren().addAll(createButtonsBox(), createSearchBox());

        // Create tab pane for clients and inactive clients
        TabPane clientsTabPane = new TabPane();

        // Create all clients tab
        Tab allClientsTab = new Tab("All Clients");
        allClientsTab.setClosable(false);
        clientsTable = createClientsTable();
        allClientsTab.setContent(clientsTable);

        // Create inactive clients tab
        Tab inactiveClientsTab = new Tab("Inactive Clients");
        inactiveClientsTab.setClosable(false);
        inactiveClientsTable = createClientsTable();
        inactiveClientsTab.setContent(inactiveClientsTable);

        // Add tabs to tab pane
        clientsTabPane.getTabs().addAll(allClientsTab, inactiveClientsTab);

        // Add components to the layout
        borderPane.setTop(topBox);
        borderPane.setCenter(clientsTabPane);

        // Set the content of the tab
        setContent(borderPane);

        // Create clients table if it doesn't exist
        createClientsTableIfNotExists();

        // Load initial data
        loadAllClients();
        loadInactiveClients();

        // Set up button actions
        setupButtonActions();
    }

    private HBox createButtonsBox() {
        HBox buttonsBox = new HBox(10);
        buttonsBox.setPadding(new Insets(10));

        addClientButton = new Button("Add Client");
        editClientButton = new Button("Edit Client");
        deleteClientButton = new Button("Delete Client");

        buttonsBox.getChildren().addAll(addClientButton, editClientButton, deleteClientButton);

        return buttonsBox;
    }

    private HBox createSearchBox() {
        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(0, 10, 10, 10));

        Label searchLabel = new Label("Search Clients:");
        searchField = new TextField();
        searchField.setPromptText("Enter client name or company");
        searchField.setPrefWidth(200);

        searchButton = new Button("Search");
        viewAllButton = new Button("View All");

        searchBox.getChildren().addAll(searchLabel, searchField, searchButton, viewAllButton);

        return searchBox;
    }

    private TableView<Client> createClientsTable() {
        TableView<Client> table = new TableView<>();

        // Create columns
        TableColumn<Client, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Client, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(150);

        TableColumn<Client, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailColumn.setPrefWidth(200);

        TableColumn<Client, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneColumn.setPrefWidth(120);

        TableColumn<Client, String> companyColumn = new TableColumn<>("Company");
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("company"));
        companyColumn.setPrefWidth(150);

        TableColumn<Client, Boolean> activeColumn = new TableColumn<>("Active");
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeColumn.setPrefWidth(60);

        TableColumn<Client, String> lastOrderColumn = new TableColumn<>("Last Order");
        lastOrderColumn.setCellValueFactory(new PropertyValueFactory<>("lastOrderDate"));
        lastOrderColumn.setPrefWidth(100);

        // Add columns to table
        table.getColumns().addAll(idColumn, nameColumn, emailColumn, phoneColumn, companyColumn, activeColumn, lastOrderColumn);

        return table;
    }

    private void setupButtonActions() {
        searchButton.setOnAction(e -> searchClients());
        viewAllButton.setOnAction(e -> loadAllClients());
        addClientButton.setOnAction(e -> showAddClientDialog());
        editClientButton.setOnAction(e -> showEditClientDialog());
        deleteClientButton.setOnAction(e -> deleteSelectedClient());
    }

    private void createClientsTableIfNotExists() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert("Database Error", "Could not establish database connection. Please check your database settings.");
                return;
            }

            // Check if the clients table exists
            boolean tableExists = false;
            try {
                String checkQuery = "SELECT 1 FROM clients LIMIT 1";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.executeQuery();
                tableExists = true;
                checkStmt.close();
            } catch (SQLException e) {
                // Table doesn't exist, we'll create it
                tableExists = false;
            }

            if (!tableExists) {
                // Create the clients table
                Statement stmt = conn.createStatement();
                String createTableSQL = "CREATE TABLE clients (" +
                                       "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                       "name VARCHAR(100) NOT NULL, " +
                                       "email VARCHAR(100) NOT NULL, " +
                                       "phone VARCHAR(20), " +
                                       "company VARCHAR(100), " +
                                       "active BOOLEAN DEFAULT TRUE, " +
                                       "last_order_date DATE)";
                stmt.executeUpdate(createTableSQL);

                // Add some sample data
                String insertSQL = "INSERT INTO clients (name, email, phone, company, active) VALUES " +
                                  "('John Doe', 'john.doe@example.com', '555-1234', 'ABC Company', TRUE), " +
                                  "('Jane Smith', 'jane.smith@example.com', '555-5678', 'XYZ Corporation', TRUE), " +
                                  "('Bob Johnson', 'bob.johnson@example.com', '555-9012', 'Acme Inc.', FALSE)";
                stmt.executeUpdate(insertSQL);

                stmt.close();

                System.out.println("Clients table created successfully");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error creating clients table: " + e.getMessage());
        }
    }

    private void loadAllClients() {
        ObservableList<Client> clientList = FXCollections.observableArrayList();

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert("Database Error", "Could not establish database connection. Please check your database settings.");
                return;
            }

            String query = "SELECT id, name, email, phone, company, active, last_order_date FROM clients ORDER BY name";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Client client = new Client(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("company"),
                    rs.getBoolean("active"),
                    rs.getDate("last_order_date") != null ? rs.getDate("last_order_date").toString() : "Never"
                );
                clientList.add(client);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading clients: " + e.getMessage());
        }

        clientsTable.setItems(clientList);
    }

    private void loadInactiveClients() {
        ObservableList<Client> inactiveClientList = FXCollections.observableArrayList();

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert("Database Error", "Could not establish database connection. Please check your database settings.");
                return;
            }

            // Get clients who are marked as inactive
            String query = "SELECT id, name, email, phone, company, active, last_order_date " +
                          "FROM clients " +
                          "WHERE active = FALSE " +
                          "ORDER BY name";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Client client = new Client(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("company"),
                    rs.getBoolean("active"),
                    rs.getDate("last_order_date") != null ? rs.getDate("last_order_date").toString() : "Never"
                );
                inactiveClientList.add(client);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading inactive clients: " + e.getMessage());
        }

        inactiveClientsTable.setItems(inactiveClientList);
    }

    private void searchClients() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            loadAllClients();
            return;
        }

        ObservableList<Client> clientList = FXCollections.observableArrayList();

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert("Database Error", "Could not establish database connection. Please check your database settings.");
                return;
            }

            String query = "SELECT id, name, email, phone, company, active, last_order_date " +
                          "FROM clients " +
                          "WHERE name LIKE ? OR company LIKE ? OR email LIKE ? " +
                          "ORDER BY name";
            PreparedStatement stmt = conn.prepareStatement(query);

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Client client = new Client(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("company"),
                    rs.getBoolean("active"),
                    rs.getDate("last_order_date") != null ? rs.getDate("last_order_date").toString() : "Never"
                );
                clientList.add(client);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error searching clients: " + e.getMessage());
        }

        clientsTable.setItems(clientList);
    }

    private void showAddClientDialog() {
        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle("Add New Client");
        dialog.setHeaderText("Enter client details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create form fields
        TextField nameField = new TextField();
        nameField.setPromptText("Full name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email address");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone number");

        TextField companyField = new TextField();
        companyField.setPromptText("Company name");

        CheckBox activeCheckBox = new CheckBox("Active client");
        activeCheckBox.setSelected(true);

        // Add fields to grid
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Company:"), 0, 3);
        grid.add(companyField, 1, 3);
        grid.add(activeCheckBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the name field by default
        nameField.requestFocus();

        // Convert the result to a client when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                String company = companyField.getText().trim();
                boolean active = activeCheckBox.isSelected();

                // Validate input
                if (name.isEmpty()) {
                    showAlert("Validation Error", "Name cannot be empty");
                    return null;
                }

                if (email.isEmpty()) {
                    showAlert("Validation Error", "Email cannot be empty");
                    return null;
                }

                // Create a temporary client object (ID will be assigned by the database)
                return new Client(0, name, email, phone, company, active, "Never");
            }
            return null;
        });

        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(client -> {
            saveNewClient(client);
        });
    }

    private void showEditClientDialog() {
        Client selectedClient = clientsTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert("Selection Error", "Please select a client to edit");
            return;
        }

        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle("Edit Client");
        dialog.setHeaderText("Edit client details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create form fields
        TextField nameField = new TextField(selectedClient.getName());
        TextField emailField = new TextField(selectedClient.getEmail());
        TextField phoneField = new TextField(selectedClient.getPhone());
        TextField companyField = new TextField(selectedClient.getCompany());
        CheckBox activeCheckBox = new CheckBox("Active client");
        activeCheckBox.setSelected(selectedClient.isActive());

        // Add fields to grid
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Company:"), 0, 3);
        grid.add(companyField, 1, 3);
        grid.add(activeCheckBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the name field by default
        nameField.requestFocus();

        // Convert the result to a client when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                String company = companyField.getText().trim();
                boolean active = activeCheckBox.isSelected();

                // Validate input
                if (name.isEmpty()) {
                    showAlert("Validation Error", "Name cannot be empty");
                    return null;
                }

                if (email.isEmpty()) {
                    showAlert("Validation Error", "Email cannot be empty");
                    return null;
                }

                // Create a client object with the updated values
                return new Client(
                    selectedClient.getId(),
                    name,
                    email,
                    phone,
                    company,
                    active,
                    selectedClient.getLastOrderDate()
                );
            }
            return null;
        });

        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(client -> {
            updateClient(client);
        });
    }

    private void saveNewClient(Client client) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert("Database Error", "Could not establish database connection. Please check your database settings.");
                return;
            }

            String query = "INSERT INTO clients (name, email, phone, company, active) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, client.getName());
            stmt.setString(2, client.getEmail());
            stmt.setString(3, client.getPhone());
            stmt.setString(4, client.getCompany());
            stmt.setBoolean(5, client.isActive());

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            if (rowsAffected > 0) {
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Client added successfully!");
                alert.showAndWait();

                // Reload the clients tables
                loadAllClients();
                loadInactiveClients();
            } else {
                showAlert("Error", "Failed to add client");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error adding client: " + e.getMessage());
        }
    }

    private void updateClient(Client client) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert("Database Error", "Could not establish database connection. Please check your database settings.");
                return;
            }

            String query = "UPDATE clients SET name = ?, email = ?, phone = ?, company = ?, active = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, client.getName());
            stmt.setString(2, client.getEmail());
            stmt.setString(3, client.getPhone());
            stmt.setString(4, client.getCompany());
            stmt.setBoolean(5, client.isActive());
            stmt.setInt(6, client.getId());

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            if (rowsAffected > 0) {
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Client updated successfully!");
                alert.showAndWait();

                // Reload the clients tables
                loadAllClients();
                loadInactiveClients();
            } else {
                showAlert("Error", "Failed to update client");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error updating client: " + e.getMessage());
        }
    }

    private void deleteSelectedClient() {
        Client selectedClient = clientsTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert("Selection Error", "Please select a client to delete");
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Client");
        confirmAlert.setContentText("Are you sure you want to delete the client: " + selectedClient.getName() + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteClient(selectedClient.getId());
            }
        });
    }

    private void deleteClient(int clientId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert("Database Error", "Could not establish database connection. Please check your database settings.");
                return;
            }

            String query = "DELETE FROM clients WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, clientId);

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            if (rowsAffected > 0) {
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Client deleted successfully!");
                alert.showAndWait();

                // Reload the clients tables
                loadAllClients();
                loadInactiveClients();
            } else {
                showAlert("Error", "Failed to delete client");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error deleting client: " + e.getMessage());
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
