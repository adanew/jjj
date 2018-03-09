public class Patient {

    //--- Pump limits
    public double minSingleDoseInsulin = 1;
    public double maxSingleDoseInsulin = 4;

    public double minSingleDoseGlucagon = 1;
    public double maxSingleDoseGlucagon = 4;



    //--- Glucose Level limits
    public double safeMinSugarLevel = 3.9;
    public double safeMaxSugarLevel = 8.3;
    public double safeMidSugarLevel;


    //--- Insulin function limits
    private double weight;
    public double asudani_koeff;

    //Daily Dose of Insulin or TDD
    private double insMaxDailyDose;

    //private double insMaxDailyDose = 25;
    private double insCumulativeDose = 0;
    private double gluMaxDailyDose = 100;
    private double gluCumulativeDose = 0;

    //Correction Ratio
    public double correction_ratio;
    public double t_koeff;
    public double k_koeff;


    public double sigma;
    public double correction_factor;



    public Patient(double weight, double asudani_koeff) {
        this.weight = weight; //75
        this.asudani_koeff = asudani_koeff; //0.4
        this.insMaxDailyDose = weight * asudani_koeff; //30
        this.correction_ratio = 100/insMaxDailyDose;
        //this.t_koeff = correction_ratio/70588;
        this.t_koeff = correction_ratio * 18 / 75000;
        this.k_koeff = correction_ratio * 18 / 1000;
        this.sigma = 0.5;
        //this.correction_factor = 1;
        this.correction_factor = 0.5;

        safeMidSugarLevel = (safeMinSugarLevel+safeMaxSugarLevel)/2;

    }


    public void setWeight(double weight) {
        this.weight = weight;
    }


    public double getWeight() {
        return weight;
    }


    public double updateInsulinCumulativeDose(double insulinDose){
        insulinDose = insulinDose *-1;

        insCumulativeDose = insCumulativeDose + insulinDose;

        return insCumulativeDose;
    }

    public double updateGlucagonCumulativeDose(double glucagonDose){


        gluCumulativeDose = gluCumulativeDose + glucagonDose;

        return gluCumulativeDose;
    }

    public double getInsCumulativeDose() {
        return insCumulativeDose;
    }

    public double getGluCumulativeDose() {
        return gluCumulativeDose;
    }


    public double getInsMaxDailyDose() {
        return insMaxDailyDose;
    }

    public double getGluMaxDailyDose() {
        return gluMaxDailyDose;
    }

}
