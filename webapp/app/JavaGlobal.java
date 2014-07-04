import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.DataAccessProvider;
import be.ugent.degage.db.dao.TemplateDAO;
import be.ugent.degage.db.jdbc.JDBCDataAccess;
import be.ugent.degage.db.models.MailType;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.Application;
import play.Logger;
import play.data.format.Formatters;
import play.db.DB;
import providers.DataProvider;
import scala.concurrent.duration.Duration;
import schedulers.CheckFinishedRidesJob;
import schedulers.Scheduler;
import schedulers.SendUnreadNotificationsMailScheduler;

import java.io.IOException;
import java.text.ParseException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Global settings. Called from Scala initializer class Global.scala
 */
public class JavaGlobal {

    // Tests if all templates are in the database, and if the database works
    private static void testDatabase() {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            TemplateDAO dao = context.getTemplateDAO();
            StringBuilder sb = new StringBuilder();
            for (MailType type : MailType.values()) {
                if (dao.getTemplate(type) == null)
                    sb.append(type + ", ");
            }
            if (sb.length() > 0)
                throw new RuntimeException("Missing database templates for: " + sb.toString());
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    private static void startScheduler() {
        Scheduler scheduler = Scheduler.getInstance();
        scheduler.start();
        scheduler.schedule(Duration.create(1, TimeUnit.HOURS), new SendUnreadNotificationsMailScheduler()); // send notifications when enough
        scheduler.schedule(Duration.create(1, TimeUnit.MINUTES), new CheckFinishedRidesJob()); // change ride status to finished
    }

    private static void registerDateTimeFormatter() {
        play.data.format.Formatters.register(DateTime.class, new Formatters.SimpleFormatter<DateTime>() {
            private final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"); //ISO time without miliseconds

            @Override
            public DateTime parse(String s, Locale locale) throws ParseException {
                return DATETIME_FORMATTER.parseDateTime(s);
            }

            @Override
            public String print(DateTime dateTime, Locale locale) {
                return dateTime.toString(DATETIME_FORMATTER);
            }
        });
    }

    private static void onStop() {
        Scheduler.getInstance().stop();
    }

    /**
     * Called when the application stops in development mode
     */
    public static void onStopDev() {
        onStop();
    }

    /**
     * Called when the application stops in production mode
     */
    public static void onStopProd() {
        onStop();
    }

    /**
     * Called when the application stops in test mode
     */
    public static void onStopTest() {
        onStop();
    }

    private static void onStartDevProd(String dataSourceName) {
        DataAccessProvider dap = JDBCDataAccess.createDataAccessProvider(DB.getDataSource (dataSourceName));
        DataProvider.setDataAccessProvider(dap);
        testDatabase();
        registerDateTimeFormatter();
        startScheduler();
    }

    /**
     * Called when  the application starts in development mode
     */
    public static void onStartDev() {
        onStartDevProd("dev");
    }

    /**
     * Called when  the application starts in production mode
     */
    public static void onStartProd() {
        onStartDevProd("prod");
    }

    /**
     * Called when  the application starts in development mode
     */
    public static void onStartTest() {
        DataAccessProvider dap = JDBCDataAccess.getTestDataAccessProvider();
        DataProvider.setDataAccessProvider(dap);
        registerDateTimeFormatter();
        startScheduler(); // TODO: needed in test mode?
    }
}
