package db;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;

/**
 *
 */
public final class Utils {

    public static final int getSchedulerInterval() {
        // put here because getContext is package private to reduce chances of misuse
        try (DataAccessContext context = DataAccess.getContext()) {
            return Integer.parseInt(context.getSettingDAO().getSettingForNow("scheduler_interval")); // refresh rate in seconds
        } catch (DataAccessException ex) {
            return 300;
        }
    }
}