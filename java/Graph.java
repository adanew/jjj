import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class Graph {

    public TimeSeries pop;
    public TimeSeries max;
    public TimeSeries min;
    public Second current;
    public JFreeChart chart;
    public ChartPanel chartPanel;
    public TimeSeriesCollection dataset;



    public Graph(){
        current = new Second();
        max = new TimeSeries("Safe Max", Second.class);
        pop = new TimeSeries("Current BSL", Second.class);
        min = new TimeSeries("Safe Min", Second.class);


        max.addOrUpdate(current, 15);
        pop.addOrUpdate(current, 10);
        min.addOrUpdate(current, 5);

        dataset = new TimeSeriesCollection();
        dataset.addSeries(max);
        dataset.addSeries(pop);
        dataset.addSeries(min);

        chart = ChartFactory.createTimeSeriesChart(
                "",
                "Time",
                "BSL (mmol/L)",
                dataset,
                true,
                true,
                false);

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 370 ) );
        chartPanel.setMouseZoomable( true , false );

    }


    public void addReading(double reading, double pmax, double pmin) {

        max.addOrUpdate(current, pmax);
        pop.addOrUpdate(current, reading);
        min.addOrUpdate(current, pmin);
        current = (Second) current.next();

    }




}
