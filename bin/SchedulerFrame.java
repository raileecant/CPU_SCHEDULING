package bin;

import javax.swing.*;
import java.awt.*;

public class SchedulerFrame extends JFrame {

    public SchedulerFrame() {
        setTitle("CPU Scheduler Simulator");
        setSize(950, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        SchedulerController controller = new SchedulerController();

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(controller.inputPanel);
        topPanel.add(controller.buttonPanel);
        topPanel.add(controller.tablePanel);
        topPanel.add(controller.algorithmPanel);

        add(topPanel, BorderLayout.NORTH);
        add(controller.outputPanel, BorderLayout.CENTER);
    }
}