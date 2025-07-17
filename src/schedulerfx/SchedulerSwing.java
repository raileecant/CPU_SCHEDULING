package src.schedulerfx;

import javax.swing.*;

public class SchedulerSwing {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SchedulerFrame().setVisible(true));
    }
}
