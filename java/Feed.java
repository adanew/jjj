import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

//Producer
public class Feed implements Runnable{
    private Buffer buffer;
    private double glucose;
    private Random rand = new Random();
    private JTextArea textArea;

    private List<Double> randoms = new ArrayList<Double>();

    private Patient patient;



    public Feed(Buffer buffer, Patient patient, JTextArea textArea) {

        this.textArea = textArea;
        this.buffer = buffer;
        this.glucose = 0;

        //generateRandomNums();

        this.patient = patient;

    }

    private void generateRandomNums(){

        Random r = new Random();
        int gauss_index = 1;

        //r.nextGaussian() * deviation + mean
        double first_value = r.nextGaussian() * 1 + 0;
        first_value = Round.round(first_value,3);

        randoms.add(first_value);
        //System.out.println("Feed values:");
        System.out.println(first_value);

        while(gauss_index < 100) {
            double past_value = randoms.get(gauss_index-1);
            double new_value = Math.abs(r.nextGaussian() * 1 + ((past_value/2)+7));
            //double new_value = Math.abs(r.nextGaussian() * 1 + ((past_value)+2));
            new_value = Round.round(new_value,3);

            randoms.add(new_value);

            System.out.println(new_value);
            gauss_index++;

        }

    }

    public void addGlucose(){
        //Producer

            //10 is the maximum and the 1 is our minimum
            glucose = rand.nextInt(4) + 1;



            //Gaussian numbers
            //glucose = randoms.remove(randoms.size()-1);

            createGlucoseThread(glucose,buffer);






    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.currentThread().sleep(80000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }




            addGlucose();

        }

    }

    private void createGlucoseThread(double dose, Buffer buffer) {
        textArea.append("Feed, add Glucose: " + glucose *10+ " (gr)\n");

        //The creation time of the thread will be the current time minus the startSimulationTime
        long creationTime = System.nanoTime() ;

        // Initialize insulin object
        Glucose glucose = new Glucose(buffer,creationTime,dose,patient);

        // Create thread with object
        Thread glucoseThread = new Thread(glucose);

        //Start thread
        glucoseThread.start();


    }


}
