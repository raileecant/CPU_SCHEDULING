package src.GUI;

import javax.swing.*;
import java.awt.*;

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

        // Add action listeners here and access controller as needed
    }
}