package schedulers;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

/**
 * Created by Cedric on 5/3/2014.
 */
public class Util {
    public static DateTime firstDayOfNextMonth() {
        MutableDateTime mdt = new MutableDateTime();
        mdt.addMonths(1);
        mdt.setDayOfMonth(1);
        mdt.setMillisOfDay(0); // if you want to make sure you're at midnight
        return mdt.toDateTime();
    }

}
