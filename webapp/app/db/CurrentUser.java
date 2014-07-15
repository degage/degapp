package db;

import be.ugent.degage.db.models.User;
import play.mvc.Controller;

/**
 * Manages information about the current user, as stored in the HTTP session
 */
public class CurrentUser {

    /**
     * Register the given user as the current user. (As part of logging in or switching to another user.)
     * @param user partially filled in user object
     */
    public static void set (User user) {
        // TODO: add roles
        Controller.session("id", Integer.toString(user.getId()));
        Controller.session("email", user.getEmail());
        Controller.session("fullName", user.getFullName());
    }

    /**
     * Clear the current user. (As part of logging out.)
     */
    public static void clear () {
        Controller.session().clear();
    }

    /**
     * Retrieve the id of the current user, or null if there is no current user
     */
    public static Integer getId () {
        String idString = Controller.session("id");
        return idString == null ? null : Integer.parseInt(idString);
    }

    /**
     * Retrieve the full name of the current user
     */
    public static String getFullName () {
        return Controller.session("fullName");
    }

}
