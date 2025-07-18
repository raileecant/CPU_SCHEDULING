package bin;

import src.Scheduling_Algorithm.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ButtonPanel extends JPanel {
    public JButton generateBtn, enqueueBtn, clearBtn, runBtn, exportBtn;

    public ButtonPanel(SchedulerController controller) {
        setLayout(new FlowLayout());

        generateBtn = new JButton("Generate Random");
        enqueueBtn = new JButton("Enqueue");
        clearBtn = new JButton("Clear Output");
        runBtn = new JButton("Simulate");
        exportBtn = new JButton("Export");

        add(generateBtn);
        add(enqueueBtn);
        add(clearBtn);
        add(runBtn);
        add(exportBtn);

        runBtn.addActionListener(e -> {
            String selectedAlgo = controller.algorithmPanel.algorithmList.getSelectedItem().toString();
            int quantum = controller.algorithmPanel.quantumSlider.getValue();
            List<Process> processes = controller.tablePanel.getProcesses();

            Algorithm algo;
            switch (selectedAlgo) {
                case "FCFS": algo = new FCFS(); break;
                case "SJF": algo = new SJF(); break;
                case "SRTF": algo = new SRTF(); break;
                case "Round Robin": algo = new RoundRobin(); break;
                case "MLFQ": algo = new MLFQ(); break;
                default: JOptionPane.showMessageDialog(null, "Unknown algorithm"); return;
            }

            algo.simulate(processes, quantum);
            controller.outputPanel.updateGanttChart(algo.getGanttChart());
        });
    }
}