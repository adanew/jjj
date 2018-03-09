import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class PumpApp {
    private JFrame frame;
    private JTextField textField_insuline;
    private JButton add_insuline;
    private JTextField textField_glucagon;
    private JButton add_glucagon;
    private JTextArea textArea1;
    private JPanel MainPanel;
    private JTextArea textArea2;
    private JTextField textField_safeMidSugarLevel;
    private JTextField textField_safeMinSugarLevel;
    private JTextField textField_safeMaxSugarLevel;
    private JTextField currentBGL;
    private JButton lockAutoButton;
    private JPasswordField passwordField1;
    private JTextField weightTextField;
    private JButton setWeightButton;
    private JLabel weightUnit;
    private JLabel currentBGL_Label;
    private JPanel JPanel_graph;
    private JPanel MainTabbedPanel;
    private JPanel HistPanel;
    private JScrollPane ScrollPane1;
    private JScrollPane ScrollPane2;
    private JPanel HistTabbedPanel;
    private JPanel jPanelHist;
    private JTabbedPane jTabbedPane1;
    private JPanel Physician;
    private JPanel physicianPanel;
    private JCheckBox needleStatus;
    private JProgressBar insulinReservoirField;
    private JProgressBar glucagonReservoirField;
    private JPasswordField passwordLock;
    private JCheckBox autoModeCheckBox;
    private JCheckBox manualModeCheckBox;
    private JPasswordField manualPass;
    private JButton lockManualButton;
    private JProgressBar batteryField;
    private JComboBox comboBoxDDC;
    private JPanel checkBoxPanel;
    private JPanel levelsPanel;
    private JPanel bslPanel;
    private JLabel setDDCbutton;
    private JPanel manualPanel;
    private JPanel patientConfPanel;
    private JButton lockPatientConfButton;
    private JPanel unlockPumpPanel;
    private JPasswordField patientConfPass;
    private JTextField currentDDCText;
    private JLabel setWeightLabel;
    private JComboBox comboWeight;
    private JLabel statusLabel;
    private JPanel jPanelMain;
    private JPanel tabbedMain;
    private JLabel insulinUnit;
    private boolean restart;

    private SimulationInputs simulationInputs;

    //Since the reservoir it is updated by the Physician and the Pump, then we need a buffer
    private ReservoirBuffer reservoirBuffer = new ReservoirBuffer();
    private Reservoir reservoir = new Reservoir(reservoirBuffer, insulinReservoirField, glucagonReservoirField);
    private Thread reservoirThread = new Thread(reservoir);


    //Generate and setup the graph for the first time
    public Graph mainGraph = new Graph();

    private Patient patient = new Patient(75,0.4);
    private PendingDoses pendingDoses = new PendingDoses();
    

    // We create the blood object to initialize the level, and then we pass the object blood to a Thread to execute
    // the run method defined in the Blood class.
    // The queue is the buffer to receive insulin or glucagon
    // The Blood thread (Consumer) will read the queue and apply the changes in the blood level.
    private GlucoseLevel glucoseLevel = new GlucoseLevel(patient);
    private Buffer buffer = new Buffer();


    private Blood blood = new Blood(glucoseLevel,buffer,textArea1,textArea2);
    private Thread bloodThread = new Thread(blood);


    // We create the pumpThread (Producer) that will change the blood level when necessary, automatically.
    // We pass the queue to add the necessary insuline or glucagon
    private Pump pump = new Pump(glucoseLevel, buffer, patient, textArea1, textArea2, currentBGL, pendingDoses,
            JPanel_graph, mainGraph, needleStatus,reservoir,batteryField,statusLabel);
    private Thread pumpThread = new Thread(pump);


    // The physicianThread (Producer) will modify the blood levels on demand, adding insulin or glucagon,
    private Physician physician = new Physician(buffer,patient, textArea1, pendingDoses, reservoir, needleStatus,statusLabel);
    private Thread physicianThread = new Thread(physician);


    //Simulates the feeding process with random numbers
    private Feed feed = new Feed(buffer,patient, textArea1);
    private Thread feedThread = new Thread(feed);




    public PumpApp() {
         this.frame = new JFrame("Pump App");
         this.simulationInputs = new SimulationInputs(needleStatus,batteryField,physician, reservoir,pump,glucoseLevel);

         //Initialize sliders with values from pump and reservoir
         this.batteryField.setValue(pump.batteryLevel);
         this.glucagonReservoirField.setValue(reservoir.getRemainingPercentGlucagon());
         this.insulinReservoirField.setValue(reservoir.getRemainingPercentInsulin());


         this.currentBGL.setText(String.valueOf(patient.safeMidSugarLevel));


         //DDC
        if(patient.asudani_koeff == 0.3){
            comboBoxDDC.setSelectedIndex(0);
        }
        if(patient.asudani_koeff == 0.4){
             comboBoxDDC.setSelectedIndex(1);
         }
        if(patient.asudani_koeff == 0.5){
            comboBoxDDC.setSelectedIndex(2);
        }
        if(patient.asudani_koeff == 0.6){
            comboBoxDDC.setSelectedIndex(3);
        }


        //Weight
        //comboWeight.setSelectedItem();
        String weight =  String.valueOf(patient.getWeight());
        comboWeight.setSelectedItem(weight);



        add_insuline.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                float num1;
                try {

                    num1 = Float.parseFloat(textField_insuline.getText());
                    double new_insulin = Double.parseDouble(textField_insuline.getText());

                    if(new_insulin > 0) {

                        if (new_insulin > patient.maxSingleDoseInsulin) {
                            //Are you sure?
                            int i_answer = okcancel("Insulin dose more than max single dose, are you sure?");
                            //System.out.println(i_answer);

                            //0 means ok
                            if (i_answer == 0) {
                                physician.setInsulin(new_insulin);
                                textField_insuline.setText("");
                            }

                        } else {
                            physician.setInsulin(new_insulin);
                            textField_insuline.setText("");

                        }
                    }
                    else{
                        JOptionPane.showMessageDialog(frame,
                                "Invalid value! Try again.",
                                "Error Message",
                                JOptionPane.ERROR_MESSAGE);

                    }


                } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Invalid value! Try again.",
                        "Error Message",
                        JOptionPane.ERROR_MESSAGE);
                }






            }
        });

        add_glucagon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                float num1;
                try {

                    num1 = Float.parseFloat(textField_glucagon.getText());
                    double new_glucagon = Double.parseDouble(textField_glucagon.getText());

                    if(new_glucagon > 0){


                        if(new_glucagon > patient.maxSingleDoseGlucagon){

                            //Are you sure?
                            int i_answer = okcancel("Glucagon dose more than max single dose, are you sure?");
                            //System.out.println(i_answer);

                            //0 means ok
                            if(i_answer == 0){
                                physician.setGlucagon(new_glucagon,"Physician");
                                textField_glucagon.setText("");

                            }

                        }
                        else{
                            physician.setGlucagon(new_glucagon,"Physician");
                            textField_glucagon.setText("");

                        }





                    }
                    else{
                        JOptionPane.showMessageDialog(frame,
                                "Invalid value! Try again.",
                                "Error Message",
                                JOptionPane.ERROR_MESSAGE);

                    }




                }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Invalid value! Try again.",
                            "Error Message",
                            JOptionPane.ERROR_MESSAGE);
                }





            }
        });


        lockAutoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                char[] input = passwordField1.getPassword();

                if(isPasswordCorrect(input) && lockAutoButton.getText()=="Unlock"){
                    JOptionPane.showMessageDialog(frame,
                            "Pump Unlocked: Auto Mode.");

                    autoModeCheckBox.setEnabled(true);

                    lockAutoButton.setText("Lock");
                    lockAutoButton.setToolTipText("Insert Pass and lock Auto mode");


                }
                //Lock auto mode and manual mode
                else if(isPasswordCorrect(input) && lockAutoButton.getText()=="Lock"){
                    JOptionPane.showMessageDialog(frame,
                            "Pump Locked: Auto mode");



                    //auto opts
                    autoModeCheckBox.setEnabled(false);
                    lockAutoButton.setText("Unlock");
                    lockAutoButton.setToolTipText("Insert Pass to unlock Auto mode.");




                }
                else if(!(isPasswordCorrect(input))){
                    JOptionPane.showMessageDialog(frame,
                            "Invalid password. Try again.",
                            "Error Message",
                            JOptionPane.ERROR_MESSAGE);

                    passwordField1.selectAll();
                }


                passwordField1.setText("");
            }
        });





        autoModeCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(autoModeCheckBox.isSelected()){
                    pump.auto=true;
                }
                if(!(autoModeCheckBox.isSelected())){
                    pump.auto=false;
                }
            }
        });

        lockManualButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                char[] input = manualPass.getPassword();

                //Unlock auto mode and manual mode
                if(isPasswordCorrect(input) && lockManualButton.getText()=="Unlock"){
                    JOptionPane.showMessageDialog(frame,
                            "Pump Unlocked: Manual mode.");

                    //manual opts
                    add_insuline.setEnabled(true);
                    add_glucagon.setEnabled(true);
                    textField_glucagon.setEnabled(true);
                    textField_insuline.setEnabled(true);
                    textField_glucagon.setText("");
                    textField_insuline.setText("");
                    manualModeCheckBox.setSelected(true);
                    lockManualButton.setText("Lock");
                    lockManualButton.setToolTipText("Insert Pass and lock Manual mode.");





                }
                else if(isPasswordCorrect(input) && lockManualButton.getText()=="Lock"){
                    JOptionPane.showMessageDialog(frame,
                            "Pump Locked: Manual mode.");

                    add_insuline.setEnabled(false);
                    add_glucagon.setEnabled(false);
                    textField_glucagon.setEnabled(false);
                    textField_insuline.setEnabled(false);
                    textField_glucagon.setText("");
                    textField_insuline.setText("");
                    manualModeCheckBox.setSelected(false);

                    lockManualButton.setText("Unlock");
                    lockManualButton.setToolTipText("Insert Pass to unlock Manual mode.");


                }
                else if(!(isPasswordCorrect(input))){
                    JOptionPane.showMessageDialog(frame,
                            "Invalid password. Try again.",
                            "Error Message",
                            JOptionPane.ERROR_MESSAGE);

                    passwordField1.selectAll();
                }

                manualPass.setText("");

            }
        });



        lockPatientConfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                char[] input = patientConfPass.getPassword();

                if(isPasswordCorrect(input) && lockPatientConfButton.getText()=="Unlock"){
                    JOptionPane.showMessageDialog(frame,
                            "Pump Unlocked: Patient Configuration");
                    comboBoxDDC.setEnabled(true);
                    comboWeight.setEnabled(true);
                    lockPatientConfButton.setText("Lock");

                }
                else if(isPasswordCorrect(input) && lockPatientConfButton.getText()=="Lock"){
                    JOptionPane.showMessageDialog(frame,
                            "Pump Locked: Patient Configuration");
                    comboBoxDDC.setEnabled(false);
                    comboWeight.setEnabled(false);
                    lockPatientConfButton.setText("Unlock");

                }
                else if(!(isPasswordCorrect(input))){
                    JOptionPane.showMessageDialog(frame,
                            "Invalid password. Try again.",
                            "Error Message",
                            JOptionPane.ERROR_MESSAGE);

                    passwordField1.selectAll();
                }
                patientConfPass.setText("");

            }
        });
        comboBoxDDC.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(comboBoxDDC.getSelectedIndex() == 0){
                    patient.asudani_koeff = 0.3;
                }
                if(comboBoxDDC.getSelectedIndex() == 1){
                    patient.asudani_koeff = 0.4;
                }
                if(comboBoxDDC.getSelectedIndex() == 2){
                    patient.asudani_koeff = 0.5;
                }
                if(comboBoxDDC.getSelectedIndex() == 3){
                    patient.asudani_koeff = 0.6;
                }

            }
        });


        comboWeight.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double weight =  Double.parseDouble((String) comboWeight.getSelectedItem());
                patient.setWeight(weight);
                //System.out.println(patient.getWeight());
            }
        });
    }


    public static int okcancel(String theMessage) {
        int result = JOptionPane.showConfirmDialog((Component) null, theMessage,
                "alert", JOptionPane.OK_CANCEL_OPTION);
        return result;
    }

    public boolean isPasswordCorrect(char[] input){
        String pass= "123";
        String pass_received = new String(input);


        if(pass_received.equals(pass) ){
            return true;
        }
        else{
            return false;
        }

    }




    /** Returns an ImageIcon, or null if the path was invalid. */
    private static ImageIcon createImageIcon(String path) {



        ClassLoader classLoader;
        classLoader = PumpApp.class.getClassLoader();
        //File file = new File(classLoader.getResource("images/"+path).getFile());

        java.net.URL imgURL = classLoader.getResource("images/"+path);

        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public static void main(String[] args) {



        PumpApp pumpapp = new PumpApp();


        //Start the pump with needle
        pumpapp.needleStatus.setSelected(true);


       //out graph in panel
        pumpapp.JPanel_graph.setLayout(new BorderLayout());
        pumpapp.JPanel_graph.add(pumpapp.mainGraph.chartPanel, BorderLayout.NORTH);

        //Icon tabs
        ImageIcon graph_icon = createImageIcon("graph_icon.png");
        ImageIcon hist_icon = createImageIcon("hist_icon.png");
        ImageIcon doc_icon = createImageIcon("doc_icon.png");
        pumpapp.jTabbedPane1.setIconAt(0,graph_icon);
        pumpapp.jTabbedPane1.setIconAt(2,hist_icon);
        pumpapp.jTabbedPane1.setIconAt(1,doc_icon);


        //set main frame visible
        pumpapp.frame.setContentPane(pumpapp.MainPanel);
        pumpapp.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pumpapp.frame.pack();
        pumpapp. frame.setVisible(true);


        //This thread updates the reservoir percentage
        pumpapp.reservoirThread.start();


        //Consumer, with while true
        pumpapp.bloodThread.start();
        String bloodThreadName = pumpapp.bloodThread.getName();
        //System.out.println("Blood: "+bloodThreadName);


        //Producer, it will be started by the button
        pumpapp.pumpThread.start();
        String pumpThreadName = pumpapp.pumpThread.getName();
        //System.out.println("Pump: "+pumpThreadName);


        pumpapp.physicianThread.start();
        String physicianThreadName = pumpapp.physicianThread.getName();
        //System.out.println("Physician "+physicianThreadName);


        //Producer, with while true
        //pumpapp.feedThread.start();
        //String feedThreadName = pumpapp.feedThread.getName();
        //System.out.println("Feed: "+feedThreadName);



        // Wait for threads to end
        try {
            pumpapp.bloodThread.join();
            pumpapp.pumpThread.join();
            pumpapp.feedThread.join();
            pumpapp.physicianThread.join();
            pumpapp.reservoirThread.join();

        } catch ( Exception e) {
            System.out.println("Threads Interrupted!!");
        }


    }



}
