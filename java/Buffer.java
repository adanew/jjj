import java.util.ArrayList;

public class Buffer {

    private Dose queue;



    public Buffer() {

        this.queue = new Dose(new ArrayList<Double>(), new ArrayList<String>());

    }


    public void addQueue(double insulin_glucagon, String prodID) {

        //Producer
        synchronized (queue) {
            queue.dose_queue.add(insulin_glucagon);
            queue.producer_queue.add(prodID);

            // The thread must own the monitor on the queue to call notify
            queue.notifyAll();
        }

    }

    public double removeDoseQueue() throws InterruptedException{


        //wait if queue is empty
        while (queue.dose_queue.isEmpty()) {
            synchronized (queue) {
                //System.out.println("Buffer Queue is empty " + Thread.currentThread().getName()
                  //      + " is waiting , size: " + queue.dose_queue.size());



                queue.wait();
            }

        }




        //Otherwise consume element and notify waiting producer
        synchronized (queue) {
            queue.notifyAll();

            //Last come first serve
            //return queue.dose_queue.remove(queue.dose_queue.size()-1);

            //First come first serve
            return queue.dose_queue.remove(0);
        }



    }




    public String removeProdQueue() throws InterruptedException{


        //wait if queue is empty
        while (queue.producer_queue.isEmpty()) {
            synchronized (queue) {
                //System.out.println("Queue is empty " + Thread.currentThread().getName()
                  //      + " is waiting , size: " + queue.producer_queue.size());



                queue.wait();
            }

        }





        //Otherwise consume element and notify waiting producer
        synchronized (queue) {
            queue.notifyAll();
            //return queue.producer_queue.remove(queue.producer_queue.size()-1);
            return queue.producer_queue.remove(0);
        }



    }






}
