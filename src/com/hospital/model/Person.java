package com.hospital.model;

/**
 * Abstract base class demonstrating OOP Inheritance and Encapsulation.
 * Doctor, Patient, and hospital staff share common personal attributes.
 */
public abstract class Person {
    private int id;
    private String name;
    private String gender;
    private String phone;
    private String email;

    public Person() {
    }

    public Person(int id, String name, String gender, String phone, String email) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
    }

    public Person(String name, String gender, String phone, String email) {
        this(0, name, gender, phone, email);
    }

    // Abstract method demonstrating Polymorphism (subclasses must supply role)
    public abstract String getRole();

    // Polymorphic display summary
    public String getContactInfo() {
        return String.format("%s | Phone: %s | Email: %s", name, phone, (email != null && !email.isEmpty() ? email : "N/A"));
    }

    // Encapsulation: Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Person name cannot be null or empty.");
        }
        this.name = name.trim();
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return String.format("[%s #%d] %s (Gender: %s, Phone: %s)", getRole(), id, name, gender, phone);
    }
}
