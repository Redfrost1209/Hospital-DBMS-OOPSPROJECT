package com.hospital.dao.impl;

import com.hospital.config.DatabaseManager;
import com.hospital.dao.AdmissionDAO;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Admission;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdmissionDAOImpl implements AdmissionDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String BASE_SELECT = """
        SELECT a.id, a.patient_id, p.name AS patient_name,
               a.room_id, r.room_number, a.admitted_at, a.discharged_at, a.status
        FROM admissions a
        JOIN patients p ON a.patient_id = p.id
        JOIN rooms r ON a.room_id = r.id
    """;

    @Override
    public Admission save(Admission admission) {
        String sql = """
            INSERT INTO admissions (patient_id, room_id, admitted_at, discharged_at, status)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, admission.getPatientId());
            ps.setInt(2, admission.getRoomId());
            ps.setString(3, admission.getFormattedAdmissionDate());
            ps.setString(4, admission.getDischargedAt() != null ? admission.getFormattedDischargeDate() : null);
            ps.setString(5, admission.getStatus());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    admission.setId(rs.getInt(1));
                }
            }
            return admission;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to record patient admission: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Admission admission) {
        String sql = """
            UPDATE admissions
            SET patient_id = ?, room_id = ?, admitted_at = ?, discharged_at = ?, status = ?
            WHERE id = ?
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, admission.getPatientId());
            ps.setInt(2, admission.getRoomId());
            ps.setString(3, admission.getFormattedAdmissionDate());
            ps.setString(4, admission.getDischargedAt() != null ? admission.getFormattedDischargeDate() : null);
            ps.setString(5, admission.getStatus());
            ps.setInt(6, admission.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update admission: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Admission> findById(Integer id) {
        String sql = BASE_SELECT + " WHERE a.id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAdmission(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query admission by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Admission> findActiveAdmissionByPatient(int patientId) {
        String sql = BASE_SELECT + " WHERE a.patient_id = ? AND a.status = 'ACTIVE'";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAdmission(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query active admission by patient: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Admission> findActiveAdmissions() {
        String sql = BASE_SELECT + " WHERE a.status = 'ACTIVE' ORDER BY a.admitted_at DESC";
        List<Admission> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAdmission(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list active admissions: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Admission> findAll() {
        String sql = BASE_SELECT + " ORDER BY a.id DESC";
        List<Admission> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAdmission(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list all admissions: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean dischargePatient(int admissionId, String dischargeTimestamp) {
        String sql = "UPDATE admissions SET discharged_at = ?, status = 'DISCHARGED' WHERE id = ? AND status = 'ACTIVE'";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dischargeTimestamp);
            ps.setInt(2, admissionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to mark admission as discharged: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM admissions WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete admission: " + e.getMessage(), e);
        }
    }

    private Admission mapResultSetToAdmission(ResultSet rs) throws SQLException {
        LocalDateTime admittedAt = null;
        LocalDateTime dischargedAt = null;
        try {
            String admStr = rs.getString("admitted_at");
            if (admStr != null) admittedAt = LocalDateTime.parse(admStr, FORMATTER);
        } catch (Exception ignored) {}

        try {
            String disStr = rs.getString("discharged_at");
            if (disStr != null) dischargedAt = LocalDateTime.parse(disStr, FORMATTER);
        } catch (Exception ignored) {}

        return new Admission(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                rs.getInt("room_id"),
                rs.getString("room_number"),
                admittedAt,
                dischargedAt,
                rs.getString("status")
        );
    }
}
