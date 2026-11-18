package com.hospital.service;

import com.hospital.dao.AdmissionDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.RoomDAO;
import com.hospital.dao.impl.AdmissionDAOImpl;
import com.hospital.dao.impl.PatientDAOImpl;
import com.hospital.dao.impl.RoomDAOImpl;
import com.hospital.exception.EntityNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Admission;
import com.hospital.model.Room;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service managing Room allocations, Inpatient Admissions, and Discharges.
 */
public class RoomService {
    private final RoomDAO roomDAO;
    private final AdmissionDAO admissionDAO;
    private final PatientDAO patientDAO;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public RoomService() {
        this.roomDAO = new RoomDAOImpl();
        this.admissionDAO = new AdmissionDAOImpl();
        this.patientDAO = new PatientDAOImpl();
    }

    public RoomService(RoomDAO roomDAO, AdmissionDAO admissionDAO, PatientDAO patientDAO) {
        this.roomDAO = roomDAO;
        this.admissionDAO = admissionDAO;
        this.patientDAO = patientDAO;
    }

    public List<Room> getAllRooms() {
        return roomDAO.findAll();
    }

    public List<Room> getAvailableRooms() {
        return roomDAO.findAvailableRooms();
    }

    public Room getRoomById(int id) {
        return roomDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room", id));
    }

    public Admission admitPatient(int patientId, int roomId) {
        // Validate patient
        patientDAO.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient", patientId));

        // Check if patient is already admitted
        if (admissionDAO.findActiveAdmissionByPatient(patientId).isPresent()) {
            throw new ValidationException("Patient #" + patientId + " is already admitted to an active room.");
        }

        // Validate room
        Room room = roomDAO.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room", roomId));

        if (!room.isAvailable()) {
            throw new ValidationException("Room " + room.getRoomNumber() + " is currently " + room.getStatus() + ".");
        }

        // Create admission record
        Admission admission = new Admission(patientId, roomId);
        Admission savedAdmission = admissionDAO.save(admission);

        // Update room status to OCCUPIED
        roomDAO.updateStatus(roomId, "OCCUPIED");

        return savedAdmission;
    }

    public Admission dischargePatient(int admissionId) {
        Admission admission = admissionDAO.findById(admissionId)
                .orElseThrow(() -> new EntityNotFoundException("Admission", admissionId));

        if (!"ACTIVE".equalsIgnoreCase(admission.getStatus())) {
            throw new ValidationException("Admission #" + admissionId + " is already discharged.");
        }

        String dischargeTimestamp = LocalDateTime.now().format(FORMATTER);
        admissionDAO.dischargePatient(admissionId, dischargeTimestamp);
        roomDAO.updateStatus(admission.getRoomId(), "AVAILABLE");

        admission.setStatus("DISCHARGED");
        admission.setDischargedAt(LocalDateTime.now());
        return admission;
    }

    public List<Admission> getActiveAdmissions() {
        return admissionDAO.findActiveAdmissions();
    }

    public List<Admission> getAllAdmissions() {
        return admissionDAO.findAll();
    }

    public Admission getAdmissionById(int id) {
        return admissionDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Admission", id));
    }
}
