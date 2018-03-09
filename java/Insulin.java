import java.util.Random;

public class Insulin implements Runnable {
//public class Insulin extends Thread {
    volatile boolean alive;
    private long creationTime;
    private Buffer buffer;
    public double dose;
    private long currentTime;
    private Patient patient;
    private PendingDoses pendingDoses;

    //for random sleep
    Random r = new Random();
    int Low = 4250;
    int High = 5250;
    int random_sleep;


    public Insulin(Buffer buffer, long creationTime, double dose, Patient patient, PendingDoses pendingDoses) {
        this.alive = true;
        this.buffer = buffer;
        this.creationTime = creationTime;
        this.dose = dose;
        this.patient = patient;
        this.pendingDoses = pendingDoses;
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
            double sumPendingDoses = calculateSumPendingDoses();


            // Add or Update remaining doses in the hash table, this hash table will be read by the pump to calculate if more injections are necessary
            if(sumPendingDoses >= 0.01){
                addPendingDosesToHash(sumPendingDoses * -1);
            }
            else{
                addPendingDosesToHash(0);
            }



            // Add the dose to be taken by the Blood thread
            if(doseToAdd >= 0.01){

                // We add the insulin to the buffer with negative sign, the glucagon is positive
                addDoseToBuffer(doseToAdd * -1);
            }
            else{
                //kill the thread
                alive = false;
            }









        }
    }

    private void addPendingDosesToHash(double sumPendingDoses) {
        String threadName = Thread.currentThread().getName();

        this.pendingDoses.addPendingDose(threadName,sumPendingDoses);


    }

    private double calculateSumPendingDoses() {
        double sumEffectDoses = 0;
        double effectDose = 0.01;

        currentTime =  System.nanoTime() - creationTime; //why? it give 30 second for first listening..
        double currentTimeSec = currentTime/1000000000;

        // dose is negative, but we make it possitive for calculations
        double myDose = dose*-1;



        while(effectDose >= 0.01){
            //--- We need to put the LOG function here ---->
            //effectDose = myDose - (currentTime/1000000000);

            // we increase the time to calculate future doses, with 6 seconds of difference
            currentTimeSec = currentTimeSec + 5; 

            double log_xk = Math.log(currentTimeSec * patient.k_koeff);
            double log_xk_square = Math.pow(log_xk,2)*-1;
            double sigma_square = Math.pow(patient.sigma,2)*2;
            double log_div_sigma = log_xk_square/sigma_square;
            double e_power = Math.exp(log_div_sigma);
            double pi_sqrt = (Math.sqrt(2*Math.PI)*patient.sigma)*2*patient.t_koeff;
            double pi_sqrt_x = pi_sqrt*currentTimeSec;
            double s_factor = e_power/(pi_sqrt_x * 18 * 2.1);

            effectDose = myDose * s_factor;
            //System.out.println("effectDose: "+effectDose);

            // we sum all the pending doses
            sumEffectDoses = sumEffectDoses + effectDose;



        }

        //System.out.println("sumEffectDoses: "+sumEffectDoses);


        return Round.round(sumEffectDoses,3);
    }


    private double calculateDose() {
        double effectDose = 0;

        currentTime = System.nanoTime() - creationTime;
        double currentTimeSec = currentTime/1000000000;

        // dose is negative, but we make it possitive for calculations
        double myDose = dose*-1;


        //--- We need to put the LOG function here ---->
        //effectDose = myDose - (currentTime/1000000000);

        double log_xk = Math.log(currentTimeSec * patient.k_koeff);
        double log_xk_square = Math.pow(log_xk,2)*-1;
        double sigma_square = Math.pow(patient.sigma,2)*2;
        double log_div_sigma = log_xk_square/sigma_square;
        double e_power = Math.exp(log_div_sigma);
        double pi_sqrt = (Math.sqrt(2*Math.PI)*patient.sigma)*2*patient.t_koeff;
        double pi_sqrt_x = pi_sqrt*currentTimeSec;
        double s_factor = e_power/(pi_sqrt_x * 18 * 2.2);

        effectDose = myDose * s_factor;
        //System.out.println("effectDose: "+effectDose);


        return Round.round(effectDose,3);
    }

    private void addDoseToBuffer(double doseToAdd) {
        String threadName = Thread.currentThread().getName();

        buffer.addQueue(doseToAdd,threadName);
    }

}
