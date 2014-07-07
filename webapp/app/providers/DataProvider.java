package providers;

import be.ugent.degage.db.DataAccessProvider;
import db.DataAccess;

/**
 * Created by Cedric on 2/16/14.
 */
public class DataProvider {

    //TODO: this class needs a decent implementation or alternative
    private static DataAccessProvider accessProvider;
    private static UserProvider userProvider;
    private static UserRoleProvider userRoleProvider;
    private static CommunicationProvider communicationProvider;
    private static SettingProvider settingProvider;

    public static UserProvider getUserProvider() {
        if (userProvider == null) {
            userProvider = new UserProvider(getDataAccessProvider());
        }
        return userProvider;
    }

    public static SettingProvider getSettingProvider(){
        if(settingProvider == null){
            settingProvider = new SettingProvider(getDataAccessProvider());
        }
        return settingProvider;
    }

    public static UserRoleProvider getUserRoleProvider() {
        if (userRoleProvider == null) {
            userRoleProvider = new UserRoleProvider(getDataAccessProvider(), getUserProvider());
        }
        return userRoleProvider;
    }

    public static CommunicationProvider getCommunicationProvider() {
        if (communicationProvider == null) {
            communicationProvider = new CommunicationProvider(getDataAccessProvider(), getUserProvider());
        }
        return communicationProvider;
    }

    @Deprecated
    public static DataAccessProvider getDataAccessProvider() {
        return DataAccess.getProvider();
    }
}
