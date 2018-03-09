import javax.swing.*;

//Consumer
/*
To simulate the Blood changing I am using a Producer Consumer approach to synchronize the changes in the glucose levels.
To do that the Producers (Pump and Feed) put insulin or glucagon into a queue and then the Consumer (Blood) read those values and change the levels.
It is not possible to change the value of the glucose level by 2 threads at the same time, because this will cause inconsistency,
that's why the Consumer takes the queue values sequentially.
 */

public class Blood implements Runnable {


    private JTextArea textArea1;
    private JTextArea textArea2;
    private GlucoseLevel glucoseLevel;
    private Buffer buffer;


    //vars to get data from the buffer
    private double insuline_glucose = 0;
    private String prodID = null;



    public Blood(GlucoseLevel glucoseLevel, Buffer buffer, JTextArea textArea1, JTextArea textArea2) {
        this.textArea1 = textArea1;
        this.textArea2 = textArea2;
        this.glucoseLevel = glucoseLevel;
        this.buffer = buffer;



    }


    @Override
    public void run() {


        while (true) {


            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();


            }



            // Get the element from the buffer of requests
             consumeDose(buffer);


            //add new blood sugar level value
            updateBloodSugarLevel(glucoseLevel);






        }//while(true)




    }//run

    private void updateBloodSugarLevel(GlucoseLevel glucoseLevel) {
        double newLevel=0;
        double oldLevel=0;


        try {
            oldLevel = glucoseLevel.getLastCurrentLevel();
            newLevel =  oldLevel + insuline_glucose;

            glucoseLevel.addCurrentLevel(newLevel);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Blood consumes, ProdID: "+prodID+ " Level: "+Round.round(oldLevel,3) + " + " +insuline_glucose+" = "+ Round.round(newLevel,3));


    }

    private void consumeDose(Buffer buffer) {

        try {
            insuline_glucose = buffer.removeDoseQueue();
            prodID = buffer.removeProdQueue();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}
