package com.yourorg.scheduler.domain;

import com.yourorg.scheduler.domain.*;
import java.util.*;

public class Data {
    private List<Room> rooms;
    private List<MeetingTime> meetingTimes;
    private List<Instructor> instructors;
    private List<Course> courses;
    private List<Department> departments;
    private List<Section> sections;

    public Data() {
        // Example hard-coded data. Replace with DB or file loading as needed:
        rooms = List.of(new Room("R1", 30), new Room("R2", 50));

        meetingTimes = List.of(
            new MeetingTime("MT1", "Mon", "09:00"),
            new MeetingTime("MT2", "Tue", "10:00"),
            new MeetingTime("MT3", "Wed", "11:00")
        );

        instructors = List.of(
            new Instructor("I1", "Alice"),
            new Instructor("I2", "Bob")
        );

        Course c1 = new Course("C1", "Algorithms", 30);
        c1.getInstructors().add(instructors.get(0));
        Course c2 = new Course("C2", "Databases", 30);
        c2.getInstructors().add(instructors.get(1));
        courses = List.of(c1, c2);

        Department d1 = new Department("D1", "Computer Science");
        d1.getCourses().addAll(courses);
        departments = List.of(d1);

        sections = List.of(new Section("S1", d1, 4));
    }

    // getters for all lists
    public List<Room> getRooms() { return rooms; }
    public List<MeetingTime> getMeetingTimes() { return meetingTimes; }
    public List<Instructor> getInstructors() { return instructors; }
    public List<Course> getCourses() { return courses; }
    public List<Department> getDepartments() { return departments; }
    public List<Section> getSections() { return sections; }
}
