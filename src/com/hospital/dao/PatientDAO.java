package com.hospital.dao;

import com.hospital.model.Patient;
import java.util.List;
import java.util.Optional;

public interface PatientDAO extends GenericDAO<Patient, Integer> {
    Optional<Patient> findByPhone(String phone);
    List<Patient> searchByName(String keyword);
}
