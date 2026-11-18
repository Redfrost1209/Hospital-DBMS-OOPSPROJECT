package com.hospital.service;

import com.hospital.dao.DepartmentDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.impl.DepartmentDAOImpl;
import com.hospital.dao.impl.DoctorDAOImpl;
import com.hospital.exception.EntityNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Department;
import com.hospital.model.Doctor;

import java.util.List;

/**
 * Service managing Doctor and Department business operations.
 */
public class DoctorService {
    private final DoctorDAO doctorDAO;
    private final DepartmentDAO departmentDAO;

    public DoctorService() {
        this.doctorDAO = new DoctorDAOImpl();
        this.departmentDAO = new DepartmentDAOImpl();
    }

    public DoctorService(DoctorDAO doctorDAO, DepartmentDAO departmentDAO) {
        this.doctorDAO = doctorDAO;
        this.departmentDAO = departmentDAO;
    }

    public Doctor registerDoctor(Doctor doctor) {
        validateDoctor(doctor);
        departmentDAO.findById(doctor.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department", doctor.getDepartmentId()));
        return doctorDAO.save(doctor);
    }

    public Doctor getDoctorById(int id) {
        return doctorDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctor", id));
    }

    public List<Doctor> getAllDoctors() {
        return doctorDAO.findAll();
    }

    public List<Doctor> getAvailableDoctors() {
        return doctorDAO.findAvailableDoctors();
    }

    public List<Doctor> getDoctorsByDepartment(int departmentId) {
        departmentDAO.findById(departmentId)
                .orElseThrow(() -> new EntityNotFoundException("Department", departmentId));
        return doctorDAO.findByDepartment(departmentId);
    }

    public boolean toggleDoctorAvailability(int doctorId) {
        Doctor doc = getDoctorById(doctorId);
        boolean newStatus = !doc.isAvailable();
        doctorDAO.updateAvailability(doctorId, newStatus);
        return newStatus;
    }

    public List<Department> getAllDepartments() {
        return departmentDAO.findAll();
    }

    private void validateDoctor(Doctor doc) {
        if (doc.getName() == null || doc.getName().trim().isEmpty()) {
            throw new ValidationException("Doctor name cannot be empty.");
        }
        if (doc.getSpecialization() == null || doc.getSpecialization().trim().isEmpty()) {
            throw new ValidationException("Doctor specialization cannot be empty.");
        }
        if (doc.getConsultationFee() <= 0) {
            throw new ValidationException("Consultation fee must be greater than zero.");
        }
    }
}
