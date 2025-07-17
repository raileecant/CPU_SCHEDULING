package schedulerfx;

import java.util.*;

public class RoundRobin implements Algorithm {
    private List<String> gantt = new ArrayList<>();

    @Override
    public void simulate(List<Process> processes, int tq) {
        Queue<Process> queue = new LinkedList<>();
        int time = 0, completed = 0;
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int i = 0, n = processes.size();

        while (completed < n || !queue.isEmpty()) {
            while (i < n && processes.get(i).arrivalTime <= time)
                queue.add(processes.get(i++));

            if (queue.isEmpty()) {
                gantt.add("Idle");
                time++;
                continue;
            }

            Process p = queue.poll();
            if (p.startTime == -1) p.startTime = time;

            int slice = Math.min(tq, p.remainingTime);
            for (int j = 0; j < slice; j++) gantt.add(p.id);
            time += slice;
            p.remainingTime -= slice;

            while (i < n && processes.get(i).arrivalTime <= time)
                queue.add(processes.get(i++));

            if (p.remainingTime > 0)
                queue.add(p);
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
