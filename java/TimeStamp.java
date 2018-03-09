import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class TimeStamp {




    public static String getTimeStamp() {
        java.util.Date date= new java.util.Date();
        Timestamp ts = new Timestamp(date.getTime());
        String format_date = new SimpleDateFormat("HH:mm:ss").format(ts);

        return format_date;
    }
}
