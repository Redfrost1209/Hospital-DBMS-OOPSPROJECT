package com.hospital.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Bill entity representing an invoice. Implements Billable interface,
 * demonstrating Polymorphism.
 */
public class Bill implements Billable {
    private int id;
    private int patientId;
    private String patientName;
    private Integer appointmentId;
    private Integer admissionId;
    private double doctorFee;
    private double roomCharges;
    private double medicationCharges;
    private double taxAmount;
    private double totalAmount;
    private PaymentStatus paymentStatus;
    private String paymentMethod; // CASH, CARD, UPI, INSURANCE
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final double TAX_RATE = 0.05; // 5% GST/Service tax

    public Bill() {
        this.paymentStatus = PaymentStatus.UNPAID;
        this.createdAt = LocalDateTime.now();
    }

    public Bill(int id, int patientId, String patientName, Integer appointmentId, Integer admissionId,
                double doctorFee, double roomCharges, double medicationCharges,
                double taxAmount, double totalAmount, PaymentStatus paymentStatus,
                String paymentMethod, LocalDateTime createdAt, LocalDateTime paidAt) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.appointmentId = appointmentId;
        this.admissionId = admissionId;
        this.doctorFee = doctorFee;
        this.roomCharges = roomCharges;
        this.medicationCharges = medicationCharges;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.paymentStatus = (paymentStatus != null) ? paymentStatus : PaymentStatus.UNPAID;
        this.paymentMethod = paymentMethod;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
        this.paidAt = paidAt;
    }

    // Builder or factory calculation
    public void calculateTotalWithTax() {
        double subtotal = calculateSubtotal();
        this.taxAmount = Math.round((subtotal * TAX_RATE) * 100.0) / 100.0;
        this.totalAmount = Math.round((subtotal + this.taxAmount) * 100.0) / 100.0;
    }

    @Override
    public double calculateSubtotal() {
        return doctorFee + roomCharges + medicationCharges;
    }

    @Override
    public String getBillingDescription() {
        StringBuilder sb = new StringBuilder();
        if (doctorFee > 0) sb.append(String.format("Doctor Consultation: $%.2f; ", doctorFee));
        if (roomCharges > 0) sb.append(String.format("Room Stay Charges: $%.2f; ", roomCharges));
        if (medicationCharges > 0) sb.append(String.format("Medications/Tests: $%.2f; ", medicationCharges));
        sb.append(String.format("Taxes (5%%): $%.2f", taxAmount));
        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Integer getAdmissionId() {
        return admissionId;
    }

    public void setAdmissionId(Integer admissionId) {
        this.admissionId = admissionId;
    }

    public double getDoctorFee() {
        return doctorFee;
    }

    public void setDoctorFee(double doctorFee) {
        this.doctorFee = doctorFee;
    }

    public double getRoomCharges() {
        return roomCharges;
    }

    public void setRoomCharges(double roomCharges) {
        this.roomCharges = roomCharges;
    }

    public double getMedicationCharges() {
        return medicationCharges;
    }

    public void setMedicationCharges(double medicationCharges) {
        this.medicationCharges = medicationCharges;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getFormattedCreatedAt() {
        return (createdAt != null) ? createdAt.format(FORMATTER) : "N/A";
    }

    public String getFormattedPaidAt() {
        return (paidAt != null) ? paidAt.format(FORMATTER) : "Unpaid";
    }

    @Override
    public String toString() {
        return String.format("Invoice #%d | Patient: %s | Total: $%.2f | Status: %s",
                id, (patientName != null ? patientName : "ID " + patientId), totalAmount, paymentStatus);
    }
}
