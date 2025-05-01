

package com.yourorg.scheduler;

import com.yourorg.scheduler.domain.Data;
import com.yourorg.scheduler.ga.ClassSession;
import com.yourorg.scheduler.ga.GeneticAlgorithm;
import com.yourorg.scheduler.ga.Population;

public class SchedulerApp {
    public static void main(String[] args) {
        Data data = new Data();
        Population population = new Population(9, data);
        GeneticAlgorithm ga = new GeneticAlgorithm();

        int generation = 0;
        population.getSchedules().sort(
            (s1, s2) -> Double.compare(s2.getFitness(), s1.getFitness())
        );

        while (population.getSchedules().get(0).getFitness() < 1.0) {
            generation++;
            System.out.println("Gen #" + generation +
                " Best fitness: " + population.getSchedules().get(0).getFitness());
            population = ga.evolve(population);
            population.getSchedules().sort(
                (s1, s2) -> Double.compare(s2.getFitness(), s1.getFitness())
            );
        }

        System.out.println("Perfect timetable found in generation " + generation);
        for (ClassSession cs : population.getSchedules().get(0).getClasses()) {
            System.out.printf(
                "%s | %s | %s | %s | %s%n",
                cs.getSection().getId(),
                cs.getCourse().getName(),
                cs.getInstructor().getName(),
                cs.getRoom().getId(),
                cs.getTime().toString()
            );
        }
    }
}
