package com.yourorg.scheduler.domain;

public class Section {
    private String id;
    private Department dept;
    private int numClassesPerWeek;

    public Section(String id, Department dept, int numClassesPerWeek) {
        this.id = id;
        this.dept = dept;
        this.numClassesPerWeek = numClassesPerWeek;
    }

    public String getId() {
        return id;
    }

    public Department getDept() {
        return dept;
    }

    public int getNumClassesPerWeek() {
        return numClassesPerWeek;
    }
}
