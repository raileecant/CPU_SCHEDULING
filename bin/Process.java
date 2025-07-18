package bin;

public class Process {
    public String id;
    public int arrivalTime, burstTime, priority;
    public int remainingTime;
    public int startTime = -1;
    public int completionTime;
    public int turnaroundTime;
    public int waitingTime;
    public int responseTime;

    public Process(String id, int arrivalTime, int burstTime, int priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
    }

    public Process(Process p) {
        this.id = p.id;
        this.arrivalTime = p.arrivalTime;
        this.burstTime = p.burstTime;
        this.remainingTime = p.remainingTime;
        this.priority = p.priority;
    }

    // JavaBean getters for TableView
    public String getId() { return id; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getPriority() { return priority; }
}
