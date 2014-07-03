package controllers.util;

import play.Configuration;
import play.api.Play;

/**
 * Created by Cedric on 4/11/2014.
 */
public class ConfigurationHelper {

    private final static Configuration CONFIG;
    static {
        CONFIG = new Configuration(Play.current().configuration()); // Wraps around the Scala version, which is almost unusable in Java
    }

    public static String getConfigurationString(String name){
        return CONFIG.getString(name);
    }
}
