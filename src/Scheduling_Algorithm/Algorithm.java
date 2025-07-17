package schedulerfx;

import java.util.List;

public interface Algorithm {
    void simulate(List<Process> processes, int timeQuantum);
    List<String> getGanttChart();
}
