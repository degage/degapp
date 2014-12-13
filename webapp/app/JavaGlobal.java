/* JavaGlobal.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see http://www.gnu.org/licenses/.
 */

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.TemplateDAO;
import be.ugent.degage.db.models.MailType;
import controllers.Utils;
import db.DataAccess;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.data.format.Formatters;
import play.db.DB;
import schedulers.Scheduler;
import data.EurocentAmount;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

        Formatters.register(Instant.class, new Formatters.SimpleFormatter<Instant>() {

            @Override
            public Instant parse(String s, Locale locale) throws ParseException {
                return Utils.toInstant(s);
            }

            @Override
            public String print(Instant instant, Locale locale) {
                // ignores locale!
                return Utils.toString(instant);
            }
        });

        Formatters.register(LocalDateTime.class, new Formatters.SimpleFormatter<LocalDateTime>() {

            @Override
            public LocalDateTime parse(String s, Locale locale) throws ParseException {
                return Utils.toLocalDateTime(s);
            }

            @Override
            public String print(LocalDateTime dateTime, Locale locale) {
                // ignores locale!
                return Utils.toString(dateTime);
            }
        });

        Formatters.register(LocalDate.class, new Formatters.SimpleFormatter<LocalDate>() {

            @Override
            public LocalDate parse(String s, Locale locale) throws ParseException {
                return Utils.toLocalDate(s);
            }

            @Override
            public String print(LocalDate localDate, Locale locale) {
                // ignores locale!
                return Utils.toDateString(localDate); // happens to be the correct format
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
