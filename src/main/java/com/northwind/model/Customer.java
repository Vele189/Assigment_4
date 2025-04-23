package com.northwind.model;

public class Customer {
    private int id;
    private String company;
    private String contactName;
    private String contactTitle;
    private String phone;
    
    public Customer(int id, String company, String contactName, String contactTitle, String phone) {
        this.id = id;
        this.company = company;
        this.contactName = contactName;
        this.contactTitle = contactTitle;
        this.phone = phone;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public String getContactName() {
        return contactName;
    }
    
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
    
    public String getContactTitle() {
        return contactTitle;
    }
    
    public void setContactTitle(String contactTitle) {
        this.contactTitle = contactTitle;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    @Override
    public String toString() {
        return company + " (" + contactName + ")";
    }
}
