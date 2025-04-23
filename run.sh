#!/bin/bash
echo "Building Northwind Application..."
mvn clean package
echo "Running Northwind Application..."
mvn javafx:run
