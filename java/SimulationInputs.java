import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.util.Hashtable;

public class SimulationInputs {
    public JFrame frame;
    private JCheckBox needleCheckBox;
    public JPanel mainPanel;
    public JSlider batterySlider;
    private JButton addGlucoseIUButton;
    private JTextField textFieldGlucose;
    private JSlider insulinSlider;
    private JSlider glucagonSlider;
    private JPanel levelsPanel;
    private JPanel addPanel;
    private JButton changeBSLButton;
    private JTextField changeBSLTextField;
    private JCheckBox needleStatus;
    private JProgressBar batteryField;
    private Physician physician;
    private Reservoir reservoir;
    private Pump pump;
    private GlucoseLevel glucoseLevel;



    public SimulationInputs(final JCheckBox needleStatus, JProgressBar batteryField, final Physician physician,
                            final Reservoir reservoir, final Pump pump, final GlucoseLevel glucoseLevel) {
        this.frame = new JFrame("Simulation Control Panel");

        this.needleStatus = needleStatus;
        this.batteryField = batteryField;
        this.physician = physician;
        this.reservoir = reservoir;
        this.pump = pump; //to get battery levels

        //-- Slider setup
        this.batterySlider.setValue(pump.batteryLevel);
        this.batterySlider.setMaximum(100);
        this.batterySlider.setMinimum(0);
        this.batterySlider.setMinorTickSpacing(10);

        //Create the label table
        Hashtable labelTable = new Hashtable();
        labelTable.put( new Integer( 0 ), new JLabel("0%") );
        //labelTable.put( new Integer( 100/10 ), new JLabel("Slow") );
        labelTable.put( new Integer( 100 ), new JLabel("100%") );
        this.batterySlider.setLabelTable( labelTable );
        this.batterySlider.setPaintLabels(true);
        this.batterySlider.setPaintTicks(true);

        this.insulinSlider.setValue(reservoir.getRemainingPercentInsulin());
        this.insulinSlider.setLabelTable( labelTable );
        this.insulinSlider.setPaintLabels(true);
        this.insulinSlider.setPaintTicks(true);

        this.glucagonSlider.setValue(reservoir.getRemainingPercentGlucagon());
        this.glucagonSlider.setLabelTable( labelTable );
        this.glucagonSlider.setPaintLabels(true);
        this.glucagonSlider.setPaintTicks(true);



        //----Display Simulator input window
        this.frame.setContentPane(mainPanel);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.pack();
        this.frame.setVisible(true);



        needleCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(needleCheckBox.isSelected()){
                    needleStatus.setSelected(true);
                }
                if(!(needleCheckBox.isSelected())){
                    needleStatus.setSelected(false);

                }
            }
        });

        addGlucoseIUButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!(textFieldGlucose.getText().isEmpty())){

                    double new_glucagon = Double.parseDouble(textFieldGlucose.getText());

                    if(new_glucagon > 0){

                        physician.setGlucagon(new_glucagon,"Feed");



                    }
                    else{
                        JOptionPane.showMessageDialog(frame,
                                "Invalid glucagon value! Try again.",
                                "Error Message",
                                JOptionPane.ERROR_MESSAGE);

                    }

                }

            }
        });

        batterySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

                int new_battery_level = (int)batterySlider.getValue();

                pump.setBatteryLevel(new_battery_level);


            }
        });
        insulinSlider.addComponentListener(new ComponentAdapter() {
        });

        insulinSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

                int new_level = (int)insulinSlider.getValue();

                reservoir.setRemainingInsulin((double)new_level);
            }
        });
        glucagonSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int new_level = (int)glucagonSlider.getValue();

                reservoir.setRemainingGlucagon((int)new_level);
            }
        });
        changeBSLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {



                    float num1;
                    try {

                        num1 = Float.parseFloat(changeBSLTextField.getText());
                        double newLevel = Double.parseDouble(changeBSLTextField.getText());

                        try {
                            glucoseLevel.addCurrentLevel(newLevel);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }


                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame,
                                "Invalid value! Try again.",
                                "Error Message",
                                JOptionPane.ERROR_MESSAGE);
                    }


            }
        });
    }


}
