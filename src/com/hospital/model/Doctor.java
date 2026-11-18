package com.hospital.model;

/**
 * Concrete subclass extending Person. Demonstrates Inheritance,
 * Constructor chaining (super), and Method Overriding.
 */
public class Doctor extends Person {
    private String specialization;
    private String qualification;
    private double consultationFee;
    private int departmentId;
    private String departmentName;
    private boolean available;

    public Doctor() {
        super();
        this.available = true;
    }

    public Doctor(int id, String name, String gender, String phone, String email,
                  String specialization, String qualification, double consultationFee,
                  int departmentId, String departmentName, boolean available) {
        super(id, name, gender, phone, email);
        this.specialization = specialization;
        this.qualification = qualification;
        this.consultationFee = consultationFee;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.available = available;
    }

    public Doctor(String name, String gender, String phone, String email,
                  String specialization, String qualification, double consultationFee,
                  int departmentId) {
        super(name, gender, phone, email);
        this.specialization = specialization;
        this.qualification = qualification;
        this.consultationFee = consultationFee;
        this.departmentId = departmentId;
        this.available = true;
    }

    @Override
    public String getRole() {
        return "Doctor";
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        if (consultationFee < 0) {
            throw new IllegalArgumentException("Consultation fee cannot be negative.");
        }
        this.consultationFee = consultationFee;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return String.format("Dr. %s (%s, %s) - Dept: %s | Fee: $%.2f | Status: %s",
                getName(), specialization, qualification,
                (departmentName != null ? departmentName : "Dept #" + departmentId),
                consultationFee, (available ? "Available" : "On Leave"));
    }
}
