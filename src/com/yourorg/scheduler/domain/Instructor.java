package com.yourorg.scheduler.domain;

public class Instructor {
    private String name;
    private String department;

    public Instructor(String name, String department) {
        this.name = name;
        this.department = department;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
