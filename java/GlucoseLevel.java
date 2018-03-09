import java.util.ArrayList;
import java.util.List;

public class GlucoseLevel {


    private List<Double> currentLevelList;

    private Patient patient;


    public GlucoseLevel(Patient patient) {

        this.currentLevelList = new ArrayList<Double>();


        this.patient = patient;
        this.currentLevelList.add(this.patient.safeMidSugarLevel);


       // this.currentLevelList.add(19.00);




    }

    public double getLastCurrentLevel() throws InterruptedException{
        //wait if queue is empty
        while (currentLevelList.isEmpty()) {
            synchronized (currentLevelList) {
                System.out.println("GlucoseLevel Queue is empty " + Thread.currentThread().getName()
                        + " is waiting , size: " + currentLevelList.size());



                currentLevelList.wait();
            }

        }


        synchronized (currentLevelList){

            currentLevelList.notifyAll();
            return currentLevelList.get(currentLevelList.size()-1);
        }

    }


    public double getCurrentLevel(int index) throws InterruptedException{
        //wait if queue is empty
        while (currentLevelList.isEmpty()) {
            synchronized (currentLevelList) {
                System.out.println("GlucoseLevel Queue is empty " + Thread.currentThread().getName()
                        + " is waiting , size: " + currentLevelList.size());



                currentLevelList.wait();
            }

        }


        synchronized (currentLevelList){

            currentLevelList.notifyAll();
            return currentLevelList.get(index);
        }

    }



    public void addCurrentLevel(double currentLevel) throws InterruptedException{

        synchronized (currentLevelList){

            /*
            //remove the older elements to have only the latest 10
            if(currentLevelList.size()>10){
                int indexToRemove = currentLevelList.size() - 11;

                for(int i=indexToRemove; i>=0; i--){
                    currentLevelList.remove(i);

                }

            }
            */

            currentLevelList.add(currentLevel);
            currentLevelList.notifyAll();

        }
    }

    public double removeLastCurrentLevel() throws InterruptedException{
        //wait if queue is empty
        while (currentLevelList.isEmpty()) {
            synchronized (currentLevelList) {
                System.out.println("Queue is empty " + Thread.currentThread().getName()
                        + " is waiting , size: " + currentLevelList.size());



                currentLevelList.wait();
            }

        }

        synchronized (currentLevelList){


            currentLevelList.notifyAll();
            return currentLevelList.remove(currentLevelList.size()-1);

        }

    }


    public int getSize(){

        return currentLevelList.size();
    }

}
