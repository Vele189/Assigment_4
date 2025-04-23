package com.northwind.model;

import javafx.beans.property.*;

public class Employee {
    private final IntegerProperty id;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty address;
    private final StringProperty addressLine2;
    private final StringProperty city;
    private final StringProperty region;
    private final StringProperty postalCode;
    private final StringProperty phone;
    private final StringProperty office;
    private final BooleanProperty active;

    public Employee(int id, String firstName, String lastName, String address, 
                   String addressLine2, String city, String region, 
                   String postalCode, String phone, String office, boolean active) {
        this.id = new SimpleIntegerProperty(id);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.address = new SimpleStringProperty(address);
        this.addressLine2 = new SimpleStringProperty(addressLine2);
        this.city = new SimpleStringProperty(city);
        this.region = new SimpleStringProperty(region);
        this.postalCode = new SimpleStringProperty(postalCode);
        this.phone = new SimpleStringProperty(phone);
        this.office = new SimpleStringProperty(office);
        this.active = new SimpleBooleanProperty(active);
    }

    // Getters and setters for all properties
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public String getFirstName() { return firstName.get(); }
    public StringProperty firstNameProperty() { return firstName; }
    public void setFirstName(String firstName) { this.firstName.set(firstName); }

    public String getLastName() { return lastName.get(); }
    public StringProperty lastNameProperty() { return lastName; }
    public void setLastName(String lastName) { this.lastName.set(lastName); }

    public String getAddress() { return address.get(); }
    public StringProperty addressProperty() { return address; }
    public void setAddress(String address) { this.address.set(address); }

    public String getAddressLine2() { return addressLine2.get(); }
    public StringProperty addressLine2Property() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2.set(addressLine2); }

    public String getCity() { return city.get(); }
    public StringProperty cityProperty() { return city; }
    public void setCity(String city) { this.city.set(city); }

    public String getRegion() { return region.get(); }
    public StringProperty regionProperty() { return region; }
    public void setRegion(String region) { this.region.set(region); }

    public String getPostalCode() { return postalCode.get(); }
    public StringProperty postalCodeProperty() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode.set(postalCode); }

    public String getPhone() { return phone.get(); }
    public StringProperty phoneProperty() { return phone; }
    public void setPhone(String phone) { this.phone.set(phone); }

    public String getOffice() { return office.get(); }
    public StringProperty officeProperty() { return office; }
    public void setOffice(String office) { this.office.set(office); }

    public boolean isActive() { return active.get(); }
    public BooleanProperty activeProperty() { return active; }
    public void setActive(boolean active) { this.active.set(active); }
}