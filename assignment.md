Here's a comprehensive rundown of the COS 221 Practical Assignment 4 without missing any important details:

# COS 221 Practical Assignment 4 (2025) - Complete Breakdown

## Key Information
- **Issue Date**: April 4, 2025
- **Due Date**: April 23, 2025 (before 11:00 AM)
- **Submission**: Upload to ClickUP as zip/tar archive
- **Total Marks**: 70 (plus 5 bonus marks possible)
- **Work Format**: Individual or pairs

## Introduction
This assignment requires you to work with the Northwind Sample Trading database, which models an international food trading company. The database contains tables for customers, orders, products, suppliers, employees, categories, etc.

## Learning Objectives
- Model an existing database
- Analyze database extensions from other sources
- Build a Graphical User Interface (GUI)
- Use the GUI to query and manipulate a relational database

## Task 1: Database Setup (4 marks)
- Download the Northwind database from the provided Google Drive link
- Import it into MariaDB
- Name it "uXXXXXXXX_northwind" where XXXXXXXX is your student number
- If working in pairs, include both student numbers in the database name

## Task 2: Pre-Packaged Application (4 marks)
- Select and use a database tool to explore the schema
- Specify which application you're using (2 marks)
- Provide a screenshot showing the imported tables with database name visible (2 marks)

## Task 3: ER Diagram (6 marks)
- Generate an Entity-Relationship diagram using your chosen tool (2 marks)
- Explore and explain the Products table structure, including:
  - Data the relation holds
  - Data types
  - Constraints present
  - How it links to other relations in the schema (4 marks)

## Task 4: Java GUI Implementation (46 marks)
Create a GUI with at least 4 tabs:

### Employees Tab
- Table component showing employee details (first name, last name, address, address line 2, city, region, postal code, phone, office, active status) (6 marks)
- Textbox to filter results by name or city (4 marks)

### Products Tab
- Button to trigger a popup for adding new products (8 marks)
- Popup must include dropdown menus for selecting supplier and category
- Table should reload to show the new product after addition

### Report Tab
- Generate a consolidated report showing the number of products in each warehouse for each category (6 marks)
- Report should include warehouse name, category name, and product count

### Notifications Tab
- Functionality to create, update, delete, and list all clients (17 marks)
- Table of inactive clients (those who stopped placing orders) (5 marks)
- Searchable list of all clients (past and present)

## Task 5: Security Implementation (5 marks)
- Modify your application to use environment variables for database credentials:
  - dvdrental_DB_PROTO
  - dvdrental_DB_HOST
  - dvdrental_DB_PORT
  - dvdrental_DB_NAME
  - dvdrental_DB_USERNAME
  - dvdrental_DB_PASSWORD

## Task 6: Bonus Marks (5 marks)
- Using git with minimum 8 commits on GitHub (3 marks)
- Using advanced, non-standard SQL in your application (2 marks)
  - Note: SQL from lectures 1-17 is considered standard

## Submission Requirements
Your submission archive must include:
1. Your Java GUI project files
2. A PDF with answers to tasks where required
3. A readme.txt file explaining how to build, connect, and execute your application

## Marking Rubric Summary
- Database setup: 4 marks
- Canned application: 4 marks
- ER diagram: 6 marks
- GUI implementation: 46 marks
- Security credentials: 5 marks
- Bonus marks: 5 marks

The assignment encourages the use of build tools like Maven and stresses the importance of planning your time well to complete all requirements.