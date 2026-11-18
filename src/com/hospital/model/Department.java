package com.hospital.model;

/**
 * Department entity representing medical branches in the hospital.
 */
public class Department {
    private int id;
    private String name;
    private int floor;

    public Department() {
    }

    public Department(int id, String name, int floor) {
        this.id = id;
        this.name = name;
        this.floor = floor;
    }

    public Department(String name, int floor) {
        this(0, name, floor);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (Floor %d)", id, name, floor);
    }
}
