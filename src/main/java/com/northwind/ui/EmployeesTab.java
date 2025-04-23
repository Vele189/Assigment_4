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
import com.northwind.model.Employee;

public class EmployeesTab extends Tab {

    private TableView<Employee> employeeTable;
    private TextField filterField;
    private Button filterButton;

    public EmployeesTab() {
        setText("Employees");

        // Create the main layout for this tab
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));

        // Create filter controls
        HBox filterBox = createFilterBox();

        // Create employee table
        employeeTable = createEmployeeTable();

        // Add components to the layout
        borderPane.setTop(filterBox);
        borderPane.setCenter(employeeTable);

        // Set the content of the tab
        setContent(borderPane);

        // Load initial data
        loadEmployees();
    }

    private HBox createFilterBox() {
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(10));

        Label filterLabel = new Label("Filter Employees:");

        filterField = new TextField();
        filterField.setPromptText("Enter name or city");

        filterButton = new Button("Filter");
        filterButton.setOnAction(e -> filterEmployees());

        Button viewAllButton = new Button("View All");
        viewAllButton.setOnAction(e -> loadEmployees());

        filterBox.getChildren().addAll(filterLabel, filterField, filterButton, viewAllButton);

        return filterBox;
    }

    private TableView<Employee> createEmployeeTable() {
        TableView<Employee> table = new TableView<>();

        // Create columns
        TableColumn<Employee, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setPrefWidth(100);

        TableColumn<Employee, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setPrefWidth(100);

        TableColumn<Employee, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setPrefWidth(150);

        TableColumn<Employee, String> address2Col = new TableColumn<>("Address Line 2");
        address2Col.setCellValueFactory(new PropertyValueFactory<>("addressLine2"));
        address2Col.setPrefWidth(150);

        TableColumn<Employee, String> cityCol = new TableColumn<>("City");
        cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
        cityCol.setPrefWidth(100);

        TableColumn<Employee, String> regionCol = new TableColumn<>("Region");
        regionCol.setCellValueFactory(new PropertyValueFactory<>("region"));
        regionCol.setPrefWidth(100);

        TableColumn<Employee, String> postalCodeCol = new TableColumn<>("Postal Code");
        postalCodeCol.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
        postalCodeCol.setPrefWidth(100);

        TableColumn<Employee, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(120);

        TableColumn<Employee, String> officeCol = new TableColumn<>("Office");
        officeCol.setCellValueFactory(new PropertyValueFactory<>("office"));
        officeCol.setPrefWidth(100);

        TableColumn<Employee, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(60);

        // Add columns to table
        table.getColumns().addAll(
            firstNameCol, lastNameCol, addressCol, address2Col, cityCol,
            regionCol, postalCodeCol, phoneCol, officeCol, activeCol
        );

        return table;
    }

    private void loadEmployees() {
        ObservableList<Employee> employeeList = FXCollections.observableArrayList();
        System.out.println("Loading employees...");

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert("Database Error", "Could not establish database connection. Please check your database settings.");
                return;
            }

            // First check if the employees table has any data
            String countQuery = "SELECT COUNT(*) FROM employees";
            PreparedStatement countStmt = conn.prepareStatement(countQuery);
            ResultSet countRs = countStmt.executeQuery();
            int employeeCount = 0;
            if (countRs.next()) {
                employeeCount = countRs.getInt(1);
            }
            countRs.close();
            countStmt.close();

            System.out.println("Found " + employeeCount + " employees in database");

            // If no employees exist, create some sample data
            if (employeeCount == 0) {
                System.out.println("Creating sample employee data...");
                createSampleEmployees(conn);
            }

            String query = "SELECT id, first_name, last_name, address, city, state_province as region, " +
                          "zip_postal_code as postal_code, home_phone, job_title as office, notes as active FROM employees";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee employee = new Employee(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("address"),
                    "", // No address_line2 in the database
                    rs.getString("city"),
                    rs.getString("region"),
                    rs.getString("postal_code"),
                    rs.getString("home_phone"),
                    rs.getString("office"),
                    rs.getString("active") != null
                );
                employeeList.add(employee);
                System.out.println("Loaded employee: " + employee.getFirstName() + " " + employee.getLastName());
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading employees: " + e.getMessage());
        }

        System.out.println("Setting " + employeeList.size() + " employees to table");
        employeeTable.setItems(employeeList);
    }

    private void filterEmployees() {
        String filterTerm = filterField.getText().trim().toLowerCase();

        if (filterTerm.isEmpty()) {
            loadEmployees();
            return;
        }

        ObservableList<Employee> employeeList = FXCollections.observableArrayList();

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert("Database Error", "Could not establish database connection. Please check your database settings.");
                return;
            }

            String query = "SELECT id, first_name, last_name, address, city, state_province as region, " +
                          "zip_postal_code as postal_code, home_phone, job_title as office, notes as active FROM employees " +
                          "WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR LOWER(city) LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(query);

            String searchPattern = "%" + filterTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee employee = new Employee(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("address"),
                    "", // No address_line2 in the database
                    rs.getString("city"),
                    rs.getString("region"),
                    rs.getString("postal_code"),
                    rs.getString("home_phone"),
                    rs.getString("office"),
                    rs.getString("active") != null
                );
                employeeList.add(employee);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error filtering employees: " + e.getMessage());
        }

        employeeTable.setItems(employeeList);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void createSampleEmployees(Connection conn) {
        try {
            String[][] employeeData = {
                {"Nancy", "Davolio", "507 - 20th Ave. E. Apt. 2A", "Seattle", "WA", "98122", "(206) 555-9857", "Sales Representative", "Education includes a BA in psychology from Colorado State University. She also completed 'The Art of the Cold Call.' Nancy is a member of Toastmasters International."},
                {"Andrew", "Fuller", "908 W. Capital Way", "Tacoma", "WA", "98401", "(206) 555-9482", "Vice President, Sales", "Andrew received his BTS commercial and a Ph.D. in international marketing from the University of Dallas. He is fluent in French and Italian and reads German. He joined the company as a sales representative, was promoted to sales manager and was then named vice president of sales. Andrew is a member of the Sales Management Roundtable, the Seattle Chamber of Commerce, and the Pacific Rim Importers Association."},
                {"Janet", "Leverling", "722 Moss Bay Blvd.", "Kirkland", "WA", "98033", "(206) 555-3412", "Sales Representative", "Janet has a BS degree in chemistry from Boston College. She has also completed a certificate program in food retailing management. Janet was hired as a sales associate and was promoted to sales representative."},
                {"Margaret", "Peacock", "4110 Old Redmond Rd.", "Redmond", "WA", "98052", "(206) 555-8122", "Sales Representative", "Margaret holds a BA in English literature from Concordia College and an MA from the American Institute of Culinary Arts. She was temporarily assigned to the London office before returning to her permanent post in Seattle."},
                {"Steven", "Buchanan", "14 Garrett Hill", "London", "UK", "SW1 8JR", "(71) 555-4848", "Sales Manager", "Steven Buchanan graduated from St. Andrews University, Scotland, with a BSC degree. Upon joining the company as a sales representative, he spent 6 months in an orientation program at the Seattle office and then returned to his permanent post in London, where he was promoted to sales manager. Mr. Buchanan has completed the courses 'Successful Telemarketing' and 'International Sales Management.' He is fluent in French."},
                {"Michael", "Suyama", "Coventry House Miner Rd.", "London", "UK", "EC2 7JR", "(71) 555-7773", "Sales Representative", "Michael is a graduate of Sussex University (MA, economics) and the University of California at Los Angeles (MBA, marketing). He has also taken the courses 'Multi-Cultural Selling' and 'Time Management for the Sales Professional.' He is fluent in Japanese and can read and write French, Portuguese, and Spanish."},
                {"Robert", "King", "Edgeham Hollow Winchester Way", "London", "UK", "RG1 9SP", "(71) 555-5598", "Sales Representative", "Robert King served in the Peace Corps and traveled extensively before completing his degree in English at the University of Michigan and then joining the company. After completing a course entitled 'Selling in Europe,' he was transferred to the London office."},
                {"Laura", "Callahan", "4726 - 11th Ave. N.E.", "Seattle", "WA", "98105", "(206) 555-1189", "Inside Sales Coordinator", "Laura received a BA in psychology from the University of Washington. She has also completed a course in business French. She reads and writes French."},
                {"Anne", "Dodsworth", "7 Houndstooth Rd.", "London", "UK", "WG2 7LT", "(71) 555-4444", "Sales Representative", "Anne has a BA degree in English from St. Lawrence College. She is fluent in French and German."}
            };

            for (String[] employee : employeeData) {
                String insertQuery = "INSERT INTO employees (first_name, last_name, address, city, state_province, " +
                                    "zip_postal_code, home_phone, job_title, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(insertQuery);
                stmt.setString(1, employee[0]);
                stmt.setString(2, employee[1]);
                stmt.setString(3, employee[2]);
                stmt.setString(4, employee[3]);
                stmt.setString(5, employee[4]);
                stmt.setString(6, employee[5]);
                stmt.setString(7, employee[6]);
                stmt.setString(8, employee[7]);
                stmt.setString(9, employee[8]);
                stmt.executeUpdate();
                stmt.close();
            }

            System.out.println("Created " + employeeData.length + " sample employees");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error creating sample employees: " + e.getMessage());
        }
    }
}
