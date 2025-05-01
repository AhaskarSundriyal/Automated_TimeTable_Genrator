package com.yourorg.scheduler.domain;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Data {
    
    // Fetching Room data from the database
    public List<Room> getRooms() {
        List<Room> rooms = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT * FROM classrooms";  // Adjust your SQL query here
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Loop through the result set and populate the Room objects
            while (rs.next()) {
                String name = rs.getString("name");
                int capacity = rs.getInt("capacity");
                rooms.add(new Room(name, capacity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rooms;
    }

    // Fetching MeetingTime data from the database
    public List<MeetingTime> getMeetingTimes() {
        List<MeetingTime> times = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT * FROM meeting_times";  // Adjust the query for meeting times
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Loop through and populate the MeetingTime objects
            while (rs.next()) {
                String time = rs.getString("time");
                times.add(new MeetingTime(time));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return times;
    }

    // Fetching Instructor data from the database
    public List<Instructor> getInstructors() {
        List<Instructor> instructors = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT * FROM instructors";  // Adjust for instructors table
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Loop through and populate the Instructor objects
            while (rs.next()) {
                String name = rs.getString("name");
                String department = rs.getString("department");
                instructors.add(new Instructor(name, department));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instructors;
    }

    // Fetching Course data from the database
    public List<Course> getCourses() {
        List<Course> courses = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT * FROM courses";  // Adjust the query for courses
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Loop through and populate the Course objects
            while (rs.next()) {
                String courseName = rs.getString("course_name");
                String courseCode = rs.getString("course_code");
                courses.add(new Course(courseName, courseCode));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return courses;
    }

    // Fetching Section data from the database
    public List<Section> getSections() {
        List<Section> sections = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT * FROM sections";  // Adjust the query for sections
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Loop through and populate the Section objects
            while (rs.next()) {
                String sectionName = rs.getString("section_name");
                String instructor = rs.getString("instructor");
                sections.add(new Section(sectionName, instructor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sections;
    }
}
