package com.hospital.model;

/**
 * Room entity representing hospital beds/rooms.
 */
public class Room {
    private int id;
    private String roomNumber;
    private RoomType roomType;
    private double dailyRate;
    private String status; // AVAILABLE, OCCUPIED, MAINTENANCE

    public Room() {
        this.status = "AVAILABLE";
    }

    public Room(int id, String roomNumber, RoomType roomType, double dailyRate, String status) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.dailyRate = dailyRate;
        this.status = (status != null) ? status : "AVAILABLE";
    }

    public Room(String roomNumber, RoomType roomType, double dailyRate) {
        this(0, roomNumber, roomType, dailyRate, "AVAILABLE");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        if (dailyRate < 0) {
            throw new IllegalArgumentException("Daily rate cannot be negative.");
        }
        this.dailyRate = dailyRate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return String.format("Room %s [%s] - $%.2f/day (%s)",
                roomNumber, (roomType != null ? roomType.getDisplayName() : "Unknown"),
                dailyRate, status);
    }
}
