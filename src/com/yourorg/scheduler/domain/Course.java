package com.yourorg.scheduler.domain;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private String id;
    private String name;
    private int maxStudents;
    private List<Instructor> instructors = new ArrayList<>();

    public Course(String id, String name, int maxStudents) {
        this.id = id;
        this.name = name;
        this.maxStudents = maxStudents;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMaxStudents() {
        return maxStudents;
    }

    public List<Instructor> getInstructors() {
        return instructors;
    }
}
