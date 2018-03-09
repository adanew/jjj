import javax.swing.*;
import java.awt.*;


//Producer
public class Pump implements Runnable {

    private JTextArea textArea;
    private JTextArea textArea2;
    private JTextField currentBGL;
    private JCheckBox needleStatus;
    private GlucoseLevel glucoseLevel;

    private Buffer buffer;
    public boolean pumpOn;

    public double reading2;
    public double reading1;
    public double reading0;

    //Reservoir Capacity
    private Reservoir reservoir;


    //Dose variables
    private double compDose;
    private double insulinDose;
    private double glucagonDose;


    private Patient patient;
    private PendingDoses pendingDoses;

    //GraphDisplay class object to invoke repaint() function which refreshes the graph every five seconds
    public Graph mainGraph = new Graph();
    private JPanel JPanel_graph;


    public Boolean auto;
    public Boolean needle;

    public JProgressBar batteryField;
    public int batteryLevel;
    private JLabel statusLabel;


    public Pump(GlucoseLevel glucoseLevel, Buffer buffer, Patient patient, JTextArea textArea, JTextArea textArea2, JTextField currentBGL,
                PendingDoses pendingDoses, JPanel JPanel_graph, Graph mainGraph,
                JCheckBox needleStatus, Reservoir reservoir,JProgressBar batteryField,JLabel statusLabel){
        this.glucoseLevel = glucoseLevel;
        this.buffer = buffer;

        this.textArea = textArea;
        this.textArea2 = textArea2;
        this.currentBGL = currentBGL;
        this.pumpOn = true;

        this.reservoir = reservoir;

        this.patient = patient;
        this.pendingDoses = pendingDoses;

        this.JPanel_graph = JPanel_graph;
        this.mainGraph = mainGraph;

        this.needleStatus = needleStatus;
        this.needle = true;

        this.auto = false;

        reading2=patient.safeMidSugarLevel;
        reading1=reading2;
        reading0=reading1;

        this.batteryField = batteryField;

        batteryLevel = 100;

        this.statusLabel = statusLabel;

    }



    @Override
    public void run() {




        //The pump needs to monitor the glucose level so we need
        while(true){

            //We need to sleep the pump to allow new values to appear from the Physician
            //Pause for 1 seconds
            try {
                Thread.currentThread().sleep(7000); // changes for 5 sec increasing



            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            //Get the last 3 glucose readings from the list of the Glucose Level
            try {
                getGlucoseReadings();


                //Update Graph
                mainGraph.addReading(glucoseLevel.getCurrentLevel(glucoseLevel.getSize()-1), patient.safeMaxSugarLevel, patient.safeMinSugarLevel);





            } catch (InterruptedException e) {
                e.printStackTrace();
            }





            calculateInjectionDose(reading2,reading1,reading0);
            //System.out.println("dose:"+dose);


            checkBatteryLevel();




            textArea2.append( TimeStamp.getTimeStamp()+" - Current: ["+ Round.round(reading2,3)  +"] Prev: ["+ Round.round(reading1,3) + "] Prev-1: ["+ Round.round(reading0,3) +"]\n");

            //Display current BGL in GUI
            if(reading2 >= patient.safeMaxSugarLevel || reading2 <= patient.safeMinSugarLevel){

                currentBGL.setText(String.format("%.2f", reading2));
                currentBGL.setForeground(Color.red);

            }
            else{
                currentBGL.setText(String.format("%.2f", reading2));
                currentBGL.setForeground(Color.black);
            }





        }//while



    }//run

    private void checkBatteryLevel() {
        int batteryLevel = batteryField.getValue();
        if(batteryLevel <= 20 &&  batteryLevel != 0){

            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Low Battery: " +batteryLevel+ "% \n");
            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Low Battery: " +batteryLevel+ "% \n");
        }


    }

    public void setBatteryLevel(int level){

        batteryLevel = level;
        batteryField.setValue(level);
    }


    public void getGlucoseReadings()throws InterruptedException{

        int listSize = glucoseLevel.getSize();


        //We update only taking the last value in the GlucoseLevel
        reading0= reading1;
        reading1= reading2;
        reading2= glucoseLevel.getCurrentLevel(listSize-1);





    }



    private void createInsulinThread(double dose, Buffer buffer, Patient patient,PendingDoses pendingDoses) {

        //The creation time of the thread will be the current time minus the startSimulationTime
        long creationTime = System.nanoTime();


        // Initialize insulin object
        Insulin insulin = new Insulin(buffer,creationTime,dose,patient,pendingDoses);


        /* To use when Insulin implements interface Runnable*/

        // Create thread with object
        Thread insulinThread = new Thread(insulin);

        // Change the thread name to identify it later
        String threadName = "Insulin-"+insulinThread.getName();
        insulinThread.setName(threadName);

        //Start thread
        insulinThread.start();







    }
    private void createGlucagonThread(double dose, Buffer buffer, Patient patient,PendingDoses pendingDoses) {

        //The creation time of the thread will be the current time minus the startSimulationTime
        long creationTime = System.nanoTime();

        // Initialize insulin object
        Glucagon glucagon = new Glucagon(buffer,creationTime,dose,patient,pendingDoses);

        // Create thread with object
        Thread glucagonThread = new Thread(glucagon);

        // Change the thread name to identify it later
        String threadName = "Glucagon-"+glucagonThread.getName();
        glucagonThread.setName(threadName);

        //Start thread
        glucagonThread.start();

    }

    private void calculateInjectionDose(double reading2, double reading1, double reading0) {


        insulinDose = 0;
        glucagonDose = 0;
        compDose = 0;



        double safeMinSugarLevel = patient.safeMinSugarLevel;
        double safeMaxSugarLevel = patient.safeMaxSugarLevel;
        double safeMidSugarLevel = patient.safeMidSugarLevel;

        double minSingleDoseInsulin = patient.minSingleDoseInsulin;
        double maxSingleDoseInsulin = patient.maxSingleDoseInsulin;

        double minSingleDoseGlucagon = patient.minSingleDoseGlucagon;
        double maxSingleDoseGlucagon = patient.maxSingleDoseGlucagon;

        double insMaxDailyDose = patient.getInsMaxDailyDose();
        double insCumulativeDose = patient.getInsCumulativeDose();
        double gluMaxDailyDose = patient.getGluMaxDailyDose();
        double gluCumulativeDose = patient.getGluCumulativeDose();

        double remainingInsulin = reservoir.getRemainingInsulin();
        double remainingGlucagon = reservoir.getRemainingGlucagon();

        //Check if we have needle
        needle = needleStatus.isSelected();




        double gLevel = 0;
        try {
            gLevel = glucoseLevel.getLastCurrentLevel();
            gLevel = Round.round(gLevel,2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if(batteryLevel == 0){

            textArea.append(TimeStamp.getTimeStamp()+" - WAR: No Battery! \n");
            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No Battery! \n");

        }
        else{



            if(reading2 == safeMidSugarLevel){
                textArea.append(TimeStamp.getTimeStamp()+" - INF: BSL: " + gLevel + " Stable." + "\n");
                statusLabel.setText(TimeStamp.getTimeStamp()+" - INF: BSL: " + gLevel + " Stable." + "\n");


            }


            //Alerts for low levels in reservoir
            lowLevelsReservoirAlerts();


            //////--- INSULIN ---

            //-- In the upper safe bound, then NOT inject, just warn the patient
            if (reading2 > safeMidSugarLevel && reading2 < safeMaxSugarLevel) {

                //Sugar level stable
                if (reading2 == reading1) {
                    compDose = 0;
                    textArea.append(TimeStamp.getTimeStamp()+" - INF: BSL: " + gLevel + " Stable." + "\n");
                    statusLabel.setText(TimeStamp.getTimeStamp()+" - INF: BSL: " + gLevel + " Stable." + "\n");

                }

                //Sugar level falling
                else if (reading2 < reading1) {
                    compDose = 0;
                    textArea.append(TimeStamp.getTimeStamp()+" - INF: BSL: " + gLevel + " Falling." + "\n");
                    statusLabel.setText(TimeStamp.getTimeStamp()+" - INF: BSL: " + gLevel + " Falling." + "\n");

                }

                //Sugar level increasing, but rate of increase slowing
                else if (reading2 > reading1 && ((reading2 - reading1) < (reading1 - reading0))) {
                    compDose = 0;
                    textArea.append(TimeStamp.getTimeStamp()+" - INF: BSL: " + gLevel + " between Mid and Max. Increase rate slowing." + "\n");
                    statusLabel.setText(TimeStamp.getTimeStamp()+" - INF: BSL: " + gLevel + " between Mid and Max. Increase rate slowing." + "\n");
                }

                //Sugar level increasing, but rate of increase accelerating
                else if (reading2 > reading1 && ((reading2 - reading1) >= (reading1 - reading0))) {
                    compDose = 0;
                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " between Mid and Max. Increase rate accelerating!" + "\n");
                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " between Mid and Max. Increase rate accelerating!" + "\n");
                }

            }

            //-- In the upper NOT safe bound, then  Inject or Warning
            else if (reading2 >= safeMaxSugarLevel) {


                //-- Stable, just inject the difference between current BSL and normal BSl
                if (reading2 == reading1) {
                    //Inject max,min or dose between

                    double tempcompDose1 =  ((reading2 - safeMaxSugarLevel) / patient.correction_ratio); //may be take average level? 10?
                    double tempcompDose2 =  tempcompDose1  / maxSingleDoseInsulin;
                    //compDose = round(tempcompDose2);
                    compDose = tempcompDose2;

                    //System.out.println("reading2: "+reading2+ " -safeMaxSugarLevel: "+safeMaxSugarLevel+ " tempcompDose1: "+tempcompDose1+ " tempcompDose2: "+tempcompDose2+
                    //      " compDose: "+compDose);

                    //Inject between minimum dose and max dose
                    if( (compDose>0 && compDose<1) && tempcompDose1>minSingleDoseInsulin){
                        //if(compDose==0){ //round


                        insulinDose = tempcompDose1 * -1;



                        if(insCumulativeDose + insulinDose <= insMaxDailyDose){
                            if(remainingInsulin >= tempcompDose1){



                                if (auto){
                                    if(needle){
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over the safe levels! Stable. Injects Insulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over the safe levels! Stable. Injects Insulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                        createInsulinThread(insulinDose,buffer,patient,pendingDoses);

                                        //Update cumulative Dose, and remainingInsulin
                                        patient.updateInsulinCumulativeDose(insulinDose);
                                        reservoir.reservoirBuffer.addQueue(insulinDose);
                                    }
                                    else{
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                    }

                                }
                                else{
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                }





                            }
                            else{
                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");

                            }

                        }
                        else{
                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                        }



                    }


                    //Inject minimum dose
                    else if( (compDose>0 && compDose<1) && tempcompDose1<=minSingleDoseInsulin){
                        //if(compDose==0){ //round


                        insulinDose = minSingleDoseInsulin * -1;




                        if(insCumulativeDose + insulinDose <= insMaxDailyDose){
                            if(remainingInsulin >= tempcompDose1){




                                if (auto){
                                    if(needle){
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over the safe levels! Stable. Injects MinInsulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over the safe levels! Stable. Injects MinInsulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                        createInsulinThread(insulinDose,buffer,patient,pendingDoses);

                                        //Update cumulative Dose, and remainingInsulin
                                        patient.updateInsulinCumulativeDose(insulinDose);
                                        reservoir.reservoirBuffer.addQueue(insulinDose);
                                    }
                                    else{
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                    }

                                }
                                else{
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                }





                            }
                            else{
                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                            }

                        }
                        else{
                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                        }


                    }



                    //Inject max dose
                    else if(compDose>=1){
                        //if(compDose>0){ //round



                        insulinDose = maxSingleDoseInsulin * -1;

                        Round.round(insulinDose,3);




                        if(insCumulativeDose + insulinDose <= insMaxDailyDose){
                            if(remainingInsulin >= tempcompDose1){




                                if (auto){
                                    if(needle){
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over the safe levels! Stable. Injects MaxInsulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over the safe levels! Stable. Injects MaxInsulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                        createInsulinThread(insulinDose,buffer,patient,pendingDoses);

                                        //Update cumulative Dose, and remainingInsulin
                                        patient.updateInsulinCumulativeDose(insulinDose);
                                        reservoir.reservoirBuffer.addQueue(insulinDose);
                                    }
                                    else{
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                    }

                                }
                                else{
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                }




                            }
                            else{
                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                            }

                        }
                        else{
                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                        }

                    }

                }

                //-- Decreasing, not inject, just warn the patient, and wait for next readings.
                else if (reading2 < reading1) { // reading1 is equal to 0, but! there should be value
                    compDose = 0;
                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over Max, but falling! " + "\n");
                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over Max, but falling! " + "\n");

                }

                //-- Increasing, Inject, but check the future remaining doses of insulin.  Next cases:
                else if (reading2 > reading1) {
                    //System.out.println("BSL Increasing...");

                    //futureDoses will be negative if the future doses are only insulin, but it could be positive
                    double futureDoses = checkFutDoses();

                    //System.out.println("futureDoses: "+futureDoses);

                    //-- Increasing and slowing, then inject
                    if((reading2 - reading1) < (reading1 - reading0)){
                        //System.out.println("Increasing and slowing, then inject...");

                        //If even after injecting all the future insulin doses the BSL is over the safeMaxSugarLevel, then inject
                        if(reading2+futureDoses >= safeMaxSugarLevel){

                            double tempcompDose1 =  ((reading2 + futureDoses - safeMaxSugarLevel) / patient.correction_ratio);
                            double tempcompDose2 =  tempcompDose1  / maxSingleDoseInsulin;
                            //compDose = round(tempcompDose2);
                            compDose = tempcompDose2;


                            //System.out.println("reading2: "+reading2+ " -safeMaxSugarLevel: "+safeMaxSugarLevel+ " tempcompDose1: "+tempcompDose1+ " tempcompDose2: "+tempcompDose2+
                            //  " compDose: "+compDose);


                            //Inject between minimum dose and max dose
                            if( (compDose>0 && compDose<1) && tempcompDose1>minSingleDoseInsulin){
                                //if(compDose==0){ //round


                                insulinDose = tempcompDose1 * -1;



                                if (insCumulativeDose + insulinDose <= insMaxDailyDose) {
                                    if (remainingInsulin >= tempcompDose1) {



                                        if (auto){
                                            if(needle){
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over the safe levels! Increase rate slowing. Injects Insulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over the safe levels! Increase rate slowing. Injects Insulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                                createInsulinThread(insulinDose,buffer,patient,pendingDoses);

                                                //Update cumulative Dose, and remainingInsulin
                                                patient.updateInsulinCumulativeDose(insulinDose);
                                                reservoir.reservoirBuffer.addQueue(insulinDose);
                                            }
                                            else{
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                            }

                                        }
                                        else{
                                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                        }




                                    } else {
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                                    }

                                } else {
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                                }




                            }



                            //Inject minimum dose
                            //if(compDose==0){ //round
                            else if( (compDose>0 && compDose<1) && tempcompDose1<=minSingleDoseInsulin){

                                insulinDose = minSingleDoseInsulin * -1;



                                if (insCumulativeDose + insulinDose <= insMaxDailyDose) {
                                    if (remainingInsulin >= tempcompDose1) {




                                        if (auto){
                                            if(needle){
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over the safe levels! Increase rate slowing. Injects MinInsulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over the safe levels! Increase rate slowing. Injects MinInsulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                                createInsulinThread(insulinDose,buffer,patient,pendingDoses);

                                                //Update cumulative Dose, and remainingInsulin
                                                patient.updateInsulinCumulativeDose(insulinDose);
                                                reservoir.reservoirBuffer.addQueue(insulinDose);
                                            }
                                            else{
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                            }

                                        }
                                        else{
                                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                        }



                                    } else {
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                                    }

                                } else {
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                                }




                            }




                            //Inject max dose
                            //if(compDose>0){ //round
                            else if(compDose>=1){


                                insulinDose = maxSingleDoseInsulin * -1;
                                Round.round(insulinDose,3);




                                if(insCumulativeDose + insulinDose <= insMaxDailyDose){
                                    if(remainingInsulin >= tempcompDose1){



                                        if (auto){
                                            if(needle){
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: "+gLevel+" over the safe levels! Increase rate slowing. Injects MaxInsulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: "+gLevel+" over the safe levels! Increase rate slowing. Injects MaxInsulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                                createInsulinThread(insulinDose,buffer,patient,pendingDoses);

                                                //Update cumulative Dose, and remainingInsulin
                                                patient.updateInsulinCumulativeDose(insulinDose);
                                                reservoir.reservoirBuffer.addQueue(insulinDose);
                                            }
                                            else{
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                            }

                                        }
                                        else{
                                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                        }




                                    }
                                    else{
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                                    }

                                }
                                else{
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                                }




                            }


                        }

                        //If after injecting all the future insulin doses the BSL is under the safeMaxSugarLevel, then DON'T inject, just warn
                        else if(reading2+futureDoses < safeMaxSugarLevel){
                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over Max, but insulin injections will take effect! " + "\n");
                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over Max, but insulin injections will take effect! " + "\n");
                        }

                    }


                    //-- Increasing and accelerating, then inject
                    else if((reading2 - reading1) >= (reading1 - reading0)){
                        //System.out.println("Increasing and accelerating, then inject...");

                        //If even after injecting all the future insulin doses the BSL is over the safeMaxSugarLevel, then inject
                        if(reading2+futureDoses >= safeMaxSugarLevel){

                            double tempcompDose1 =  ((reading2 + futureDoses - safeMaxSugarLevel) / patient.correction_ratio);
                            double tempcompDose2 =  tempcompDose1  / maxSingleDoseInsulin;
                            //compDose = round(tempcompDose2);
                            compDose = tempcompDose2;


                            //System.out.println("reading2: "+reading2+ " -safeMaxSugarLevel: "+safeMaxSugarLevel+ " tempcompDose1: "+tempcompDose1+ " tempcompDose2: "+tempcompDose2+
                            //      " compDose: "+compDose);





                            //Inject between minimum dose and max dose
                            if( (compDose>0 && compDose<1) && tempcompDose1>minSingleDoseInsulin){
                                //if(compDose==0){ //round


                                insulinDose = tempcompDose1 * -1;



                                if (insCumulativeDose + insulinDose <= insMaxDailyDose) {
                                    if (remainingInsulin >= tempcompDose1) {

                                        if (auto){
                                            if(needle){
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over the safe levels! Increase rate accelerating. Injects Insulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over the safe levels! Increase rate accelerating. Injects Insulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                                createInsulinThread(insulinDose,buffer,patient,pendingDoses);

                                                //Update cumulative Dose, and remainingInsulin
                                                patient.updateInsulinCumulativeDose(insulinDose);
                                                reservoir.reservoirBuffer.addQueue(insulinDose);
                                            }
                                            else{
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                            }

                                        }
                                        else{
                                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                        }





                                    } else {
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                                    }

                                } else {
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                                }




                            }



                            //Inject minimum dose
                            //if(compDose==0){ //round
                            else if( (compDose>0 && compDose<1) && tempcompDose1<=minSingleDoseInsulin){

                                insulinDose = minSingleDoseInsulin * -1;



                                if (insCumulativeDose + insulinDose <= insMaxDailyDose) {
                                    if (remainingInsulin >= tempcompDose1) {



                                        if (auto){
                                            if(needle){
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over the safe levels! Increase rate accelerating. Injects MinInsulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over the safe levels! Increase rate accelerating. Injects MinInsulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                                createInsulinThread(insulinDose,buffer,patient,pendingDoses);

                                                //Update cumulative Dose, and remainingInsulin
                                                patient.updateInsulinCumulativeDose(insulinDose);
                                                reservoir.reservoirBuffer.addQueue(insulinDose);
                                            }
                                            else{
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                            }

                                        }
                                        else{
                                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                        }



                                    } else {
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                                    }

                                } else {
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                                }




                            }




                            //Inject max dose
                            //if(compDose>0){ //round
                            else if(compDose>=1){


                                insulinDose = maxSingleDoseInsulin * -1;
                                Round.round(insulinDose,3);




                                if(insCumulativeDose + insulinDose <= insMaxDailyDose){
                                    if(remainingInsulin >= tempcompDose1){

                                        if (auto){
                                            if(needle){
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: "+gLevel+" over the safe levels! Increase rate accelerating. Injects MaxInsulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: "+gLevel+" over the safe levels! Increase rate accelerating. Injects MaxInsulin: " + Round.round(insulinDose,3) + " (IU)\n");
                                                createInsulinThread(insulinDose,buffer,patient,pendingDoses);

                                                //Update cumulative Dose, and remainingInsulin
                                                patient.updateInsulinCumulativeDose(insulinDose);
                                                reservoir.reservoirBuffer.addQueue(insulinDose);
                                            }
                                            else{
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " over the safe levels!" + "\n");
                                            }

                                        }
                                        else{
                                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " over the safe levels!" + "\n");
                                        }



                                    }
                                    else{
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough insulin in the reservoir to inject!" + "\n");
                                    }

                                }
                                else{
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily insulin dose exceeded!" + "\n");
                                }




                            }






                        }

                        //If after injecting all the future insulin doses the BSL is under the safeMaxSugarLevel, then DON'T inject, just warn
                        else if(reading2+futureDoses < safeMaxSugarLevel){
                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over Max, but insulin injections will take effect! " + "\n");
                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " over Max, but insulin injections will take effect! " + "\n");
                        }

                    }

                }













            }




            //////--- GLUCAGON ---

            //-- In the lower safe bound, then NOT inject, just warn the patient
            else if (reading2 < safeMidSugarLevel && reading2 > safeMinSugarLevel) {

                //Sugar level stable
                if (reading2 == reading1) {
                    compDose = 0;
                    textArea.append(TimeStamp.getTimeStamp()+" - INF: BSL: " + gLevel + " Stable." + "\n");
                    statusLabel.setText(TimeStamp.getTimeStamp()+" - INF: BSL: " + gLevel + " Stable." + "\n");

                }

                //Sugar level raising
                else if (reading2 > reading1) {
                    compDose = 0;
                    textArea.append(TimeStamp.getTimeStamp()+" - INF: BSL: " + gLevel + " Raising." + "\n");
                    statusLabel.setText(TimeStamp.getTimeStamp()+" - INF: BSL: " + gLevel + " Raising." + "\n");

                }

                //Sugar level decreasing, but rate of increase slowing
                else if (reading2 < reading1 && ((reading1 - reading2) < (reading0 - reading1))) {
                    compDose = 0;
                    textArea.append(TimeStamp.getTimeStamp()+" - INF: BSL: " + gLevel + " between Mid and Min. Decrease rate slowing." + "\n");
                    statusLabel.setText(TimeStamp.getTimeStamp()+" - INF: BSL: " + gLevel + " between Mid and Min. Decrease rate slowing." + "\n");
                }

                //Sugar level decreasing, but rate of increase accelerating
                else if (reading2 < reading1 && ((reading1 - reading2) >= (reading0 - reading1))) {
                    compDose = 0;
                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " between Mid and Min. Decrease rate accelerating!" + "\n");
                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " between Mid and Min. Decrease rate accelerating!" + "\n");
                }

            }
////////////////////////////////////



            //-- In the lower NOT safe bound, then  Inject or Warning
            else if (reading2 <= safeMinSugarLevel) {


                //-- Stable, just inject the difference between current BSL and normal BSl
                if (reading2 == reading1) {
                    //Inject max,min or dose between

                    double tempcompDose1 =  ((safeMinSugarLevel - reading2) / patient.correction_ratio);
                    double tempcompDose2 =  tempcompDose1  / maxSingleDoseGlucagon;
                    //compDose = round(tempcompDose2);
                    compDose = tempcompDose2;

                    //System.out.println("reading2: "+reading2+ " -safeMaxSugarLevel: "+safeMaxSugarLevel+ " tempcompDose1: "+tempcompDose1+ " tempcompDose2: "+tempcompDose2+
                    //      " compDose: "+compDose);

                    //Inject between minimum dose and max dose
                    if( (compDose>0 && compDose<1) && tempcompDose1>minSingleDoseGlucagon){
                        //if(compDose==0){ //round


                        glucagonDose = tempcompDose1;



                        if(gluCumulativeDose + glucagonDose <= gluMaxDailyDose){
                            if(remainingGlucagon >= glucagonDose){


                                if (auto){
                                    if(needle){
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under the safe levels! Stable. Injects Glucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under the safe levels! Stable. Injects Glucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                        createGlucagonThread(glucagonDose,buffer,patient,pendingDoses);

                                        //Update cumulative Dose, and remainingGlucagon
                                        reservoir.reservoirBuffer.addQueue(glucagonDose);
                                        patient.updateGlucagonCumulativeDose(glucagonDose);
                                    }
                                    else{
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                    }

                                }
                                else{
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                }






                            }
                            else{
                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                            }

                        }
                        else{
                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                        }



                    }


                    //Inject minimum dose
                    else if( (compDose>0 && compDose<1) && tempcompDose1<=minSingleDoseGlucagon){
                        //if(compDose==0){ //round


                        glucagonDose = minSingleDoseGlucagon;




                        if(gluCumulativeDose + glucagonDose <= gluMaxDailyDose){
                            if(remainingGlucagon >= glucagonDose){

                                if (auto){
                                    if(needle){
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under the safe levels! Stable. Injects MinGlucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under the safe levels! Stable. Injects MinGlucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                        createGlucagonThread(glucagonDose,buffer,patient,pendingDoses);

                                        //Update cumulative Dose, and remainingGlucagon
                                        reservoir.reservoirBuffer.addQueue(glucagonDose);
                                        patient.updateGlucagonCumulativeDose(glucagonDose);

                                    }
                                    else{
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                    }

                                }
                                else{
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                }



                            }
                            else{
                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                            }

                        }
                        else{
                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                        }


                    }



                    //Inject max dose
                    else if(compDose>=1){
                        //if(compDose>0){ //round



                        glucagonDose = maxSingleDoseGlucagon;

                        Round.round(glucagonDose,3);



                        if(gluCumulativeDose + glucagonDose <= gluMaxDailyDose){
                            if(remainingGlucagon >= glucagonDose){

                                if (auto){
                                    if(needle){
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under the safe levels! Stable. Injects MaxGlucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under the safe levels! Stable. Injects MaxGlucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                        createGlucagonThread(glucagonDose,buffer,patient,pendingDoses);

                                        //Update cumulative Dose, and remainingGlucagon
                                        reservoir.reservoirBuffer.addQueue(glucagonDose);
                                        patient.updateGlucagonCumulativeDose(glucagonDose);
                                    }
                                    else{
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                    }

                                }
                                else{
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                }




                            }
                            else{
                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                            }

                        }
                        else{
                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                        }





                    }

                }




                //-- Increasing, not inject, just warn the patient, and wait for next readings.
                else if (reading2 > reading1) {
                    compDose = 0;
                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under Min, but rasing! " + "\n");
                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under Min, but rasing! " + "\n");

                }




                //-- Decreasing, Inject, but check the future remaining doses of Glucagon.  Next cases:
                else if (reading2 < reading1) {




                    //futureDoses will be negative if the future doses are only insulin, but it could be positive when glucagon
                    double futureDoses = checkFutDoses();

                    //System.out.println("futureDoses: "+futureDoses);




                    //-- Decreasing and slowing, then inject
                    if((reading1 - reading2) < (reading0 - reading1)){
                        //System.out.println("Decreasing and slowing, then inject...");

                        //If even after injecting all the future Glucagon doses the BSL is under the safeMinSugarLevel, then inject
                        if(reading2+futureDoses <= safeMinSugarLevel){


                            double tempcompDose1 =  ((safeMinSugarLevel - reading2 + futureDoses) / patient.correction_ratio);
                            double tempcompDose2 =  tempcompDose1  / maxSingleDoseGlucagon;
                            //compDose = round(tempcompDose2);
                            compDose = tempcompDose2;



                            //System.out.println("reading2: "+reading2+ " -safeMaxSugarLevel: "+safeMaxSugarLevel+ " tempcompDose1: "+tempcompDose1+ " tempcompDose2: "+tempcompDose2+
                            //  " compDose: "+compDose);


                            //Inject between minimum dose and max dose
                            if( (compDose>0 && compDose<1) && tempcompDose1>minSingleDoseGlucagon){
                                //if(compDose==0){ //round


                                glucagonDose = tempcompDose1;





                                if(gluCumulativeDose + glucagonDose <= gluMaxDailyDose){
                                    if(remainingGlucagon >= glucagonDose){

                                        if (auto){
                                            if(needle){
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under the safe levels! Decrease rate slowing. Injects Glucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under the safe levels! Decrease rate slowing. Injects Glucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                                createGlucagonThread(glucagonDose,buffer,patient,pendingDoses);

                                                //Update cumulative Dose, and remainingGlucagon
                                                reservoir.reservoirBuffer.addQueue(glucagonDose);
                                                patient.updateGlucagonCumulativeDose(glucagonDose);
                                            }
                                            else{
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                            }

                                        }
                                        else{
                                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                        }




                                    }
                                    else{
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                                    }

                                }
                                else{
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                                }




                            }



                            //Inject minimum dose
                            //if(compDose==0){ //round
                            else if( (compDose>0 && compDose<1) && tempcompDose1<=minSingleDoseGlucagon){

                                glucagonDose = minSingleDoseGlucagon;




                                if(gluCumulativeDose + glucagonDose <= gluMaxDailyDose){
                                    if(remainingGlucagon >= glucagonDose){

                                        if (auto){
                                            if(needle){
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under the safe levels! Decrease rate slowing. Injects MinGlucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under the safe levels! Decrease rate slowing. Injects MinGlucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                                createGlucagonThread(glucagonDose,buffer,patient,pendingDoses);

                                                //Update cumulative Dose, and remainingGlucagon
                                                reservoir.reservoirBuffer.addQueue(glucagonDose);
                                                patient.updateGlucagonCumulativeDose(glucagonDose);

                                            }
                                            else{
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                            }

                                        }
                                        else{
                                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                        }



                                    }
                                    else{
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                                    }

                                }
                                else{
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                                }





                            }




                            //Inject max dose
                            //if(compDose>0){ //round
                            else if(compDose>=1){


                                glucagonDose = maxSingleDoseGlucagon;
                                Round.round(glucagonDose,3);






                                if(gluCumulativeDose + glucagonDose <= gluMaxDailyDose){
                                    if(remainingGlucagon >= glucagonDose){

                                        if (auto){
                                            if(needle){
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: "+gLevel+" under the safe levels! Decrease rate slowing. Injects MaxGlucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: "+gLevel+" under the safe levels! Decrease rate slowing. Injects MaxGlucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                                createGlucagonThread(glucagonDose,buffer,patient,pendingDoses);

                                                //Update cumulative Dose, and remainingGlucagon
                                                reservoir.reservoirBuffer.addQueue(glucagonDose);
                                                patient.updateGlucagonCumulativeDose(glucagonDose);
                                            }
                                            else{
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                            }

                                        }
                                        else{
                                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                        }




                                    }
                                    else{
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                                    }

                                }
                                else{
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                                }




                            }


                        }




                        //If after injecting all the future glucagon doses the BSL is over the safeMinSugarLevel, then DON'T inject, just warn
                        else if(reading2+futureDoses > safeMinSugarLevel){
                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under Min, but glucagon injections will take effect! " + "\n");
                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under Min, but glucagon injections will take effect! " + "\n");
                        }

                    }




                    //-- Decreasing and accelerating, then inject
                    else if((reading1 - reading2) >= (reading0 - reading1)){
                        //System.out.println("Increasing and accelerating, then inject...");

                        //If even after injecting all the future Glucagon doses the BSL is under the safeMinSugarLevel, then inject
                        if(reading2+futureDoses <= safeMinSugarLevel){



                            double tempcompDose1 =  ((safeMinSugarLevel - reading2 + futureDoses ) / patient.correction_ratio);
                            double tempcompDose2 =  tempcompDose1  / maxSingleDoseGlucagon;
                            //compDose = round(tempcompDose2);
                            compDose = tempcompDose2;





                            //System.out.println("reading2: "+reading2+ " -safeMaxSugarLevel: "+safeMaxSugarLevel+ " tempcompDose1: "+tempcompDose1+ " tempcompDose2: "+tempcompDose2+
                            //      " compDose: "+compDose);





                            //Inject between minimum dose and max dose
                            if( (compDose>0 && compDose<1) && tempcompDose1>minSingleDoseGlucagon){
                                //if(compDose==0){ //round


                                glucagonDose = tempcompDose1;





                                if(gluCumulativeDose + glucagonDose <= gluMaxDailyDose){
                                    if(remainingGlucagon >= glucagonDose){

                                        if (auto){
                                            if(needle){
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under the safe levels! Decrease rate accelerating. Injects Glucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under the safe levels! Decrease rate accelerating. Injects Glucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                                createGlucagonThread(glucagonDose,buffer,patient,pendingDoses);

                                                //Update cumulative Dose, and remainingGlucagon
                                                reservoir.reservoirBuffer.addQueue(glucagonDose);
                                                patient.updateGlucagonCumulativeDose(glucagonDose);
                                            }
                                            else{
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                            }

                                        }
                                        else{
                                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                        }





                                    }
                                    else{
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                                    }

                                }
                                else{
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                                }


                            }



                            //Inject minimum dose
                            //if(compDose==0){ //round
                            else if( (compDose>0 && compDose<1) && tempcompDose1<=minSingleDoseGlucagon){

                                glucagonDose = minSingleDoseGlucagon;




                                if(gluCumulativeDose + glucagonDose <= gluMaxDailyDose){
                                    if(remainingGlucagon >= glucagonDose){

                                        if (auto){
                                            if(needle){
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under the safe levels! Decrease rate accelerating. Injects MinGlucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under the safe levels! Decrease rate accelerating. Injects MinGlucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                                createGlucagonThread(glucagonDose,buffer,patient,pendingDoses);

                                                //Update cumulative Dose, and remainingGlucagon
                                                reservoir.reservoirBuffer.addQueue(glucagonDose);
                                                patient.updateGlucagonCumulativeDose(glucagonDose);
                                            }
                                            else{
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                            }

                                        }
                                        else{
                                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                        }




                                    }
                                    else{
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                                    }

                                }
                                else{
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                                }





                            }




                            //Inject max dose
                            //if(compDose>0){ //round
                            else if(compDose>=1){


                                glucagonDose = maxSingleDoseGlucagon;
                                Round.round(glucagonDose,3);





                                if(gluCumulativeDose + glucagonDose <= gluMaxDailyDose){
                                    if(remainingGlucagon >= glucagonDose){

                                        if (auto){
                                            if(needle){
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: "+gLevel+" under the safe levels! Decrease rate accelerating. Injects MaxGlucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: "+gLevel+" under the safe levels! Decrease rate accelerating. Injects MaxGlucagon: " + Round.round(glucagonDose,3) + " (IU)\n");
                                                createGlucagonThread(glucagonDose,buffer,patient,pendingDoses);

                                                //Update cumulative Dose, and remainingGlucagon
                                                reservoir.reservoirBuffer.addQueue(glucagonDose);
                                                patient.updateGlucagonCumulativeDose(glucagonDose);
                                            }
                                            else{
                                                textArea.append(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                                statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No needle. BSL: " + gLevel + " under the safe levels!" + "\n");
                                            }

                                        }
                                        else{
                                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Auto Off. BSL: " + gLevel + " under the safe levels!" + "\n");
                                        }





                                    }
                                    else{
                                        textArea.append(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                                        statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Not enough glucagon in the reservoir to inject!" + "\n");
                                    }

                                }
                                else{
                                    textArea.append(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                                    statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Daily glucagon dose exceeded!" + "\n");
                                }





                            }






                        }



                        //If after injecting all the future glucagon doses the BSL is over the safeMinSugarLevel, then DON'T inject, just warn
                        else if(reading2+futureDoses > safeMinSugarLevel){
                            textArea.append(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under Min, but glucagon injections will take effect! " + "\n");
                            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: BSL: " + gLevel + " under Min, but glucagon injections will take effect! " + "\n");
                        }

                    }

                }













            }





////////////////////////////////////






        }//else battery




    }

    private void lowLevelsReservoirAlerts() {
        //Low levels in reservoir alerts
        int remainingPercentGlucagon = reservoir.getRemainingPercentGlucagon();
        int remainingPercentInsulin = reservoir.getRemainingPercentInsulin();

        if(remainingPercentInsulin <= 20 && remainingPercentInsulin != 0){
            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Low Insulin in the Reservoir: " +remainingPercentInsulin+ "% \n");
            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Low Insulin in the Reservoir: " +remainingPercentInsulin+ "% \n");
        }
        if(remainingPercentInsulin == 0){
            textArea.append(TimeStamp.getTimeStamp()+" - WAR: No Insulin in the Reservoir \n");
            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No Insulin in the Reservoir \n");

        }
        if(remainingPercentGlucagon <= 20 && remainingPercentGlucagon != 0){
            textArea.append(TimeStamp.getTimeStamp()+" - WAR: Low Glucagon in the Reservoir: " +remainingPercentGlucagon+ "% \n");
            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: Low Glucagon in the Reservoir: " +remainingPercentGlucagon+ "% \n");
        }
        if(remainingPercentGlucagon == 0){
            textArea.append(TimeStamp.getTimeStamp()+" - WAR: No Glucagon in the Reservoir \n");
            statusLabel.setText(TimeStamp.getTimeStamp()+" - WAR: No Glucagon in the Reservoir \n");

        }
    }


    private double checkFutDoses()  {

        //for every insulin thread, ask how many insulin is still remaining to inject
        double sumAllPendingDoses = 0;


        try {
            sumAllPendingDoses = pendingDoses.getAllPendingDoses();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return sumAllPendingDoses;
    }

}
