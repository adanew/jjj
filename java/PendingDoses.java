import java.util.Enumeration;
import java.util.Hashtable;

public class PendingDoses {

    Hashtable<String, Double> pendingDoses;

    public PendingDoses() {
        this.pendingDoses = new Hashtable<String, Double>();
    }



    public void addPendingDose (String threadName, double pendingDose) {

        //Producer
        synchronized (pendingDoses) {

            pendingDoses.put(threadName,pendingDose);

            System.out.println("addPendingDose: "+threadName +" pendingDose: "+ pendingDose);



            // The thread must own the monitor on the queue to call notify
            pendingDoses.notifyAll();
        }

    }


    public double getAllPendingDoses() throws InterruptedException{
        double sumPendingDoses = 0;
        Enumeration names;
        String key;
        double value;

        /*
        //wait if queue is empty
        while (pendingDoses.isEmpty()) {
            synchronized (pendingDoses) {


                    pendingDoses.wait();

            }

        }*/



        if(pendingDoses.isEmpty()){
            synchronized (pendingDoses) {
                return 0;
            }
        }







        //We will sum all the pending doses of Insulin and Glucagon

        synchronized (pendingDoses) {

            names = pendingDoses.keys();
            while(names.hasMoreElements()) {
                key = (String) names.nextElement();
                value = pendingDoses.get(key);

                //System.out.println("Key: " +key+ " Value: " + value);

                sumPendingDoses = sumPendingDoses + value;

            }
            System.out.println("getAllPendingDoses sumPendingDoses: " +sumPendingDoses);


            pendingDoses.notifyAll();
            return sumPendingDoses;
        }



    }
}
