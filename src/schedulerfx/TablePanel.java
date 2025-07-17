package src.schedulerfx;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TablePanel extends JPanel {
    public JTable processTable;
    public DefaultTableModel tableModel;
    public JButton removeBtn;

    public TablePanel() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new Object[]{"Process", "Arrival", "Exec", "Priority"}, 0);
        processTable = new JTable(tableModel);
        removeBtn = new JButton("Remove Selected");

        add(new JScrollPane(processTable), BorderLayout.CENTER);
        add(removeBtn, BorderLayout.SOUTH);
    }
}