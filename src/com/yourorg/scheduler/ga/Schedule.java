package com.yourorg.scheduler.ga;
import com.yourorg.scheduler.domain.*;
import java.util.*;

public class Schedule {
    private static Random rnd = new Random();
    private Data data;
    private List<ClassSession> classes;
    private double fitness = -1;
    private int conflicts = 0;

    public Schedule(Data data) {
        this.data = data;
        this.classes = new ArrayList<>();
    }

    public Schedule initialize() {
        int classId = 0;
        for (Section sec : data.getSections()) {
            for (int i = 0; i < sec.getNumClassesPerWeek(); i++) {
                ClassSession cs = new ClassSession(classId++, sec,
                    sec.getDept().getCourses().get(i % sec.getDept().getCourses().size()));
                cs.setTime(data.getMeetingTimes().get(rnd.nextInt(data.getMeetingTimes().size())));
                cs.setRoom(data.getRooms().get(rnd.nextInt(data.getRooms().size())));
                cs.setInstructor(
                    cs.getCourse().getInstructors()
                      .get(rnd.nextInt(cs.getCourse().getInstructors().size()))
                );
                classes.add(cs);
            }
        }
        return this;
    }

    public double calculateFitness() {
        conflicts = 0;
        for (int i = 0; i < classes.size(); i++) {
            ClassSession c1 = classes.get(i);
            if (c1.getRoom().getCapacity() < c1.getCourse().getMaxStudents()) {
                conflicts++;
            }
            for (int j = i + 1; j < classes.size(); j++) {
                ClassSession c2 = classes.get(j);
                if (c1.getTime().equals(c2.getTime())) {
                    if (c1.getRoom().equals(c2.getRoom())) conflicts++;
                    if (c1.getInstructor().equals(c2.getInstructor())) conflicts++;
                }
            }
        }
        fitness = 1.0 / (1 + conflicts);
        return fitness;
    }

    public double getFitness() {
        if (fitness < 0) calculateFitness();
        return fitness;
    }

    public List<ClassSession> getClasses() {
        return classes;
    }
    public Data getData() {
        return this.data;
    }
    
}
