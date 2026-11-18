package com.hospital.dao.impl;

import com.hospital.config.DatabaseManager;
import com.hospital.dao.RoomDAO;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Room;
import com.hospital.model.RoomType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDAOImpl implements RoomDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    @Override
    public Room save(Room room) {
        String sql = "INSERT INTO rooms (room_number, room_type, daily_rate, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setDouble(3, room.getDailyRate());
            ps.setString(4, room.getStatus());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    room.setId(rs.getInt(1));
                }
            }
            return room;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save room: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Room room) {
        String sql = "UPDATE rooms SET room_number = ?, room_type = ?, daily_rate = ?, status = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setDouble(3, room.getDailyRate());
            ps.setString(4, room.getStatus());
            ps.setInt(5, room.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update room: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Room> findById(Integer id) {
        String sql = "SELECT id, room_number, room_type, daily_rate, status FROM rooms WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRoom(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query room by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Room> findByRoomNumber(String roomNumber) {
        String sql = "SELECT id, room_number, room_type, daily_rate, status FROM rooms WHERE UPPER(room_number) = UPPER(?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRoom(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query room by number: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Room> findAvailableRooms() {
        String sql = "SELECT id, room_number, room_type, daily_rate, status FROM rooms WHERE status = 'AVAILABLE' ORDER BY room_number";
        List<Room> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToRoom(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query available rooms: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Room> findAll() {
        String sql = "SELECT id, room_number, room_type, daily_rate, status FROM rooms ORDER BY room_number";
        List<Room> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToRoom(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query all rooms: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(int roomId, String status) {
        String sql = "UPDATE rooms SET status = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update room status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete room: " + e.getMessage(), e);
        }
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        RoomType rt;
        try {
            rt = RoomType.valueOf(rs.getString("room_type"));
        } catch (Exception e) {
            rt = RoomType.GENERAL;
        }
        return new Room(
                rs.getInt("id"),
                rs.getString("room_number"),
                rt,
                rs.getDouble("daily_rate"),
                rs.getString("status")
        );
    }
}
