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
    private JPanel ganttChartPanel;
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
        setSize(1360, 900);
        setResizable(true);
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
        panel.setPreferredSize(new Dimension(640, 0));
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
        panel.add(new JLabel("Quantum(s):"), gbc);
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
        ganttChartPanel = new JPanel();
        ganttChartPanel.setLayout(new BoxLayout(ganttChartPanel, BoxLayout.X_AXIS));
        ganttChartPanel.setBackground(Color.WHITE);
        ganttScrollPane = new JScrollPane(ganttChartPanel);
        ganttScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        ganttScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        wrapper.add(ganttScrollPane, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(0, 130));
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
    
    private void onAlgorithmChange(ActionEvent e) {
        String selected = (String) algorithmComboBox.getSelectedItem();
        if (selected != null) {
            boolean usesQuantum = selected.equals("Round Robin") || selected.equals("MLFQ");
            rrQuantumField.setEnabled(usesQuantum);
            
            boolean isPreemptive = selected.equals("SRTF") || selected.equals("Round Robin") || selected.equals("MLFQ");
            contextSwitchField.setEnabled(isPreemptive);
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
    int totalGanttWidth = ganttScrollPane.getViewport().getWidth();
    int totalTime = Math.max(1, currentState.currentTime);

    for (GanttChartEntry entry : currentState.ganttChart) {
        int duration = entry.endTime - entry.startTime;
        int width = Math.max(1, (int) Math.round((double) duration / totalTime * totalGanttWidth));

        JPanel block = new JPanel(new BorderLayout());
        block.setPreferredSize(new Dimension(width, 60));
        block.setMaximumSize(new Dimension(width, 60));
        block.setMinimumSize(new Dimension(width, 60));
        
        String labelText;
        Color labelColor;
        
        // This block determines the text and color for the Gantt entry
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
        
        // A single JLabel is created here, after all properties have been determined
        JLabel label = new JLabel(labelText, SwingConstants.CENTER);
        label.setForeground(labelColor);
        block.add(label, BorderLayout.CENTER);
        ganttChartPanel.add(block);
    }
    ganttChartPanel.revalidate();
    ganttChartPanel.repaint();
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
class SRTF extends BaseScheduler {
    @Override
    public void step(SchedulerState state) {
        state.currentTime++;
        handleArrivals(state);
        
        if (state.runningProcess != null) {
            state.readyQueue.add(state.runningProcess);
            state.runningProcess = null;
        }
        
        if (handleContextSwitch(state)) return;

        if (!state.readyQueue.isEmpty()) {
            state.readyQueue.sort(Comparator.comparingInt(p -> p.remainingBurstTime));
            Process nextProcess = state.readyQueue.remove(0);

            Process lastProcess = (Process) state.algorithmState.get("lastProcess");
            if (lastProcess != nextProcess) {
                state.algorithmState.put("lastProcess", nextProcess);
                startContextSwitch(state, nextProcess);
                return;
            }
            
            state.runningProcess = nextProcess;
            state.status = "Running P" + state.runningProcess.id;
            state.runningProcess.remainingBurstTime--;
            updateGantt(state, state.runningProcess.id, -1);

            if (state.runningProcess.remainingBurstTime == 0) {
                // The fix is here: passing the completion time
                finishProcess(state, state.runningProcess, state.currentTime);
                state.algorithmState.remove("lastProcess");
            }
        } else {
            state.runningProcess = null;
            state.status = "Idle";
        }
    }
}

// Replace your existing RoundRobin class
class RoundRobin extends BaseScheduler {
    private int quantum;
    public RoundRobin(int quantum) { this.quantum = quantum; }

    @Override
    public void step(SchedulerState state) {
        state.currentTime++;
        handleArrivals(state);

        Integer slice = (Integer) state.algorithmState.getOrDefault("slice", quantum);
        if (state.runningProcess != null) {
            slice--;
            state.algorithmState.put("slice", slice);
        }

        if (handleContextSwitch(state)) return;
        
        if (state.runningProcess != null) {
            state.status = "Running P" + state.runningProcess.id;
            state.runningProcess.remainingBurstTime--;
            updateGantt(state, state.runningProcess.id, -1);

            if (state.runningProcess.remainingBurstTime == 0) {
                // The fix is here: passing the completion time
                finishProcess(state, state.runningProcess, state.currentTime);
                state.algorithmState.put("slice", quantum);
            } else if (slice == 0) {
                state.readyQueue.add(state.runningProcess);
                startContextSwitch(state, null);
                state.algorithmState.put("slice", quantum);
            }
        }
        
        if (state.runningProcess == null && !state.readyQueue.isEmpty()) {
            startContextSwitch(state, state.readyQueue.remove(0));
        } else if (state.runningProcess == null) {
            state.status = "Idle";
        }
    }
}

// Replace your existing MLFQ class
class MLFQ extends BaseScheduler {
    private int[] quanta;
    public MLFQ(int[] quanta) { this.quanta = quanta; }

    @Override
    @SuppressWarnings("unchecked")
    public void step(SchedulerState state) {
        if (!state.algorithmState.containsKey("queues")) {
            List<Queue<Process>> queues = new ArrayList<>();
            for (int i = 0; i < quanta.length; i++) queues.add(new LinkedList<>());
            state.algorithmState.put("queues", queues);
        }
        List<Queue<Process>> queues = (List<Queue<Process>>) state.algorithmState.get("queues");

        state.currentTime++;
        handleArrivals(state);

        Integer slice = (Integer) state.algorithmState.getOrDefault("slice", 0);
        if (state.runningProcess != null) {
            slice--;
            state.algorithmState.put("slice", slice);
        }

        if (handleContextSwitch(state)) return;
        
        if (state.runningProcess != null) {
            state.status = "Running P" + state.runningProcess.id + " [Q" + state.runningProcess.priority + "]";
            state.runningProcess.remainingBurstTime--;
            updateGantt(state, state.runningProcess.id, state.runningProcess.priority);

            if (state.runningProcess.remainingBurstTime == 0) {
                // The fix is here: passing the completion time
                finishProcess(state, state.runningProcess, state.currentTime);
            } else if (slice == 0) {
                int nextQueue = Math.min(state.runningProcess.priority + 1, quanta.length - 1);
                state.runningProcess.priority = nextQueue;
                queues.get(nextQueue).add(state.runningProcess);
                startContextSwitch(state, null);
            }
        }
        
        if (state.runningProcess == null) {
            for (int i = 0; i < queues.size(); i++) {
                if (!queues.get(i).isEmpty()) {
                    Process next = queues.get(i).poll();
                    next.priority = i;
                    state.algorithmState.put("slice", quanta[i]);
                    startContextSwitch(state, next);
                    break;
                }
            }
        }
        
        if (state.runningProcess == null) {
            state.status = "Idle";
        }
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



class GanttChartEntry {
    int processId, startTime, endTime, queueLevel;
    public GanttChartEntry(int processId, int startTime, int endTime, int queueLevel) {
        this.processId = processId; this.startTime = startTime; this.endTime = endTime; this.queueLevel = queueLevel;
    }
}