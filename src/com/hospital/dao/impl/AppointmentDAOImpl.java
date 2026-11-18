package com.hospital.dao.impl;

import com.hospital.config.DatabaseManager;
import com.hospital.dao.AppointmentDAO;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppointmentDAOImpl implements AppointmentDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    private static final String BASE_SELECT = """
        SELECT a.id, a.patient_id, p.name AS patient_name,
               a.doctor_id, d.name AS doctor_name, d.specialization AS doctor_specialization,
               a.appointment_date, a.time_slot, a.status, a.reason
        FROM appointments a
        JOIN patients p ON a.patient_id = p.id
        JOIN doctors d ON a.doctor_id = d.id
    """;

    @Override
    public Appointment save(Appointment appointment) {
        String sql = """
            INSERT INTO appointments (patient_id, doctor_id, appointment_date, time_slot, status, reason)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDoctorId());
            ps.setString(3, appointment.getAppointmentDate());
            ps.setString(4, appointment.getTimeSlot());
            ps.setString(5, appointment.getStatus().name());
            ps.setString(6, appointment.getReason());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    appointment.setId(rs.getInt(1));
                }
            }
            return appointment;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Appointment appointment) {
        String sql = """
            UPDATE appointments
            SET patient_id = ?, doctor_id = ?, appointment_date = ?, time_slot = ?, status = ?, reason = ?
            WHERE id = ?
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDoctorId());
            ps.setString(3, appointment.getAppointmentDate());
            ps.setString(4, appointment.getTimeSlot());
            ps.setString(5, appointment.getStatus().name());
            ps.setString(6, appointment.getReason());
            ps.setInt(7, appointment.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Appointment> findById(Integer id) {
        String sql = BASE_SELECT + " WHERE a.id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAppointment(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query appointment by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Appointment> findAll() {
        String sql = BASE_SELECT + " ORDER BY a.appointment_date DESC, a.time_slot ASC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAppointment(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query all appointments: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Appointment> findByPatientId(int patientId) {
        String sql = BASE_SELECT + " WHERE a.patient_id = ? ORDER BY a.appointment_date DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query appointments by patient: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Appointment> findByDoctorId(int doctorId) {
        String sql = BASE_SELECT + " WHERE a.doctor_id = ? ORDER BY a.appointment_date DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query appointments by doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Appointment> findByDate(String date) {
        String sql = BASE_SELECT + " WHERE a.appointment_date = ? ORDER BY a.time_slot ASC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query appointments by date: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(int appointmentId, AppointmentStatus status) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update appointment status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM appointments WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete appointment: " + e.getMessage(), e);
        }
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        AppointmentStatus st;
        try {
            st = AppointmentStatus.valueOf(rs.getString("status"));
        } catch (Exception e) {
            st = AppointmentStatus.SCHEDULED;
        }
        return new Appointment(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                rs.getInt("doctor_id"),
                rs.getString("doctor_name"),
                rs.getString("doctor_specialization"),
                rs.getString("appointment_date"),
                rs.getString("time_slot"),
                st,
                rs.getString("reason")
        );
    }
}
