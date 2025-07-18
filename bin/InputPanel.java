 package bin;

import javax.swing.*;

public class InputPanel extends JPanel {
    public JTextField arrivalField, burstField, priorityField;

    public InputPanel() {
        arrivalField = new JTextField(5);
        burstField = new JTextField(5);
        priorityField = new JTextField(5);

        add(new JLabel("Arrival:"));
        add(arrivalField);
        add(new JLabel("Exec Time:"));
        add(burstField);
        add(new JLabel("Priority:"));
        add(priorityField);
    }
}