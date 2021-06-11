package app.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {

    public static String getDateString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return dateFormat.format(new Date());
    }
}
