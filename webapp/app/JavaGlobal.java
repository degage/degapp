import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.TemplateDAO;
import be.ugent.degage.db.models.MailType;
import db.DataAccess;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.data.format.Formatters;
import play.db.DB;
import schedulers.Scheduler;
import data.EurocentAmount;

import java.text.ParseException;
import java.util.Locale;

/**
 * Global settings. Called from Scala initializer class Global.scala
 */
public class JavaGlobal {

    // Tests if all templates are in the database, and if the database works
    private static void testDatabase() {
        try (DataAccessContext context = DataAccess.getContext()) {
            TemplateDAO dao = context.getTemplateDAO();
            StringBuilder sb = new StringBuilder();
            for (MailType type : MailType.values()) {
                if (dao.getTemplate(type) == null) {
                    sb.append(type).append(", ");
                }
            }
            if (sb.length() > 0)
                throw new RuntimeException("Missing database templates for: " + sb.toString());
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    private static void registerFormatters() {
        Formatters.register(DateTime.class, new Formatters.SimpleFormatter<DateTime>() {
            private final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm"); //ISO time without miliseconds

            @Override
            public DateTime parse(String s, Locale locale) throws ParseException {
                return DATETIME_FORMATTER.parseDateTime(s);
            }

            @Override
            public String print(DateTime dateTime, Locale locale) {
                return dateTime.toString(DATETIME_FORMATTER);
            }
        });

        Formatters.register(EurocentAmount.class, new Formatters.SimpleFormatter<EurocentAmount>() {
            @Override
            public EurocentAmount parse(String s, Locale locale) throws ParseException {
                return EurocentAmount.parse(s);
            }

            @Override
            public String print(EurocentAmount eurocentAmount, Locale locale) {
                return eurocentAmount.toString();
            }
        });
    }

    private static void onStop() {
        Scheduler.stop();
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
        DataAccess.setProviderFromDataSource(DB.getDataSource(dataSourceName));
        testDatabase();
        registerFormatters();
        Scheduler.start();
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
        DataAccess.setProviderForTesting();
        registerFormatters();
        Scheduler.start();
    }
}
