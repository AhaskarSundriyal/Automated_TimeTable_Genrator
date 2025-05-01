package com.yourorg.scheduler.ga;
import java.util.*;

public class GeneticAlgorithm {
    private static final double MUTATION_RATE = 0.05;
    private static final int ELITE_SCHEDULES = 1;
    private static final int TOURNAMENT_SIZE = 3;
    private Random rnd = new Random();

    public Population evolve(Population pop) {
        List<Schedule> newSchedules = new ArrayList<>();

        // 1. Elitism
        pop.getSchedules().sort(Comparator.comparingDouble(Schedule::getFitness).reversed());
        newSchedules.addAll(pop.getSchedules().subList(0, ELITE_SCHEDULES));

        // 2. Crossover
        while (newSchedules.size() < pop.getSchedules().size()) {
            Schedule s1 = tournamentSelection(pop);
            Schedule s2 = tournamentSelection(pop);
            newSchedules.add(crossover(s1, s2));
        }

        // 3. Mutation
        for (int i = ELITE_SCHEDULES; i < newSchedules.size(); i++) {
            mutate(newSchedules.get(i));
        }

        pop.setSchedules(newSchedules);
        return pop;
    }

    private Schedule tournamentSelection(Population pop) {
        List<Schedule> tournament = new ArrayList<>();
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            tournament.add(pop.getSchedules().get(rnd.nextInt(pop.getSchedules().size())));
        }
        tournament.sort(Comparator.comparingDouble(Schedule::getFitness).reversed());
        return tournament.get(0);
    }

    private Schedule crossover(Schedule s1, Schedule s2) {
        Schedule child = new Schedule(s1.getData()).initialize();

        for (int i = 0; i < child.getClasses().size(); i++) {
            if (rnd.nextBoolean())
                child.getClasses().set(i, s1.getClasses().get(i));
            else
                child.getClasses().set(i, s2.getClasses().get(i));
        }
        return child;
    }

    private void mutate(Schedule schedule) {
        for (int i = 0; i < schedule.getClasses().size(); i++) {
            if (rnd.nextDouble() < MUTATION_RATE) {
                ClassSession newCs = new Schedule(schedule.getData()).initialize().getClasses().get(i);
;
                schedule.getClasses().set(i, newCs);
            }
        }
    }
}
