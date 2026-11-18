package com.hospital.model;

/**
 * Appointment entity encapsulating Doctor-Patient consultation schedules.
 */
public class Appointment {
    private int id;
    private int patientId;
    private String patientName;
    private int doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private String appointmentDate; // YYYY-MM-DD
    private String timeSlot;        // e.g. 10:00 AM
    private AppointmentStatus status;
    private String reason;

    public Appointment() {
        this.status = AppointmentStatus.SCHEDULED;
    }

    public Appointment(int id, int patientId, String patientName, int doctorId,
                       String doctorName, String doctorSpecialization,
                       String appointmentDate, String timeSlot,
                       AppointmentStatus status, String reason) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorSpecialization = doctorSpecialization;
        this.appointmentDate = appointmentDate;
        this.timeSlot = timeSlot;
        this.status = (status != null) ? status : AppointmentStatus.SCHEDULED;
        this.reason = reason;
    }

    public Appointment(int patientId, int doctorId, String appointmentDate, String timeSlot, String reason) {
        this(0, patientId, null, doctorId, null, null, appointmentDate, timeSlot, AppointmentStatus.SCHEDULED, reason);
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

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return String.format("Appointment #%d: Patient %s with Dr. %s on %s at %s [%s]",
                id, (patientName != null ? patientName : "#" + patientId),
                (doctorName != null ? doctorName : "#" + doctorId),
                appointmentDate, timeSlot, status);
    }
}
