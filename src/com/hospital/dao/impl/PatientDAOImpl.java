package com.hospital.dao.impl;

import com.hospital.config.DatabaseManager;
import com.hospital.dao.PatientDAO;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Patient;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientDAOImpl implements PatientDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String BASE_SELECT = """
        SELECT id, name, gender, phone, email, blood_group, emergency_contact, medical_history, registered_at
        FROM patients
    """;

    @Override
    public Patient save(Patient patient) {
        String sql = """
            INSERT INTO patients (name, gender, phone, email, blood_group, emergency_contact, medical_history, registered_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, patient.getName());
            ps.setString(2, patient.getGender());
            ps.setString(3, patient.getPhone());
            ps.setString(4, patient.getEmail());
            ps.setString(5, patient.getBloodGroup());
            ps.setString(6, patient.getEmergencyContact());
            ps.setString(7, patient.getMedicalHistory());
            ps.setString(8, patient.getFormattedRegistrationDate());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    patient.setId(rs.getInt(1));
                }
            }
            return patient;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save patient: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Patient patient) {
        String sql = """
            UPDATE patients
            SET name = ?, gender = ?, phone = ?, email = ?, blood_group = ?,
                emergency_contact = ?, medical_history = ?
            WHERE id = ?
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patient.getName());
            ps.setString(2, patient.getGender());
            ps.setString(3, patient.getPhone());
            ps.setString(4, patient.getEmail());
            ps.setString(5, patient.getBloodGroup());
            ps.setString(6, patient.getEmergencyContact());
            ps.setString(7, patient.getMedicalHistory());
            ps.setInt(8, patient.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update patient: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Patient> findById(Integer id) {
        String sql = BASE_SELECT + " WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query patient by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Patient> findByPhone(String phone) {
        String sql = BASE_SELECT + " WHERE phone = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query patient by phone: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Patient> searchByName(String keyword) {
        String sql = BASE_SELECT + " WHERE LOWER(name) LIKE ? ORDER BY name";
        List<Patient> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPatient(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search patients by name: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Patient> findAll() {
        String sql = BASE_SELECT + " ORDER BY id DESC";
        List<Patient> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToPatient(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query all patients: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM patients WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete patient: " + e.getMessage(), e);
        }
    }

    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        LocalDateTime regDate;
        try {
            String dateStr = rs.getString("registered_at");
            regDate = (dateStr != null && !dateStr.isEmpty()) ? LocalDateTime.parse(dateStr, FORMATTER) : LocalDateTime.now();
        } catch (Exception e) {
            regDate = LocalDateTime.now();
        }

        return new Patient(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("gender"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("blood_group"),
                rs.getString("emergency_contact"),
                rs.getString("medical_history"),
                regDate
        );
    }
}
