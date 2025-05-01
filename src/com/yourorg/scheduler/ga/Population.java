package com.yourorg.scheduler.ga;
import com.yourorg.scheduler.domain.Data;
import java.util.*;
import java.util.stream.*;

public class Population {
    private List<Schedule> schedules;

    public Population(int size, Data data) {
        schedules = IntStream.range(0, size)
            .mapToObj(i -> new Schedule(data).initialize())
            .collect(Collectors.toList());
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }
}
