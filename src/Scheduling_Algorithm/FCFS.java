package src.Scheduling_Algorithm;

import java.util.*;

import bin.Process;

public class FCFS implements Algorithm {
    private List<String> gantt = new ArrayList<>();

    @Override
    public void simulate(List<Process> processes, int tq) {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int time = 0;
        for (Process p : processes) {
            if (time < p.arrivalTime) time = p.arrivalTime;
            p.startTime = time;
            for (int i = 0; i < p.burstTime; i++) gantt.add(p.id);
            time += p.burstTime;
            p.completionTime = time;
            p.turnaroundTime = p.completionTime - p.arrivalTime;
            p.waitingTime = p.turnaroundTime - p.burstTime;
            p.responseTime = p.startTime - p.arrivalTime;
        }
    }

    @Override
    public List<String> getGanttChart() {
        return gantt;
    }
}
