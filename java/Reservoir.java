import javax.swing.*;

public class Reservoir implements Runnable{
    private double insulinCapacity = 100;
    private double glucagonCapacity = 100;

    private double remainingInsulin;
    private double remainingGlucagon;
    public ReservoirBuffer reservoirBuffer;

    private JProgressBar insulinReservoirField;
    private JProgressBar glucagonReservoirField;



    public Reservoir(ReservoirBuffer reservoirBuffer,JProgressBar insulinReservoirField, JProgressBar glucagonReservoirField){
        this.remainingGlucagon = glucagonCapacity;
        this.remainingInsulin = insulinCapacity;
        this.reservoirBuffer = reservoirBuffer;
        this.insulinReservoirField = insulinReservoirField;
        this.glucagonReservoirField = glucagonReservoirField;

    }

    public void updateInsulin(double insulinDose){
        //We expect the insulinDose to be negative so we multiply by -1
        insulinDose = insulinDose *-1;

        remainingInsulin = remainingInsulin - insulinDose;

        double percentage = (remainingInsulin/insulinCapacity) *100;
        percentage = Round.round(percentage,1);

        //insulinReservoirField.setText(Double.toString(percentage)+"%");
        insulinReservoirField.setValue((int)percentage);
    }


    public void updateGlucagon(double glucagonDose){

        remainingGlucagon = remainingGlucagon - glucagonDose;

        double percentage = (remainingGlucagon/glucagonCapacity) *100;
        percentage = Round.round(percentage,1);

        //glucagonReservoirField.setText(Double.toString(percentage)+"%");
        glucagonReservoirField.setValue((int)percentage);
    }


    public double getRemainingInsulin() {
        return remainingInsulin;
    }



    public double getRemainingGlucagon() {
        return remainingGlucagon;
    }


    public int getRemainingPercentInsulin() {

        int remaining_percent =  (int)((remainingInsulin*100)/insulinCapacity);

        return remaining_percent;
    }

    public int getRemainingPercentGlucagon() {

        int remaining_percent =  (int)((remainingGlucagon*100)/glucagonCapacity);
        //System.out.println("remaining_percent: "+remaining_percent);

        return remaining_percent;
    }


    public void setRemainingInsulin(double remainingInsulin) {
        this.remainingInsulin = remainingInsulin;

        //update slider in pump app
        int new_value = (int)remainingInsulin;
        insulinReservoirField.setValue(new_value);
    }

    public void setRemainingGlucagon(double remainingGlucagon) {
        this.remainingGlucagon = remainingGlucagon;

        //update slider in pump app
        int new_value = (int)remainingGlucagon;
        glucagonReservoirField.setValue(new_value);
    }



    @Override
    public void run() {
        while (true) {


            try {
                Thread.currentThread().sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();


            }


            double insuline_glucose = 0;
            try {
                insuline_glucose = reservoirBuffer.removeDoseQueue();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }



            if(insuline_glucose > 0){
                updateGlucagon(insuline_glucose);


            }

            if(insuline_glucose < 0){
                updateInsulin(insuline_glucose);


            }






        }//while(true)
    }
}
