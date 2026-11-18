package com.hospital.dao.impl;

import com.hospital.config.DatabaseManager;
import com.hospital.dao.BillDAO;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Bill;
import com.hospital.model.PaymentStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BillDAOImpl implements BillDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String BASE_SELECT = """
        SELECT b.id, b.patient_id, p.name AS patient_name,
               b.appointment_id, b.admission_id,
               b.doctor_fee, b.room_charges, b.medication_charges,
               b.tax_amount, b.total_amount, b.payment_status,
               b.payment_method, b.created_at, b.paid_at
        FROM bills b
        JOIN patients p ON b.patient_id = p.id
    """;

    @Override
    public Bill save(Bill bill) {
        String sql = """
            INSERT INTO bills (patient_id, appointment_id, admission_id,
                               doctor_fee, room_charges, medication_charges,
                               tax_amount, total_amount, payment_status, payment_method, created_at, paid_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bill.getPatientId());
            if (bill.getAppointmentId() != null) ps.setInt(2, bill.getAppointmentId());
            else ps.setNull(2, Types.INTEGER);

            if (bill.getAdmissionId() != null) ps.setInt(3, bill.getAdmissionId());
            else ps.setNull(3, Types.INTEGER);

            ps.setDouble(4, bill.getDoctorFee());
            ps.setDouble(5, bill.getRoomCharges());
            ps.setDouble(6, bill.getMedicationCharges());
            ps.setDouble(7, bill.getTaxAmount());
            ps.setDouble(8, bill.getTotalAmount());
            ps.setString(9, bill.getPaymentStatus().name());
            ps.setString(10, bill.getPaymentMethod());
            ps.setString(11, bill.getFormattedCreatedAt());
            ps.setString(12, bill.getPaidAt() != null ? bill.getFormattedPaidAt() : null);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    bill.setId(rs.getInt(1));
                }
            }
            return bill;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save bill: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Bill bill) {
        String sql = """
            UPDATE bills
            SET patient_id = ?, appointment_id = ?, admission_id = ?,
                doctor_fee = ?, room_charges = ?, medication_charges = ?,
                tax_amount = ?, total_amount = ?, payment_status = ?,
                payment_method = ?, created_at = ?, paid_at = ?
            WHERE id = ?
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bill.getPatientId());
            if (bill.getAppointmentId() != null) ps.setInt(2, bill.getAppointmentId());
            else ps.setNull(2, Types.INTEGER);

            if (bill.getAdmissionId() != null) ps.setInt(3, bill.getAdmissionId());
            else ps.setNull(3, Types.INTEGER);

            ps.setDouble(4, bill.getDoctorFee());
            ps.setDouble(5, bill.getRoomCharges());
            ps.setDouble(6, bill.getMedicationCharges());
            ps.setDouble(7, bill.getTaxAmount());
            ps.setDouble(8, bill.getTotalAmount());
            ps.setString(9, bill.getPaymentStatus().name());
            ps.setString(10, bill.getPaymentMethod());
            ps.setString(11, bill.getFormattedCreatedAt());
            ps.setString(12, bill.getPaidAt() != null ? bill.getFormattedPaidAt() : null);
            ps.setInt(13, bill.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update bill: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Bill> findById(Integer id) {
        String sql = BASE_SELECT + " WHERE b.id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBill(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query bill by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Bill> findAll() {
        String sql = BASE_SELECT + " ORDER BY b.id DESC";
        List<Bill> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToBill(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list all bills: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Bill> findByPatientId(int patientId) {
        String sql = BASE_SELECT + " WHERE b.patient_id = ? ORDER BY b.id DESC";
        List<Bill> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBill(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query bills by patient: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Bill> findByStatus(PaymentStatus status) {
        String sql = BASE_SELECT + " WHERE b.payment_status = ? ORDER BY b.id DESC";
        List<Bill> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBill(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query bills by status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean markAsPaid(int billId, String paymentMethod, String paidAtTimestamp) {
        String sql = "UPDATE bills SET payment_status = 'PAID', payment_method = ?, paid_at = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentMethod);
            ps.setString(2, paidAtTimestamp);
            ps.setInt(3, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update bill payment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM bills WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete bill: " + e.getMessage(), e);
        }
    }

    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        PaymentStatus ps;
        try {
            ps = PaymentStatus.valueOf(rs.getString("payment_status"));
        } catch (Exception e) {
            ps = PaymentStatus.UNPAID;
        }

        LocalDateTime createdAt = null;
        LocalDateTime paidAt = null;
        try {
            String cStr = rs.getString("created_at");
            if (cStr != null) createdAt = LocalDateTime.parse(cStr, FORMATTER);
        } catch (Exception ignored) {}

        try {
            String pStr = rs.getString("paid_at");
            if (pStr != null) paidAt = LocalDateTime.parse(pStr, FORMATTER);
        } catch (Exception ignored) {}

        Integer aptId = rs.getInt("appointment_id");
        if (rs.wasNull()) aptId = null;

        Integer admId = rs.getInt("admission_id");
        if (rs.wasNull()) admId = null;

        return new Bill(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                aptId,
                admId,
                rs.getDouble("doctor_fee"),
                rs.getDouble("room_charges"),
                rs.getDouble("medication_charges"),
                rs.getDouble("tax_amount"),
                rs.getDouble("total_amount"),
                ps,
                rs.getString("payment_method"),
                createdAt,
                paidAt
        );
    }
}
