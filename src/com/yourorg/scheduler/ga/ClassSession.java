package com.yourorg.scheduler.ga;

import com.yourorg.scheduler.domain.*;

public class ClassSession {
    private int id;
    private Section section;
    private Course course;
    private Instructor instructor;
    private Room room;
    private MeetingTime time;

    public ClassSession(int id, Section section, Course course) {
        this.id = id;
        this.section = section;
        this.course = course;
    }

    // getters and setters

    public int getId() {
        return id;
    }

    public Section getSection() {
        return section;
    }

    public Course getCourse() {
        return course;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public MeetingTime getTime() {
        return time;
    }

    public void setTime(MeetingTime time) {
        this.time = time;
    }
}
