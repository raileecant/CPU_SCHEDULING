package src.schedulerfx;
import javax.swing.*;

public class OutputPanel extends JPanel {
    public JTextArea actionMessage, ganttChartArea;
    public JLabel avgWaitTime, avgExecTime, totalExecTime, cpuLabel, nextQueueLabel;
    public JProgressBar progressBar;

    public OutputPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        actionMessage = new JTextArea(10, 80);
        ganttChartArea = new JTextArea(5, 80);
        actionMessage.setEditable(false);
        ganttChartArea.setEditable(false);
        ganttChartArea.setBorder(BorderFactory.createTitledBorder("Gantt Chart"));

        avgWaitTime = new JLabel("Average Waiting Time: --");
        avgExecTime = new JLabel("Average Exec Time: --");
        totalExecTime = new JLabel("Total Execution Time: --");
        cpuLabel = new JLabel("CPU: --");
        nextQueueLabel = new JLabel("Next Queue: --");

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.add(avgWaitTime);
        statsPanel.add(avgExecTime);
        statsPanel.add(totalExecTime);
        statsPanel.add(cpuLabel);
        statsPanel.add(nextQueueLabel);

        progressBar = new JProgressBar(0, 100);

        add(new JScrollPane(actionMessage));
        add(new JScrollPane(ganttChartArea));
        add(statsPanel);
        add(progressBar);
    }
}
