package com.hospital.dao.impl;

import com.hospital.config.DatabaseManager;
import com.hospital.dao.DepartmentDAO;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartmentDAOImpl implements DepartmentDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    @Override
    public Department save(Department department) {
        String sql = "INSERT INTO departments (name, floor) VALUES (?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, department.getName());
            ps.setInt(2, department.getFloor());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    department.setId(rs.getInt(1));
                }
            }
            return department;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save department: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Department department) {
        String sql = "UPDATE departments SET name = ?, floor = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, department.getName());
            ps.setInt(2, department.getFloor());
            ps.setInt(3, department.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update department: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Department> findById(Integer id) {
        String sql = "SELECT id, name, floor FROM departments WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDepartment(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query department by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Department> findByName(String name) {
        String sql = "SELECT id, name, floor FROM departments WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDepartment(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query department by name: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Department> findAll() {
        String sql = "SELECT id, name, floor FROM departments ORDER BY id";
        List<Department> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToDepartment(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list departments: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM departments WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete department (might have doctors assigned): " + e.getMessage(), e);
        }
    }

    private Department mapResultSetToDepartment(ResultSet rs) throws SQLException {
        return new Department(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("floor")
        );
    }
}
