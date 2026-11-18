package com.hospital.service;

import com.hospital.dao.PatientDAO;
import com.hospital.dao.impl.PatientDAOImpl;
import com.hospital.exception.EntityNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Patient;

import java.util.List;

/**
 * Service managing Patient business operations and domain validations.
 */
public class PatientService {
    private final PatientDAO patientDAO;

    public PatientService() {
        this.patientDAO = new PatientDAOImpl();
    }

    public PatientService(PatientDAO patientDAO) {
        this.patientDAO = patientDAO;
    }

    public Patient registerPatient(Patient patient) {
        validatePatient(patient);
        if (patientDAO.findByPhone(patient.getPhone()).isPresent()) {
            throw new ValidationException("A patient with phone number '" + patient.getPhone() + "' already exists.");
        }
        return patientDAO.save(patient);
    }

    public void updatePatient(Patient patient) {
        validatePatient(patient);
        patientDAO.findById(patient.getId())
                .orElseThrow(() -> new EntityNotFoundException("Patient", patient.getId()));
        patientDAO.update(patient);
    }

    public Patient getPatientById(int id) {
        return patientDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient", id));
    }

    public List<Patient> getAllPatients() {
        return patientDAO.findAll();
    }

    public List<Patient> searchPatients(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllPatients();
        }
        return patientDAO.searchByName(query.trim());
    }

    private void validatePatient(Patient p) {
        if (p.getName() == null || p.getName().trim().isEmpty()) {
            throw new ValidationException("Patient name cannot be empty.");
        }
        if (p.getPhone() == null || !p.getPhone().matches("\\d{7,15}")) {
            throw new ValidationException("Invalid phone number. Must be between 7 and 15 digits.");
        }
        if (p.getGender() == null || p.getGender().trim().isEmpty()) {
            throw new ValidationException("Patient gender is required.");
        }
    }
}
