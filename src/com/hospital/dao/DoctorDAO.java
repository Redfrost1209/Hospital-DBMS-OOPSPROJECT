package com.hospital.dao;

import com.hospital.model.Doctor;
import java.util.List;

public interface DoctorDAO extends GenericDAO<Doctor, Integer> {
    List<Doctor> findByDepartment(int departmentId);
    List<Doctor> findAvailableDoctors();
    boolean updateAvailability(int doctorId, boolean available);
}
