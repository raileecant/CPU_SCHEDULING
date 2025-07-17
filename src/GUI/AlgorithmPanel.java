package src.GUI;

import javax.swing.*;
import java.awt.*;

public class AlgorithmPanel extends JPanel {
    public JComboBox<String> algorithmList;
    public JSlider quantumSlider;

    public AlgorithmPanel() {
        setLayout(new FlowLayout());

        algorithmList = new JComboBox<>(new String[]{"FCFS", "SJF", "SRTF", "Round Robin", "MLFQ"});
        quantumSlider = new JSlider(1, 10, 4);
        quantumSlider.setMajorTickSpacing(1);
        quantumSlider.setPaintTicks(true);
        quantumSlider.setPaintLabels(true);

        add(new JLabel("Algorithm:"));
        add(algorithmList);
        add(new JLabel("Quantum:"));
        add(quantumSlider);
    }
}
