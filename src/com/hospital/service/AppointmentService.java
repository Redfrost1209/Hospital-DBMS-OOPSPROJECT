package com.hospital.service;

import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.impl.AppointmentDAOImpl;
import com.hospital.dao.impl.DoctorDAOImpl;
import com.hospital.dao.impl.PatientDAOImpl;
import com.hospital.exception.EntityNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.model.Doctor;

import java.util.List;

/**
 * Service managing Appointment booking, rescheduling, and status transitions.
 */
public class AppointmentService {
    private final AppointmentDAO appointmentDAO;
    private final DoctorDAO doctorDAO;
    private final PatientDAO patientDAO;

    public AppointmentService() {
        this.appointmentDAO = new AppointmentDAOImpl();
        this.doctorDAO = new DoctorDAOImpl();
        this.patientDAO = new PatientDAOImpl();
    }

    public AppointmentService(AppointmentDAO appointmentDAO, DoctorDAO doctorDAO, PatientDAO patientDAO) {
        this.appointmentDAO = appointmentDAO;
        this.doctorDAO = doctorDAO;
        this.patientDAO = patientDAO;
    }

    public Appointment bookAppointment(int patientId, int doctorId, String date, String timeSlot, String reason) {
        // Validate patient
        patientDAO.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient", patientId));

        // Validate doctor
        Doctor doc = doctorDAO.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor", doctorId));

        if (!doc.isAvailable()) {
            throw new ValidationException("Dr. " + doc.getName() + " is currently unavailable for bookings.");
        }

        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new ValidationException("Appointment date must follow format YYYY-MM-DD (e.g. 2026-10-30).");
        }

        if (timeSlot == null || timeSlot.trim().isEmpty()) {
            throw new ValidationException("Time slot is required (e.g. '10:30 AM').");
        }

        Appointment appt = new Appointment(patientId, doctorId, date, timeSlot.trim(), reason != null ? reason.trim() : "General Consultation");
        return appointmentDAO.save(appt);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentDAO.findAll();
    }

    public List<Appointment> getAppointmentsByPatient(int patientId) {
        return appointmentDAO.findByPatientId(patientId);
    }

    public List<Appointment> getAppointmentsByDoctor(int doctorId) {
        return appointmentDAO.findByDoctorId(doctorId);
    }

    public List<Appointment> getAppointmentsByDate(String date) {
        return appointmentDAO.findByDate(date);
    }

    public Appointment getAppointmentById(int id) {
        return appointmentDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment", id));
    }

    public void cancelAppointment(int appointmentId) {
        getAppointmentById(appointmentId);
        appointmentDAO.updateStatus(appointmentId, AppointmentStatus.CANCELLED);
    }

    public void completeAppointment(int appointmentId) {
        getAppointmentById(appointmentId);
        appointmentDAO.updateStatus(appointmentId, AppointmentStatus.COMPLETED);
    }
}
