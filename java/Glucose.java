import java.util.Random;

public class Glucose implements Runnable {
    volatile boolean alive;
    private long creationTime;
    private Buffer buffer;
    private double dose;

    private long currentTime;


    private Patient patient;

    //for random sleep
    Random r = new Random();
    int Low = 4500;
    int High = 5500; // changed for time from 4,5 sec to 5,5 sec
    int random_sleep;

    public Glucose(Buffer buffer, long creationTime, double dose, Patient patient) {
        this.alive = true;
        this.buffer = buffer;
        this.creationTime = creationTime;
        this.dose = dose;
        this.patient = patient;
    }


    @Override
    public void run() {
        while(alive){

            random_sleep = r.nextInt(High-Low) + Low;

            //Pause for N milliseconds
            try {
                Thread.currentThread().sleep(random_sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }



            double doseToAdd = calculateDose();

            if(doseToAdd > 0.01){

                addDoseToBuffer(doseToAdd);
            }
            else{
                //kill the thread
                alive = false;
            }


        }
    }



    private double calculateDose() {
        double effectDose = 0;

        currentTime = System.nanoTime() - creationTime;
        double currentTimeSec = currentTime/1000000000;

        // dose is negative, but we make it possitive for calculations
        double myDose = dose;


        //--- We need to put the LOG function here ---->
        //effectDose = myDose - (currentTime/1000000000);

        double log_xk = Math.log(currentTimeSec * patient.k_koeff);
        double log_xk_square = Math.pow(log_xk,2)*-1;
        double sigma_square = Math.pow(patient.sigma,2)*2;
        double log_div_sigma = log_xk_square/sigma_square;
        double e_power = Math.exp(log_div_sigma);
        double pi_sqrt = (Math.sqrt(2*Math.PI)*patient.sigma)*2*patient.t_koeff;
        double pi_sqrt_x = pi_sqrt*currentTimeSec;
        double s_factor = e_power/(pi_sqrt_x * 18 * 3.1);

        effectDose = myDose * s_factor;
        //System.out.println("currentTimeSec: "+currentTimeSec+" effectDose: "+effectDose);


        //double seconds = (double)currentTime / 1000000000.0;
        //effectDose = dose - (seconds/2);


        return Round.round(effectDose,3);
    }

    private void addDoseToBuffer(double doseToAdd) {
        String threadName = "Glucose-"+Thread.currentThread().getName();
        buffer.addQueue(doseToAdd,threadName);
    }
}
