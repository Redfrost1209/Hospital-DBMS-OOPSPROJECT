package com.hospital.dao;

import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import java.util.List;

public interface AppointmentDAO extends GenericDAO<Appointment, Integer> {
    List<Appointment> findByPatientId(int patientId);
    List<Appointment> findByDoctorId(int doctorId);
    List<Appointment> findByDate(String date);
    boolean updateStatus(int appointmentId, AppointmentStatus status);
}
