package src.Scheduling_Algorithm;

import java.util.List;

import bin.Process;

public interface Algorithm {
    void simulate(List<Process> processes, int timeQuantum);
    List<String> getGanttChart();
}
