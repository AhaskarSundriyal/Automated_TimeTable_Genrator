package com.yourorg.scheduler.domain;

public class MeetingTime {
    private String id;
    private String day;
    private String time;

    public MeetingTime(String id, String day, String time) {
        this.id = id;
        this.day = day;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getDay() {
        return day;
    }

    public String getTime() {
        return time;
    }

    @Override
    public String toString() {
        return day + " " + time;
    }
}
