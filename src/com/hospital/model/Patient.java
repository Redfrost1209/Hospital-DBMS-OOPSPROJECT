package com.hospital.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Concrete subclass extending Person. Demonstrates Inheritance,
 * Encapsulation, and Polymorphism.
 */
public class Patient extends Person {
    private String bloodGroup;
    private String emergencyContact;
    private String medicalHistory;
    private LocalDateTime registeredAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Patient() {
        super();
        this.registeredAt = LocalDateTime.now();
    }

    public Patient(int id, String name, String gender, String phone, String email,
                   String bloodGroup, String emergencyContact, String medicalHistory,
                   LocalDateTime registeredAt) {
        super(id, name, gender, phone, email);
        this.bloodGroup = bloodGroup;
        this.emergencyContact = emergencyContact;
        this.medicalHistory = medicalHistory;
        this.registeredAt = (registeredAt != null) ? registeredAt : LocalDateTime.now();
    }

    public Patient(String name, String gender, String phone, String email,
                   String bloodGroup, String emergencyContact, String medicalHistory) {
        super(name, gender, phone, email);
        this.bloodGroup = bloodGroup;
        this.emergencyContact = emergencyContact;
        this.medicalHistory = medicalHistory;
        this.registeredAt = LocalDateTime.now();
    }

    @Override
    public String getRole() {
        return "Patient";
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getFormattedRegistrationDate() {
        return (registeredAt != null) ? registeredAt.format(FORMATTER) : "N/A";
    }

    @Override
    public String toString() {
        return String.format("Patient #%d: %s (Blood: %s, Phone: %s, Emergency: %s)",
                getId(), getName(), bloodGroup, getPhone(), emergencyContact);
    }
}
