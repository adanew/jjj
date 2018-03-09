import javax.swing.*;


//Producer
public class Physician implements Runnable {
    private double insulin;
    private double glucagon;
    private JTextArea textArea;
    private Buffer buffer;
    private Double dose;

    private Patient patient;
    private PendingDoses pendingDoses;

    private Reservoir reservoir;
    private JCheckBox needleStatus;
    private Boolean needle;

    private String setter;
    private JLabel statusLabel;

    public Physician(Buffer buffer, Patient patient, JTextArea textArea, PendingDoses pendingDoses,
                     Reservoir reservoir, JCheckBox needleStatus, JLabel statusLabel) {

        this.textArea = textArea;
        this.buffer = buffer;
        this.insulin = 0;
        this.glucagon = 0;
        this.patient = patient;
        this.pendingDoses = pendingDoses;
        this.reservoir = reservoir;
        this.needleStatus = needleStatus;
        this.needle = true;
        this.statusLabel = statusLabel;



    }


    public void setInsulin(double insulin){
        this.insulin = insulin;
    }

    public void setGlucagon(double glucagon, String setter){
        this.glucagon = glucagon;
        this.setter = setter;
    }




    @Override
    public void run() {


        while (true) {

            //we need this sleep time to allow the thread to get the values from the GUI
            try {
                Thread.currentThread().sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();


            }


            //Check if we have needle
            needle = needleStatus.isSelected();



            if(setter == "Feed" && glucagon > 0){
                dose = glucagon;

                createGlucagonThread(dose,buffer,patient,pendingDoses);

            }




            if(setter != "Feed"){

                if(needle){


                    if(setter == "Physician" && glucagon > 0){
                        //System.out.println("DOC: I'm going to inject glucagon... ");
                        dose = glucagon;


                        double gluCumulativeDose = patient.getGluCumulativeDose();
                        double gluMaxDailyDose = patient.getGluMaxDailyDose();
                        double remainingGlucagon = reservoir.getRemainingGlucagon();


                        if(gluCumulativeDose + dose <= gluMaxDailyDose){
                            if(remainingGlucagon >= dose){


                                createGlucagonThread(dose,buffer,patient,pendingDoses);


                                //Update cumulative Dose, and remainingGlucagon
                                reservoir.reservoirBuffer.addQueue(dose);;
                                patient.updateGlucagonCumulativeDose(dose);

                            }
                            else{
                                textArea.append(TimeStamp.getTimeStamp()+" -WAR: No more glucagon in the pump reservoir!" + "\n");
                                statusLabel.setText(TimeStamp.getTimeStamp()+" -WAR: No more glucagon in the pump reservoir!" + "\n");
                            }
                        }
                        else{
                            textArea.append(TimeStamp.getTimeStamp()+" -WAR: Daily glucagon dose exceeded!" + "\n");
                            statusLabel.setText(TimeStamp.getTimeStamp()+" -WAR: Daily glucagon dose exceeded!" + "\n");
                        }






                    }



                    //Inject Insulin
                    if(insulin > 0){
                        //System.out.println("DOC: I'm going to inject insulin... ");
                        dose = insulin *-1;

                        double insCumulativeDose = patient.getInsCumulativeDose();
                        double insMaxDailyDose = patient.getInsMaxDailyDose();
                        double remainingInsulin = reservoir.getRemainingInsulin();


                        if(insCumulativeDose + dose <= insMaxDailyDose){
                            if(remainingInsulin >= dose){


                                //Create thread to inject insulin
                                createInsulinThread(dose,buffer,patient,pendingDoses);


                                //Update cumulative Dose, and remainingGlucagon
                                reservoir.reservoirBuffer.addQueue(dose);;
                                patient.updateGlucagonCumulativeDose(dose);

                            }
                            else{
                                textArea.append(TimeStamp.getTimeStamp()+" -WAR: No more insulin in the pump reservoir!" + "\n");
                                statusLabel.setText(TimeStamp.getTimeStamp()+" -WAR: No more insulin in the pump reservoir!" + "\n");
                            }
                        }
                        else{
                            textArea.append(TimeStamp.getTimeStamp()+" -WAR: Daily insulin dose exceeded!" + "\n");
                            statusLabel.setText(TimeStamp.getTimeStamp()+" -WAR: Daily insulin dose exceeded!" + "\n");
                        }




                    }



                }
                else{
                    textArea.append(TimeStamp.getTimeStamp()+" -WAR: No needle!" + "\n");
                    statusLabel.setText(TimeStamp.getTimeStamp()+" -WAR: No needle!" + "\n");
                }


            }












        }








    }//run




    private void createInsulinThread(double dose, Buffer buffer,Patient patient, PendingDoses pendingDoses) {

        textArea.append(TimeStamp.getTimeStamp()+" - INF: Physician injects Insulin: " + insulin + " (IU)\n");
        statusLabel.setText(TimeStamp.getTimeStamp()+" - INF: Physician injects Insulin: " + insulin + " (IU)\n");

        //The creation time of the thread will be the current time minus the startSimulationTime
        long creationTime = System.nanoTime();

        // Initialize insulin object
        Insulin insulin = new Insulin(buffer,creationTime,dose,patient,pendingDoses);

        // Create thread with object
        Thread insulinThread = new Thread(insulin);

        // Change the thread name to identify it later
        String threadName = "Insulin-"+insulinThread.getName();
        insulinThread.setName(threadName);

        //Start thread
        insulinThread.start();

        // Set the insulin to inject by the physician to 0
        this.insulin = 0;


    }
    private void createGlucagonThread(double dose, Buffer buffer,Patient patient, PendingDoses pendingDoses) {
        if(setter == "Feed"){
            textArea.append(TimeStamp.getTimeStamp()+" - INF: Feed add Glucose: " + glucagon + " (IU)\n");
            statusLabel.setText(TimeStamp.getTimeStamp()+" - INF: Feed add Glucose: " + glucagon + " (IU)\n");
        }
        else if (setter == "Physician"){
            textArea.append(TimeStamp.getTimeStamp()+" - INF: Physician injects Glucagon: " + glucagon + " (IU)\n");
            statusLabel.setText(TimeStamp.getTimeStamp()+" - INF: Physician injects Glucagon: " + glucagon + " (IU)\n");
        }


        //The creation time of the thread will be the current time minus the startSimulationTime
        //long creationTime = (System.nanoTime() - startSimulationTime);
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

        this.glucagon = 0;

    }

}
