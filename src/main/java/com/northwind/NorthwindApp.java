package com.northwind;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import com.northwind.ui.CustomersTab;
import com.northwind.ui.EmployeesTab;
import com.northwind.ui.NotificationsTab;
import com.northwind.ui.ProductsTab;
import com.northwind.ui.OrdersTab;
import com.northwind.ui.ReportsTab;

public class NorthwindApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Create the main layout
            BorderPane root = new BorderPane();

            // Create the tab pane
            TabPane tabPane = new TabPane();

            // Create tabs
            Tab customersTab = new CustomersTab();
            Tab productsTab = new ProductsTab();
            Tab ordersTab = new OrdersTab();
            Tab employeesTab = new EmployeesTab();
            Tab reportsTab = new ReportsTab();
            Tab notificationsTab = new NotificationsTab();

            // Add tabs to the tab pane
            tabPane.getTabs().addAll(customersTab, productsTab, ordersTab, employeesTab, reportsTab, notificationsTab);

            // Set the tab pane as the center of the border pane
            root.setCenter(tabPane);

            // Create the scene
            Scene scene = new Scene(root, 800, 600);

            // Set the scene and show the stage
            primaryStage.setScene(scene);
            primaryStage.setTitle("Northwind Database Application");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
