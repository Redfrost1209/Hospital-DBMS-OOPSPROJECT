package com.hospital.dao;

import com.hospital.model.Admission;
import java.util.List;
import java.util.Optional;

public interface AdmissionDAO extends GenericDAO<Admission, Integer> {
    Optional<Admission> findActiveAdmissionByPatient(int patientId);
    List<Admission> findActiveAdmissions();
    boolean dischargePatient(int admissionId, String dischargeTimestamp);
}
