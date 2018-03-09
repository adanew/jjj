import java.util.List;

public class Dose {

    public List<Double> dose_queue;
    public List<String> producer_queue;


    public Dose(List<Double> dose_queue, List<String> producer_queue) {
        this.dose_queue = dose_queue;
        this.producer_queue = producer_queue;
    }
}
