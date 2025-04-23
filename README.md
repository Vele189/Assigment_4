# Northwind Database Application

This is a Java application that provides a graphical user interface for the Northwind database.

## Features

The application includes the following tabs:

1. **Customers Tab**: View and search for customers in the database.
2. **Products Tab**: View, search, and filter products by category.
3. **Orders Tab**: View orders and their details, with the ability to filter by date range.
4. **Reports Tab**: Generate various reports including sales by category, sales by customer, monthly sales, and product inventory.

## Prerequisites

- Java 11 or higher
- Maven
- MySQL or MariaDB with the Northwind database imported

## Setup

1. Make sure you have the Northwind database set up in your MySQL/MariaDB server.
2. Update the database connection parameters in `src/main/java/com/northwind/db/DatabaseConnection.java` if needed.
3. Build the project using Maven:

```bash
mvn clean package
```

## Running the Application

You can run the application using Maven:

```bash
mvn javafx:run
```

Or you can run the generated JAR file:

```bash
java -jar target/northwind-app-1.0-SNAPSHOT.jar
```

## Database Schema

The application works with the Northwind database, which includes the following main tables:

- Customers
- Products
- Orders
- Order Details
- Suppliers
- Employees
- Shippers

## Troubleshooting

If you encounter any issues with the database connection:

1. Verify that your MySQL/MariaDB server is running.
2. Check that the database connection parameters in `DatabaseConnection.java` are correct.
3. Ensure that the Northwind database has been properly imported.
