package bin;

public class SchedulerController {
    public InputPanel inputPanel;
    public ButtonPanel buttonPanel;
    public TablePanel tablePanel;
    public AlgorithmPanel algorithmPanel;
    public OutputPanel outputPanel;

    public SchedulerController() {
        inputPanel = new InputPanel();
        tablePanel = new TablePanel();
        algorithmPanel = new AlgorithmPanel();
        outputPanel = new OutputPanel();
        buttonPanel = new ButtonPanel(this);
    }
}