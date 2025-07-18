package src.Scheduling_Algorithm;
import java.util.*;

import bin.Process;

public class MLFQ implements Algorithm {
    private List<String> gantt = new ArrayList<>();

    @Override
    public void simulate(List<Process> processes, int tq) {
        @SuppressWarnings("unchecked")
        Queue<Process>[] queues = (Queue<Process>[]) new Queue[3];
        for (int i = 0; i < 3; i++) queues[i] = new LinkedList<>();

        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int time = 0, i = 0, completed = 0;

        while (completed < processes.size()) {
            while (i < processes.size() && processes.get(i).arrivalTime <= time)
                queues[0].add(processes.get(i++));

            Process p = null;
            int level = -1;

            for (int j = 0; j < 3; j++) {
                if (!queues[j].isEmpty()) {
                    p = queues[j].poll();
                    level = j;
                    break;
                }
            }

            if (p == null) {
                gantt.add("Idle");
                time++;
                continue;
            }

            if (p.startTime == -1) p.startTime = time;
            int slice = Math.min((int) Math.pow(2, level), p.remainingTime);

            for (int s = 0; s < slice; s++) gantt.add(p.id);
            time += slice;
            p.remainingTime -= slice;

            while (i < processes.size() && processes.get(i).arrivalTime <= time)
                queues[0].add(processes.get(i++));

            if (p.remainingTime > 0 && level < 2)
                queues[level + 1].add(p);
            else if (p.remainingTime > 0)
                queues[2].add(p);
            else {
                p.completionTime = time;
                p.turnaroundTime = p.completionTime - p.arrivalTime;
                p.waitingTime = p.turnaroundTime - p.burstTime;
                p.responseTime = p.startTime - p.arrivalTime;
                completed++;
            }
        }
    }

    @Override
    public List<String> getGanttChart() {
        return gantt;
    }
}
