package schedulerfx;

import java.util.*;

public class SJF implements Algorithm {
    private List<String> gantt = new ArrayList<>();

    @Override
    public void simulate(List<Process> processes, int tq) {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        PriorityQueue<Process> pq = new PriorityQueue<>(Comparator.comparingInt(p -> p.burstTime));
        int time = 0, completed = 0, n = processes.size();
        int i = 0;

        while (completed < n) {
            while (i < n && processes.get(i).arrivalTime <= time)
                pq.add(processes.get(i++));
            if (pq.isEmpty()) {
                gantt.add("Idle");
                time++;
                continue;
            }
            Process p = pq.poll();
            p.startTime = time;
            for (int j = 0; j < p.burstTime; j++) gantt.add(p.id);
            time += p.burstTime;
            p.completionTime = time;
            p.turnaroundTime = p.completionTime - p.arrivalTime;
            p.waitingTime = p.turnaroundTime - p.burstTime;
            p.responseTime = p.startTime - p.arrivalTime;
            completed++;
        }
    }

    @Override
    public List<String> getGanttChart() {
        return gantt;
    }
}
