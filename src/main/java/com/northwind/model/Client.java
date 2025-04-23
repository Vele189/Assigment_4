package com.northwind.model;

import javafx.beans.property.*;

public class Client {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty email;
    private final StringProperty phone;
    private final StringProperty company;
    private final BooleanProperty active;
    private final StringProperty lastOrderDate;

    public Client(int id, String name, String email, String phone, String company, boolean active, String lastOrderDate) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.phone = new SimpleStringProperty(phone);
        this.company = new SimpleStringProperty(company);
        this.active = new SimpleBooleanProperty(active);
        this.lastOrderDate = new SimpleStringProperty(lastOrderDate);
    }

    // Getters and setters for all properties
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setName(String name) { this.name.set(name); }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }
    public void setEmail(String email) { this.email.set(email); }

    public String getPhone() { return phone.get(); }
    public StringProperty phoneProperty() { return phone; }
    public void setPhone(String phone) { this.phone.set(phone); }

    public String getCompany() { return company.get(); }
    public StringProperty companyProperty() { return company; }
    public void setCompany(String company) { this.company.set(company); }

    public boolean isActive() { return active.get(); }
    public BooleanProperty activeProperty() { return active; }
    public void setActive(boolean active) { this.active.set(active); }

    public String getLastOrderDate() { return lastOrderDate.get(); }
    public StringProperty lastOrderDateProperty() { return lastOrderDate; }
    public void setLastOrderDate(String lastOrderDate) { this.lastOrderDate.set(lastOrderDate); }

    @Override
    public String toString() {
        return name.get() + " (" + company.get() + ")";
    }
}
