package bin;

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
    public java.util.List<Process> getProcesses() {
        java.util.List<Process> list = new java.util.ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String id = tableModel.getValueAt(i, 0).toString();
            int arrival = Integer.parseInt(tableModel.getValueAt(i, 1).toString());
            int exec = Integer.parseInt(tableModel.getValueAt(i, 2).toString());
            int priority = Integer.parseInt(tableModel.getValueAt(i, 3).toString());
            list.add(new Process(id, arrival, exec, priority));
        }
        return list;
    }
}