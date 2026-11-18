package com.hospital.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Represents patient admission to a hospital room/ward.
 */
public class Admission {
    private int id;
    private int patientId;
    private String patientName;
    private int roomId;
    private String roomNumber;
    private LocalDateTime admittedAt;
    private LocalDateTime dischargedAt;
    private String status; // ACTIVE, DISCHARGED

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Admission() {
        this.admittedAt = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    public Admission(int id, int patientId, String patientName, int roomId, String roomNumber,
                     LocalDateTime admittedAt, LocalDateTime dischargedAt, String status) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.admittedAt = (admittedAt != null) ? admittedAt : LocalDateTime.now();
        this.dischargedAt = dischargedAt;
        this.status = (status != null) ? status : "ACTIVE";
    }

    public Admission(int patientId, int roomId) {
        this(0, patientId, null, roomId, null, LocalDateTime.now(), null, "ACTIVE");
    }

    public long calculateDaysStayed() {
        LocalDateTime end = (dischargedAt != null) ? dischargedAt : LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(admittedAt.toLocalDate(), end.toLocalDate());
        return (days <= 0) ? 1 : days;
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

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public LocalDateTime getAdmittedAt() {
        return admittedAt;
    }

    public void setAdmittedAt(LocalDateTime admittedAt) {
        this.admittedAt = admittedAt;
    }

    public LocalDateTime getDischargedAt() {
        return dischargedAt;
    }

    public void setDischargedAt(LocalDateTime dischargedAt) {
        this.dischargedAt = dischargedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFormattedAdmissionDate() {
        return (admittedAt != null) ? admittedAt.format(FORMATTER) : "N/A";
    }

    public String getFormattedDischargeDate() {
        return (dischargedAt != null) ? dischargedAt.format(FORMATTER) : "Still Admitted";
    }

    @Override
    public String toString() {
        return String.format("Admission #%d: Patient '%s' in Room '%s' since %s [%s]",
                id, (patientName != null ? patientName : "ID " + patientId),
                (roomNumber != null ? roomNumber : "ID " + roomId),
                getFormattedAdmissionDate(), status);
    }
}
