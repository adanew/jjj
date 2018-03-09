import java.util.ArrayList;
import java.util.List;

public class ReservoirBuffer {

    private List<Double> dosesList;


    public ReservoirBuffer(){

        this.dosesList = new ArrayList<Double>();
    }


    public void addQueue(double doseInsulinGlucagon) {

        //Producer
        synchronized (dosesList) {
            dosesList.add(doseInsulinGlucagon);


            // The thread must own the monitor on the queue to call notify
            dosesList.notifyAll();
        }

    }



    public double removeDoseQueue() throws InterruptedException{


        //wait if queue is empty
        while (dosesList.isEmpty()) {
            synchronized (dosesList) {
                //System.out.println("Buffer Queue is empty " + Thread.currentThread().getName()
                //      + " is waiting , size: " + queue.dose_queue.size());



                dosesList.wait();
            }

        }




        //Otherwise consume element and notify waiting producer
        synchronized (dosesList) {
            dosesList.notifyAll();

            //Last come first serve
            //return queue.dose_queue.remove(queue.dose_queue.size()-1);

            //First come first serve
            return dosesList.remove(0);
        }



    }



}
