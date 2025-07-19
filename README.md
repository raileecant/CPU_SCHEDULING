# This is our project on CPU_SCHEDULING

Project Overview: CPU Scheduling
This project is a desktop application built with Java Swing that provides an interactive, visual simulation of several common CPU scheduling algorithms. Its primary purpose is to serve as an educational tool for students and enthusiasts to understand, compare, and analyze the behavior and performance of different scheduling policies.

To run the Code
First, you need to populate the Process Input Queue. You have two options:
Manually: Enter a number in the Arrival Time and Burst Time fields, then click the Add Process button. The process will appear in the queue on the left.
Randomly: Click the Generate Random button. A dialog box will ask how many processes you want to create. Enter a number and click OK.
Configure Simulation 
Secondly, choose your algorithm and its parameters in the Algorithm & Settings panel at the bottom.
Select Algorithm: Choose an algorithm from the Algorithm dropdown menu (e.g., FCFS, Round Robin, MLFQ).
Set Parameters:
Quantum: This field is enabled only for Round Robin and MLFQ.
For Round Robin, enter a single number (e.g., 4).
For MLFQ, enter comma-separated numbers for each queue's quantum (e.g., 8,16,32).
Context Switch Delay: This field is enabled for preemptive algorithms (SRTF, Round Robin, MLFQ). Enter the time delay for each context switch (e.g., 1).
Run and Analyze
Lastly, once your processes and settings are ready, you can run the simulation.
Start Simulation: Click the Simulate button in the Controls panel.
Observe: Watch the simulation unfold in real-time.
The Gantt Chart will build from left to right, showing which process is running.
The Process Status & Results table on the right will update with the status and metrics for each process.
The Performance Metrics will show the running averages.
Stop/Pause: The Simulate button will change to Stop. You can click it anytime to pause the simulation.
Review Final Results: When the simulation finishes, the final metrics are displayed. You can then click Export CSV to save the detailed results to a file.
Reset: Click the Reset All button to clear all processes and results to start a new simulation.


1. FCFS (First-Come, First-Served): Processes run in the exact order they arrive. It's non-preemptive, meaning a process runs until it is completely finished.

2. SJF (Shortest Job First): Selects the waiting process with the smallest total burst time and runs it to completion. It is non-preemptive.

3. SRTF (Shortest Remaining Time First): A preemptive version of SJF. It always runs the process with the least time remaining. A running process can be interrupted if a new, shorter job arrives.

4. Round Robin (RR): Each process runs for a small, fixed time slice (quantum). If not finished, it's moved to the back of the queue to give other processes a turn, ensuring fairness.

5. MLFQ (Multi-Level Feedback Queue): Uses several priority queues. Processes start at high priority and are demoted to lower-priority queues if they use their full time slice. This favors short jobs while preventing long ones from starving.


<img width="1478" height="1011" alt="image" src="https://github.com/user-attachments/assets/ad0c8846-278b-49a1-b442-9d82025a0726" />


<img width="1479" height="1017" alt="image" src="https://github.com/user-attachments/assets/dfaa1f12-7d9c-4461-8d97-a6c8867e7e58" />


<img width="1481" height="1026" alt="image" src="https://github.com/user-attachments/assets/cac1dac8-65e0-40b2-89e1-b970dd5f386b" />


<img width="1477" height="1017" alt="image" src="https://github.com/user-attachments/assets/b67dab17-f9b4-4836-a2f3-13df82dddaaa" />


<img width="1479" height="1015" alt="image" src="https://github.com/user-attachments/assets/f3064843-3255-442f-a16a-cfe91684632f" />

So far we have yet to encounter a bug, but we have no time allotment.

Railee is based on editing the UI of the project.
Julian on the other hand is focused on Making the algorithm work


