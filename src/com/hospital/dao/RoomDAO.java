package com.hospital.dao;

import com.hospital.model.Room;
import java.util.List;
import java.util.Optional;

public interface RoomDAO extends GenericDAO<Room, Integer> {
    Optional<Room> findByRoomNumber(String roomNumber);
    List<Room> findAvailableRooms();
    boolean updateStatus(int roomId, String status);
}
