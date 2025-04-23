package com.northwind.model;

import java.time.LocalDate;

public class Order {
    private int id;
    private String customerName;
    private LocalDate orderDate;
    private LocalDate shippedDate;
    private double total;
    
    public Order(int id, String customerName, LocalDate orderDate, LocalDate shippedDate, double total) {
        this.id = id;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.shippedDate = shippedDate;
        this.total = total;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public LocalDate getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
    
    public LocalDate getShippedDate() {
        return shippedDate;
    }
    
    public void setShippedDate(LocalDate shippedDate) {
        this.shippedDate = shippedDate;
    }
    
    public double getTotal() {
        return total;
    }
    
    public void setTotal(double total) {
        this.total = total;
    }
    
    @Override
    public String toString() {
        return "Order #" + id + " - " + customerName + " (" + orderDate + ")";
    }
}
