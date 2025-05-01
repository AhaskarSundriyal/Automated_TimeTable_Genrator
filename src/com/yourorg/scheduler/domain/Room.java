package com.yourorg.scheduler.domain;

public class Room {
    private String id;
    private int capacity;

    public Room(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }
}
