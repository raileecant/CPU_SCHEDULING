package src.GUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;



public class Shedulerui extends JFrame {

    private JTextField arrivalField, burstField, rrQuantumField, contextSwitchField;
    private JButton addProcessButton, generateRandomButton, removeProcessButton;
    private JButton simulateButton, resetAllButton, exportButton;
    private JComboBox<String> algorithmComboBox;
    private JTable inputQueueTable, resultsTable;
    private DefaultTableModel inputQueueModel, resultsModel;
    private JLabel avgTurnaroundLabel, avgWaitingLabel, avgResponseLabel, totalTimeLabel, statusLabel;
    private JLabel quantumLabel;
    private JPanel ganttChartPanel;
    private JPanel timeAxisPanel;
    private JScrollPane ganttScrollPane;

    // --- Data & Simulation State ---
    private final List<Process> processList = new ArrayList<>();
    private int processCounter = 1;
    private javax.swing.Timer simulationTimer;
    private SchedulerState currentState;
    private StepBasedScheduler currentAlgorithm;

    public Shedulerui() {
        // --- Main Frame Setup ---
        setTitle("CPU Scheduling Simulator 3.9 (Live)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception e) { e.printStackTrace(); }
        setSize(1200, 825);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().setBackground(new Color(10, 25, 50));

        // --- Panel Creation ---
        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();
        JPanel bottomPanel = createBottomPanel();

        add(leftPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        setupActionListeners();
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(createProcessEntryPanel());
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createProcessInputQueuePanel());
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(650, 0));
        panel.add(createRealTimeStatusPanel());
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createPerformanceMetricsPanel());
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel topOfBottom = new JPanel(new BorderLayout(10, 0));
        topOfBottom.add(createAlgorithmSelectionPanel(), BorderLayout.CENTER);
        topOfBottom.add(createSimulationControlsPanel(), BorderLayout.EAST);
        panel.add(topOfBottom, BorderLayout.NORTH);
        panel.add(createGanttChartPanel(), BorderLayout.CENTER);
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Status: Idle");
        statusPanel.add(statusLabel);
        panel.add(statusPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createProcessEntryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Process Entry & Management"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Arrival Time:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        arrivalField = new JTextField("0", 10);
        panel.add(arrivalField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Burst Time:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        burstField = new JTextField("10", 10);
        panel.add(burstField, gbc);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        addProcessButton = new JButton("Add Process");
        generateRandomButton = new JButton("Generate Random");
        removeProcessButton = new JButton("Remove Selected");
        addProcessButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        generateRandomButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeProcessButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(addProcessButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanel.add(generateRandomButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanel.add(removeProcessButton);
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridheight = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.VERTICAL;
        panel.add(buttonPanel, gbc);
        return panel;
    }

    private JPanel createProcessInputQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Process Input Queue"));
        String[] inputColumnNames = {"PID", "Arrival", "Burst Time"};
        inputQueueModel = new DefaultTableModel(inputColumnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        inputQueueTable = new JTable(inputQueueModel);
        panel.add(new JScrollPane(inputQueueTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRealTimeStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Process Status & Results"));
        String[] resultsColumnNames = {"PID", "Status", "Remaining", "Completion", "Turnaround", "Waiting", "Response"};
        resultsModel = new DefaultTableModel(resultsColumnNames, 0);
        resultsTable = new JTable(resultsModel);
        panel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPerformanceMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(new TitledBorder("Performance Metrics"));
        avgTurnaroundLabel = new JLabel("Avg. Turnaround Time: 0.00");
        avgWaitingLabel = new JLabel("Avg. Waiting Time: 0.00");
        avgResponseLabel = new JLabel("Avg. Response Time: 0.00");
        totalTimeLabel = new JLabel("Total Execution Time: 0");
        panel.add(avgTurnaroundLabel);
        panel.add(avgWaitingLabel);
        panel.add(avgResponseLabel);
        panel.add(totalTimeLabel);
        return panel;
    }

    private JPanel createAlgorithmSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Algorithm & Settings"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5); gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Algorithm:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        String[] algorithms = {"FCFS", "SJF (Non-Pre-emptive)", "SRTF", "Round Robin", "MLFQ"};
        algorithmComboBox = new JComboBox<>(algorithms);
        panel.add(algorithmComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        quantumLabel = new JLabel("Quantum(s):");
        panel.add(quantumLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        rrQuantumField = new JTextField("8,16,32", 10);
        panel.add(rrQuantumField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Context Switch Delay:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        contextSwitchField = new JTextField("1", 10);
        panel.add(contextSwitchField, gbc);
        return panel;
    }
    
    private JPanel createSimulationControlsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setBorder(new TitledBorder("Controls"));
        simulateButton = new JButton("Simulate");
        resetAllButton = new JButton("Reset All");
        exportButton = new JButton("Export CSV");
        panel.add(simulateButton);
        panel.add(resetAllButton);
        panel.add(exportButton);
        return panel;
    }

    private JPanel createGanttChartPanel() {
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.setBorder(new TitledBorder("Gantt Chart"));

    // Main panel for process blocks
    ganttChartPanel = new JPanel();
    ganttChartPanel.setLayout(new BoxLayout(ganttChartPanel, BoxLayout.X_AXIS));
    ganttChartPanel.setBackground(Color.WHITE);
    ganttScrollPane = new JScrollPane(ganttChartPanel);
    ganttScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    ganttScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // Panel for the time axis ticks and numbers
    timeAxisPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (currentState == null || currentState.currentTime == 0) return;

            int width = getWidth();
            int totalTime = currentState.currentTime;
            
            // Dynamically determine the interval for time markers to avoid clutter
            int interval = 1;
            if (totalTime > 150) interval = 20;
            else if (totalTime > 75) interval = 10;
            else if (totalTime > 20) interval = 5;

            g.setColor(Color.BLACK);
            for (int t = 0; t <= totalTime; t++) {
                int x = (int) ((double) t / totalTime * width);
                if (t % interval == 0) {
                    g.drawLine(x, 0, x, 8); // Taller tick mark
                    g.drawString(String.valueOf(t), x + 2, 20);
                } else if (totalTime < 50) {
                    g.drawLine(x, 0, x, 4); // Shorter tick mark
                }
            }
        }
    };
    timeAxisPanel.setPreferredSize(new Dimension(0, 25));

    wrapper.add(ganttScrollPane, BorderLayout.CENTER);
    wrapper.add(timeAxisPanel, BorderLayout.SOUTH);
    wrapper.setPreferredSize(new Dimension(0, 155)); // Increased height for axis
    return wrapper;
}
    
    private void setupActionListeners() {
        addProcessButton.addActionListener(this::addProcessAction);
        generateRandomButton.addActionListener(this::generateRandomAction);
        removeProcessButton.addActionListener(this::removeProcessAction);
        simulateButton.addActionListener(this::simulateAction);
        resetAllButton.addActionListener(this::resetAllAction);
        exportButton.addActionListener(this::exportToCsvAction);
        simulationTimer = new javax.swing.Timer(50, this::simulationStep);
        
        algorithmComboBox.addActionListener(this::onAlgorithmChange);
        onAlgorithmChange(null);
    }
    
    // Replace your existing onAlgorithmChange method with this one
private void onAlgorithmChange(ActionEvent e) {
    String selected = (String) algorithmComboBox.getSelectedItem();
    if (selected == null) return;

    boolean usesQuantum = selected.equals("Round Robin") || selected.equals("MLFQ");
    boolean isPreemptive = selected.equals("SRTF") || usesQuantum;

    // Enable/disable the quantum and context switch fields
    quantumLabel.setEnabled(usesQuantum);
    rrQuantumField.setEnabled(usesQuantum);
    contextSwitchField.setEnabled(isPreemptive);

    // Dynamically change the label and tooltip based on the algorithm
    switch (selected) {
        case "Round Robin":
            quantumLabel.setText("Quantum:");
            String helpTextRR = "Enter a single time quantum for Round Robin.";
            quantumLabel.setToolTipText(helpTextRR);
            rrQuantumField.setToolTipText(helpTextRR);
            // If the current text has commas, intelligently use only the first part
            if (rrQuantumField.getText().contains(",")) {
                rrQuantumField.setText(rrQuantumField.getText().split(",")[0].trim());
            }
            break;

        case "MLFQ":
            quantumLabel.setText("Quantum(s):");
            String helpTextMLFQ = "Enter comma-separated quanta for each priority level (e.g., 8,16,32).";
            quantumLabel.setToolTipText(helpTextMLFQ);
            rrQuantumField.setToolTipText(helpTextMLFQ);
            break;
            
        default:
            // Handles FCFS, SJF, SRTF where quantum is not used
            quantumLabel.setText("Quantum(s):");
            quantumLabel.setToolTipText(null);
            rrQuantumField.setToolTipText(null);
            break;
    }
}
    
    private void addProcessAction(ActionEvent e) {
        try {
            int arrival = Integer.parseInt(arrivalField.getText());
            int burst = Integer.parseInt(burstField.getText());
            if (arrival < 0 || burst <= 0) {
                JOptionPane.showMessageDialog(this, "Arrival time must be non-negative and burst time must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Process p = new Process(processCounter++, arrival, burst);
            processList.add(p);
            inputQueueModel.addRow(new Object[]{p.id, p.arrivalTime, p.burstTime});
            arrivalField.setText(String.valueOf(arrival + 1));
            burstField.setText("");
            burstField.requestFocus();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid integer values for arrival and burst times.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeProcessAction(ActionEvent e) {
        int selectedRow = inputQueueTable.getSelectedRow();
        if (selectedRow >= 0) {
            int pidToRemove = (int) inputQueueModel.getValueAt(selectedRow, 0);
            processList.removeIf(p -> p.id == pidToRemove);
            inputQueueModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a process from the input queue to remove.", "No Process Selected", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void generateRandomAction(ActionEvent e) {
        String countStr = JOptionPane.showInputDialog(this, "How many random processes to generate?", "5");
        if (countStr == null) return;
        try {
            int count = Integer.parseInt(countStr);
            Random rand = new Random();
            for (int i = 0; i < count; i++) {
                int arrival = rand.nextInt(20);
                int burst = rand.nextInt(15) + 1;
                Process p = new Process(processCounter++, arrival, burst);
                processList.add(p);
                inputQueueModel.addRow(new Object[]{p.id, p.arrivalTime, p.burstTime});
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportToCsvAction(ActionEvent e) {
        if (resultsModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No results to export. Please run a simulation first.", "Export Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Results as CSV");
        fileChooser.setSelectedFile(new File("cpu_scheduling_results.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                for (int i = 0; i < resultsModel.getColumnCount(); i++) {
                    writer.write(resultsModel.getColumnName(i) + (i == resultsModel.getColumnCount() - 1 ? "" : ","));
                }
                writer.newLine();
                for (int row = 0; row < resultsModel.getRowCount(); row++) {
                    for (int col = 0; col < resultsModel.getColumnCount(); col++) {
                        writer.write(resultsModel.getValueAt(row, col).toString() + (col == resultsModel.getColumnCount() - 1 ? "" : ","));
                    }
                    writer.newLine();
                }
                writer.newLine();
                writer.write("Summary Metrics");
                writer.newLine();
                writer.write(avgTurnaroundLabel.getText().replace(":", ","));
                writer.newLine();
                writer.write(avgWaitingLabel.getText().replace(":", ","));
                writer.newLine();
                writer.write(avgResponseLabel.getText().replace(":", ","));
                writer.newLine();
                writer.write(totalTimeLabel.getText().replace(":", ","));
                writer.newLine();
                
                JOptionPane.showMessageDialog(this, "Results successfully exported to:\n" + fileToSave.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error writing to file:\n" + ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void simulateAction(ActionEvent e) {
        if (simulationTimer.isRunning()) {
            simulationTimer.stop();
            setControlsEnabled(true);
            return;
        }
        if (processList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one process.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        resetSimulationState(false);
        setControlsEnabled(false);
        String selectedAlgo = (String) algorithmComboBox.getSelectedItem();
        try {
            int contextDelay = 0;
            if (contextSwitchField.isEnabled()) {
                 contextDelay = Integer.parseInt(contextSwitchField.getText());
            }
            currentState.contextSwitchDelay = Math.max(0, contextDelay);

            switch (selectedAlgo) {
                case "FCFS": currentAlgorithm = new FCFS(); break;
                case "SJF": currentAlgorithm = new SJF(); break;
                case "SRTF": currentAlgorithm = new SRTF(); break;
                case "Round Robin":
                    int quantum = Integer.parseInt(rrQuantumField.getText().split(",")[0].trim());
                    currentAlgorithm = new RoundRobin(quantum); break;
                case "MLFQ":
                    String[] quantaStr = rrQuantumField.getText().split(",");
                    int[] quanta = new int[4];
                    for(int i = 0; i < 3; i++) quanta[i] = Integer.parseInt(quantaStr[i].trim());
                    quanta[3] = Integer.MAX_VALUE;
                    currentAlgorithm = new MLFQ(quanta); break;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantum or context switch value.", "Input Error", JOptionPane.ERROR_MESSAGE);
            setControlsEnabled(true);
            return;
        }
        simulationTimer.start();
    }

    private void simulationStep(ActionEvent e) {
        currentAlgorithm.step(currentState);
        updateLiveUI();
        
        if (currentState.completedProcesses.size() == processList.size()) {
            simulationTimer.stop();
            setControlsEnabled(true);
            statusLabel.setText("Status: Simulation Finished at time " + currentState.currentTime);
            calculateAndDisplayFinalMetrics();
        }
    }

    private void resetAllAction(ActionEvent e) {
        if (simulationTimer.isRunning()) simulationTimer.stop();
        processList.clear();
        processCounter = 1;
        inputQueueModel.setRowCount(0);
        resetSimulationState(true);
        setControlsEnabled(true);
    }
    
    private void resetSimulationState(boolean fullReset) {
        currentState = new SchedulerState();
        if(!fullReset) {
             for (Process p : processList) {
                currentState.remainingProcesses.add(new Process(p.id, p.arrivalTime, p.burstTime));
             }
             currentState.remainingProcesses.sort(Comparator.comparingInt((Process p) -> p.arrivalTime).thenComparingInt(p -> p.id));
        }
        resultsModel.setRowCount(0);
        ganttChartPanel.removeAll();
        ganttChartPanel.revalidate();
        ganttChartPanel.repaint();
        calculateAndDisplayFinalMetrics();
        statusLabel.setText("Status: Idle");
    }

    private void setControlsEnabled(boolean enabled) {
        simulateButton.setText(enabled ? "Simulate" : "Stop");
        addProcessButton.setEnabled(enabled);
        generateRandomButton.setEnabled(enabled);
        removeProcessButton.setEnabled(enabled);
        algorithmComboBox.setEnabled(enabled);
        resetAllButton.setEnabled(enabled);
        
        if (enabled) {
            onAlgorithmChange(null);
        } else {
            rrQuantumField.setEnabled(false);
            contextSwitchField.setEnabled(false);
        }
    }

private void updateLiveUI() {
    totalTimeLabel.setText("Total Execution Time: " + currentState.currentTime);
    statusLabel.setText(currentState.status);

    resultsModel.setRowCount(0);
    List<Process> allProcesses = new ArrayList<>();
    allProcesses.addAll(currentState.completedProcesses);
    allProcesses.addAll(currentState.readyQueue);
    if(currentState.runningProcess != null && !allProcesses.contains(currentState.runningProcess)) {
        allProcesses.add(currentState.runningProcess);
    }
    allProcesses.sort(Comparator.comparingInt(p -> p.id));
    for (Process p : allProcesses) {
        String status = "Waiting";
        if (p.remainingBurstTime == 0) status = "Finished";
        else if (currentState.runningProcess != null && currentState.runningProcess.id == p.id) status = "Running";

        resultsModel.addRow(new Object[]{ p.id, status, p.remainingBurstTime, p.completionTime, p.turnaroundTime, p.waitingTime, p.responseTime});
    }

    ganttChartPanel.removeAll();
    Color[] colors = {new Color(217, 83, 79), new Color(91, 192, 222), new Color(240, 173, 78), new Color(92, 184, 92), new Color(104, 93, 156), new Color(2, 117, 216)};
    int totalGanttWidth = Math.max(1200, ganttScrollPane.getViewport().getWidth()); // Set a min width for readability
    int totalTime = Math.max(1, currentState.currentTime);

    for (GanttChartEntry entry : currentState.ganttChart) {
        int duration = entry.endTime - entry.startTime;
        int width = (int) Math.round((double) duration / totalTime * totalGanttWidth);

        // Use our new custom panel to get tooltips
        GanttBlockPanel block = new GanttBlockPanel(entry);
        block.setPreferredSize(new Dimension(width, 60));
        block.setMaximumSize(new Dimension(width, 60));
        block.setMinimumSize(new Dimension(width, 60));
        
        String labelText;
        Color labelColor;
        
        if (entry.processId == 0) {
            block.setBackground(new Color(235, 235, 235));
            block.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            labelText = "Idle";
            labelColor = Color.GRAY;
        } else if (entry.processId == -1) {
            block.setBackground(Color.LIGHT_GRAY);
            block.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            labelText = "CS";
            labelColor = Color.DARK_GRAY;
        } else {
            block.setBackground(colors[(entry.processId - 1) % colors.length]);
            block.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            labelText = "P" + entry.processId;
            if (entry.queueLevel != -1) labelText += " [Q" + entry.queueLevel + "]";
            labelColor = Color.WHITE;
        }
        
        JLabel label = new JLabel(labelText, SwingConstants.CENTER);
        label.setForeground(labelColor);
        block.add(label, BorderLayout.CENTER);
        ganttChartPanel.add(block);
    }
    
    // Repaint both the chart and the new time axis
    ganttChartPanel.revalidate();
    ganttChartPanel.repaint();
    timeAxisPanel.repaint();
}
    
    private void calculateAndDisplayFinalMetrics() {
        double totalTurnaround = 0, totalWaiting = 0, totalResponse = 0;
        int n = currentState.completedProcesses.size();
        if (n > 0) {
            for (Process p : currentState.completedProcesses) {
                totalTurnaround += p.turnaroundTime;
                totalWaiting += p.waitingTime;
                totalResponse += p.responseTime;
            }
            avgTurnaroundLabel.setText(String.format("Avg. Turnaround Time: %.2f", totalTurnaround / n));
            avgWaitingLabel.setText(String.format("Avg. Waiting Time: %.2f", totalWaiting / n));
            avgResponseLabel.setText(String.format("Avg. Response Time: %.2f", totalResponse / n));
        } else {
             avgTurnaroundLabel.setText("Avg. Turnaround Time: 0.00");
             avgWaitingLabel.setText("Avg. Waiting Time: 0.00");
             avgResponseLabel.setText("Avg. Response Time: 0.00");
        }
        totalTimeLabel.setText("Total Execution Time: " + currentState.currentTime);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Shedulerui().setVisible(true));
    }
}

class SchedulerState {
    int currentTime = 0;
    int contextSwitchDelay = 0;
    int contextSwitchTimeRemaining = 0;
    String status = "Idle";
    Process runningProcess = null;
    List<Process> remainingProcesses = new ArrayList<>();
    List<Process> readyQueue = new ArrayList<>();
    List<Process> completedProcesses = new ArrayList<>();
    List<GanttChartEntry> ganttChart = new ArrayList<>();
    Map<String, Object> algorithmState = new HashMap<>();
}

interface StepBasedScheduler { void step(SchedulerState state); }

abstract class BaseScheduler implements StepBasedScheduler {
    @SuppressWarnings("unchecked")
    protected void handleArrivals(SchedulerState state) {
        while (!state.remainingProcesses.isEmpty() && state.remainingProcesses.get(0).arrivalTime <= state.currentTime) {
            Process p = state.remainingProcesses.remove(0);
            if (state.algorithmState.containsKey("queues")) {
                ((List<Queue<Process>>) state.algorithmState.get("queues")).get(0).add(p);
            } else {
                state.readyQueue.add(p);
            }
        }
    }

    protected void startContextSwitch(SchedulerState state, Process nextProcess) {
        state.runningProcess = null;
        if (state.contextSwitchDelay > 0) {
            state.contextSwitchTimeRemaining = state.contextSwitchDelay;
            state.algorithmState.put("nextProcess", nextProcess);
        } else {
            state.runningProcess = nextProcess;
            if (nextProcess != null && !nextProcess.started) {
                nextProcess.responseTime = state.currentTime - nextProcess.arrivalTime;
                nextProcess.started = true;
            }
        }
    }

    protected void updateGantt(SchedulerState state, int processId, int queueLevel) {
        // Use the current time as the start for the Gantt block.
        int startTime = state.currentTime;
        if (state.ganttChart.isEmpty() || state.ganttChart.get(state.ganttChart.size() - 1).processId != processId) {
            // Add a new entry if the chart is empty or the process has changed.
            state.ganttChart.add(new GanttChartEntry(processId, startTime, startTime + 1, queueLevel));
        } else {
            // Otherwise, just extend the end time of the last block.
            state.ganttChart.get(state.ganttChart.size() - 1).endTime++;
        }
    }

    protected boolean handleContextSwitch(SchedulerState state) {
        if (state.contextSwitchTimeRemaining > 0) {
            state.status = "Context Switching...";
            state.contextSwitchTimeRemaining--;
            updateGantt(state, -1, -1); // -1 for Context Switch
            if (state.contextSwitchTimeRemaining == 0) {
                state.runningProcess = (Process) state.algorithmState.get("nextProcess");
                if (state.runningProcess != null && !state.runningProcess.started) {
                    state.runningProcess.responseTime = state.currentTime - state.runningProcess.arrivalTime;
                    state.runningProcess.started = true;
                }
            }
            return true;
        }
        return false;
    }

    protected void finishProcess(SchedulerState state, Process p, int completionTime) {
        p.calculateFinalMetrics(completionTime); // Use our new helper method
        state.completedProcesses.add(p);
        state.runningProcess = null;
    }
}

class FCFS extends BaseScheduler {
    @Override
    public void step(SchedulerState state) {
        handleArrivals(state);

        if (state.runningProcess == null) {
            if (!state.readyQueue.isEmpty()) {
                state.readyQueue.sort(Comparator.comparingInt((Process p) -> p.arrivalTime).thenComparingInt(p -> p.id));
                Process next = state.readyQueue.get(0);

                if (state.currentTime >= next.arrivalTime) {
                    state.runningProcess = state.readyQueue.remove(0);
                    if (!state.runningProcess.started) {
                        state.runningProcess.responseTime = state.currentTime - state.runningProcess.arrivalTime;
                        state.runningProcess.started = true;
                    }
                }
            }
        }

        if (state.runningProcess != null) {
            state.status = "Running P" + state.runningProcess.id;
            updateGantt(state, state.runningProcess.id, -1);
            state.runningProcess.remainingBurstTime--;
            if (state.runningProcess.remainingBurstTime == 0) {
                finishProcess(state, state.runningProcess, state.currentTime + 1);
            }
        } else {
            state.status = "Idle";
            // Only draw idle block if the simulation isn't finished yet
            if (!state.remainingProcesses.isEmpty() || !state.readyQueue.isEmpty()) {
                updateGantt(state, 0, -1); // Use PID 0 for IDLE
            }
        }
        state.currentTime++;
    }
}

class SJF extends BaseScheduler {
    @Override
    public void step(SchedulerState state) {
        handleArrivals(state);

        if (state.runningProcess == null) {
            if (!state.readyQueue.isEmpty()) {
                // The only difference from FCFS: sort by burst time instead of arrival time.
                state.readyQueue.sort(Comparator.comparingInt(p -> p.burstTime));
                state.runningProcess = state.readyQueue.remove(0);
                if (!state.runningProcess.started) {
                    state.runningProcess.responseTime = state.currentTime - state.runningProcess.arrivalTime;
                    state.runningProcess.started = true;
                }
            }
        }

        if (state.runningProcess != null) {
            state.status = "Running P" + state.runningProcess.id;
            updateGantt(state, state.runningProcess.id, -1);
            state.runningProcess.remainingBurstTime--;
            if (state.runningProcess.remainingBurstTime == 0) {
                finishProcess(state, state.runningProcess, state.currentTime + 1);
            }
        } else {
            state.status = "Idle";
            if (!state.remainingProcesses.isEmpty() || !state.readyQueue.isEmpty()) {
                updateGantt(state, 0, -1); // Use PID 0 for IDLE
            }
        }
        state.currentTime++;
    }
}

// Replace your existing SRTF class
// Replace your existing SRTF class with this one
class SRTF extends BaseScheduler {
    @Override
    public void step(SchedulerState state) {
        // --- 1. Handle Arrivals & Preemption ---
        handleArrivals(state);

        // Check for preemption: If a process is running, see if a newly arrived process is shorter.
        if (state.runningProcess != null) {
            // Find the shortest process in the ready queue
            Process shortestInQueue = state.readyQueue.stream()
                .min(Comparator.comparingInt(p -> p.remainingBurstTime))
                .orElse(null);

            // If a process in the queue is shorter than the running one, preempt!
            if (shortestInQueue != null && shortestInQueue.remainingBurstTime < state.runningProcess.remainingBurstTime) {
                state.readyQueue.add(state.runningProcess);
                state.runningProcess = null;
            }
        }

        // --- 2. Select a Process if CPU is Idle ---
        if (state.runningProcess == null && !state.readyQueue.isEmpty()) {
            // Sort by remaining time, then by PID as a tie-breaker
            state.readyQueue.sort(Comparator.comparingInt((Process p) -> p.remainingBurstTime).thenComparingInt(p -> p.id));
            state.runningProcess = state.readyQueue.remove(0);
        }

        // --- 3. Execute the Current Tick ---
        if (state.runningProcess != null) {
            // Set response time if this is the first time the process runs
            if (!state.runningProcess.started) {
                state.runningProcess.responseTime = state.currentTime - state.runningProcess.arrivalTime;
                state.runningProcess.started = true;
            }

            state.status = "Running P" + state.runningProcess.id;
            updateGantt(state, state.runningProcess.id, -1);
            state.runningProcess.remainingBurstTime--;

            // Check if the process finished
            if (state.runningProcess.remainingBurstTime == 0) {
                // The process finishes at the end of this tick
                finishProcess(state, state.runningProcess, state.currentTime + 1);
            }
        } else {
            // If nothing is running, the CPU is idle
            state.status = "Idle";
            if (!state.remainingProcesses.isEmpty() || !state.readyQueue.isEmpty()) {
                updateGantt(state, 0, -1); // Draw an idle block
            }
        }

        // --- 4. Advance Time ---
        state.currentTime++;
    }
}

// Replace your existing RoundRobin class with this one
class RoundRobin extends BaseScheduler {
    private final int quantum;

    public RoundRobin(int quantum) { this.quantum = quantum; }

    @Override
    public void step(SchedulerState state) {
        handleArrivals(state);

        // If a context switch is in progress, let it finish and do nothing else this tick.
        if (handleContextSwitch(state)) {
            state.currentTime++;
            return;
        }

        // If a process was running, check if its time slice is up or if it's finished.
        if (state.runningProcess != null) {
            Integer slice = (Integer) state.algorithmState.get("slice");

            if (state.runningProcess.remainingBurstTime == 0) {
                // Process finished its job.
                finishProcess(state, state.runningProcess, state.currentTime);
                state.runningProcess = null; // Free the CPU.
            } else if (slice == 0) {
                // Process's time slice is over, but it's not done.
                state.readyQueue.add(state.runningProcess); // Add it to the back of the queue.
                state.runningProcess = null; // Free the CPU.
            }
        }

        // If the CPU is now free, select the next process from the ready queue.
        if (state.runningProcess == null && !state.readyQueue.isEmpty()) {
            Process nextProcess = state.readyQueue.remove(0);
            state.algorithmState.put("slice", quantum); // Reset the quantum for the new process.
            startContextSwitch(state, nextProcess);
        }
        
        // If a process is running (either continuing or just started after a context switch)...
        if (state.runningProcess != null) {
            // Set response time if this is the first time the process runs.
            if (!state.runningProcess.started) {
                state.runningProcess.responseTime = state.currentTime - state.runningProcess.arrivalTime;
                state.runningProcess.started = true;
            }
            
            // Execute one tick of work.
            state.status = "Running P" + state.runningProcess.id;
            updateGantt(state, state.runningProcess.id, -1);
            state.runningProcess.remainingBurstTime--;
            
            // Decrement its time slice for this tick.
            Integer slice = (Integer) state.algorithmState.get("slice");
            state.algorithmState.put("slice", slice - 1);
        } else {
            // If nothing is running and no context switch is happening, the CPU is idle.
            state.status = "Idle";
            if (!state.remainingProcesses.isEmpty() || !state.readyQueue.isEmpty()) {
                updateGantt(state, 0, -1);
            }
        }
        
        // Advance the simulation clock for the next tick.
        state.currentTime++;
    }
}

// Replace your existing MLFQ class with this one
class MLFQ extends BaseScheduler {
    private final int[] quanta;

    public MLFQ(int[] quanta) { this.quanta = quanta; }

    @Override
    @SuppressWarnings("unchecked")
    public void step(SchedulerState state) {
        // Initialize the queues on the first run.
        if (!state.algorithmState.containsKey("queues")) {
            List<Queue<Process>> queues = new ArrayList<>();
            for (int i = 0; i < quanta.length; i++) queues.add(new LinkedList<>());
            state.algorithmState.put("queues", queues);
        }
        List<Queue<Process>> queues = (List<Queue<Process>>) state.algorithmState.get("queues");

        handleArrivals(state);

        if (handleContextSwitch(state)) {
            state.currentTime++;
            return;
        }

        // If a process was running, check if it finished or its time slice expired.
        if (state.runningProcess != null) {
            Integer slice = (Integer) state.algorithmState.get("slice");

            if (state.runningProcess.remainingBurstTime == 0) {
                // Process finished its job.
                finishProcess(state, state.runningProcess, state.currentTime);
                state.runningProcess = null;
            } else if (slice == 0) {
                // Time slice is over. Demote the process to the next-lower queue.
                int nextQueueIndex = Math.min(state.runningProcess.priority + 1, quanta.length - 1);
                state.runningProcess.priority = nextQueueIndex;
                queues.get(nextQueueIndex).add(state.runningProcess);
                state.runningProcess = null;
            }
        }

        // If the CPU is now free, find the highest-priority process to run.
        if (state.runningProcess == null) {
            for (int i = 0; i < queues.size(); i++) {
                if (!queues.get(i).isEmpty()) {
                    Process nextProcess = queues.get(i).poll();
                    nextProcess.priority = i;
                    state.algorithmState.put("slice", quanta[i]);
                    startContextSwitch(state, nextProcess);
                    break;
                }
            }
        }

        // If a process is running, execute one tick.
        if (state.runningProcess != null) {
            if (!state.runningProcess.started) {
                state.runningProcess.responseTime = state.currentTime - state.runningProcess.arrivalTime;
                state.runningProcess.started = true;
            }

            state.status = "Running P" + state.runningProcess.id + " [Q" + state.runningProcess.priority + "]";
            updateGantt(state, state.runningProcess.id, state.runningProcess.priority);
            state.runningProcess.remainingBurstTime--;

            Integer slice = (Integer) state.algorithmState.get("slice");
            state.algorithmState.put("slice", slice - 1);
        } else {
            // If nothing is running, the CPU is idle.
            state.status = "Idle";
            if (!state.remainingProcesses.isEmpty() || !queues.stream().allMatch(Queue::isEmpty)) {
                updateGantt(state, 0, -1);
            }
        }

        state.currentTime++;
    }
}

class Process {
    int id, arrivalTime, burstTime, remainingBurstTime, priority;
    int completionTime, turnaroundTime, waitingTime, responseTime;
    boolean started = false;

    public Process(int id, int arrivalTime, int burstTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingBurstTime = burstTime;
    }

    /**
     * Calculates all final time metrics for the process.
     * @param pCompletionTime The time the process finished execution.
     */
    public void calculateFinalMetrics(int pCompletionTime) {
        this.completionTime = pCompletionTime;
        this.turnaroundTime = this.completionTime - this.arrivalTime;
        this.waitingTime = this.turnaroundTime - this.burstTime;
    }
}

/**
 * A custom panel for a single block in the Gantt chart that displays
 * detailed information in a tooltip on mouse hover.
 */
class GanttBlockPanel extends JPanel {
    private final String tooltipText;

    public GanttBlockPanel(GanttChartEntry entry) {
        super(new BorderLayout());

        // Prepare the detailed tooltip text based on the entry type
        if (entry.processId > 0) {
            tooltipText = String.format(
                "<html><b>Process:</b> P%d<br><b>Start Time:</b> %d<br><b>End Time:</b> %d<br><b>Duration:</b> %d</html>",
                entry.processId, entry.startTime, entry.endTime, (entry.endTime - entry.startTime)
            );
        } else if (entry.processId == 0) {
            tooltipText = "<html><b>Status:</b> Idle</html>";
        } else {
            tooltipText = "<html><b>Status:</b> Context Switch</html>";
        }
        setToolTipText(tooltipText);
    }
}

class GanttChartEntry {
    int processId, startTime, endTime, queueLevel;
    public GanttChartEntry(int processId, int startTime, int endTime, int queueLevel) {
        this.processId = processId; this.startTime = startTime; this.endTime = endTime; this.queueLevel = queueLevel;
    }
}