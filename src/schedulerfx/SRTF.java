package src.schedulerfx;

import java.util.*;

public class SRTF implements Algorithm {
    private List<String> gantt = new ArrayList<>();

    @Override
    public void simulate(List<Process> processes, int tq) {
        int time = 0, completed = 0, n = processes.size();
        Process current = null;
        int minRemainingTime = Integer.MAX_VALUE;

        while (completed < n) {
            Process selected = null;
            for (Process p : processes) {
                if (p.arrivalTime <= time && p.remainingTime > 0) {
                    if (selected == null || p.remainingTime < selected.remainingTime)
                        selected = p;
                }
            }

            if (selected == null) {
                gantt.add("Idle");
                time++;
                continue;
            }

            if (selected.startTime == -1) selected.startTime = time;
            gantt.add(selected.id);
            selected.remainingTime--;
            time++;

            if (selected.remainingTime == 0) {
                selected.completionTime = time;
                selected.turnaroundTime = selected.completionTime - selected.arrivalTime;
                selected.waitingTime = selected.turnaroundTime - selected.burstTime;
                selected.responseTime = selected.startTime - selected.arrivalTime;
                completed++;
            }
        }
    }

    @Override
    public List<String> getGanttChart() {
        return gantt;
    }
}
