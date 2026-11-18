package com.hospital.dao.impl;

import com.hospital.config.DatabaseManager;
import com.hospital.dao.DoctorDAO;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DoctorDAOImpl implements DoctorDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    private static final String BASE_SELECT = """
        SELECT d.id, d.name, d.gender, d.phone, d.email, d.specialization,
               d.qualification, d.consultation_fee, d.department_id, d.is_available,
               dep.name AS department_name
        FROM doctors d
        JOIN departments dep ON d.department_id = dep.id
    """;

    @Override
    public Doctor save(Doctor doctor) {
        String sql = """
            INSERT INTO doctors (name, gender, phone, email, specialization, qualification, consultation_fee, department_id, is_available)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, doctor.getName());
            ps.setString(2, doctor.getGender());
            ps.setString(3, doctor.getPhone());
            ps.setString(4, doctor.getEmail());
            ps.setString(5, doctor.getSpecialization());
            ps.setString(6, doctor.getQualification());
            ps.setDouble(7, doctor.getConsultationFee());
            ps.setInt(8, doctor.getDepartmentId());
            ps.setInt(9, doctor.isAvailable() ? 1 : 0);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    doctor.setId(rs.getInt(1));
                }
            }
            return doctor;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Doctor doctor) {
        String sql = """
            UPDATE doctors
            SET name = ?, gender = ?, phone = ?, email = ?, specialization = ?,
                qualification = ?, consultation_fee = ?, department_id = ?, is_available = ?
            WHERE id = ?
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctor.getName());
            ps.setString(2, doctor.getGender());
            ps.setString(3, doctor.getPhone());
            ps.setString(4, doctor.getEmail());
            ps.setString(5, doctor.getSpecialization());
            ps.setString(6, doctor.getQualification());
            ps.setDouble(7, doctor.getConsultationFee());
            ps.setInt(8, doctor.getDepartmentId());
            ps.setInt(9, doctor.isAvailable() ? 1 : 0);
            ps.setInt(10, doctor.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Doctor> findById(Integer id) {
        String sql = BASE_SELECT + " WHERE d.id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDoctor(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find doctor by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Doctor> findAll() {
        String sql = BASE_SELECT + " ORDER BY d.id";
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToDoctor(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query all doctors: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Doctor> findByDepartment(int departmentId) {
        String sql = BASE_SELECT + " WHERE d.department_id = ? ORDER BY d.id";
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDoctor(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query doctors by department: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Doctor> findAvailableDoctors() {
        String sql = BASE_SELECT + " WHERE d.is_available = 1 ORDER BY d.name";
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToDoctor(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query available doctors: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateAvailability(int doctorId, boolean available) {
        String sql = "UPDATE doctors SET is_available = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, available ? 1 : 0);
            ps.setInt(2, doctorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update doctor availability: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM doctors WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete doctor: " + e.getMessage(), e);
        }
    }

    private Doctor mapResultSetToDoctor(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("gender"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("specialization"),
                rs.getString("qualification"),
                rs.getDouble("consultation_fee"),
                rs.getInt("department_id"),
                rs.getString("department_name"),
                rs.getInt("is_available") == 1
        );
    }
}
