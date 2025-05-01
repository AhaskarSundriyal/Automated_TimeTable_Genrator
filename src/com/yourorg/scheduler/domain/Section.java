package com.yourorg.scheduler.domain;

public class Section {
    private String sectionName;
    private String instructor;

    public Section(String sectionName, String instructor) {
        this.sectionName = sectionName;
        this.instructor = instructor;
    }

    // Getters and Setters
    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }
}
