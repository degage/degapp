package be.ugent.degage.db;

import be.ugent.degage.db.models.UserRole;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link UserRole} class.
 */
public class UserRoleTest {

    @Test
    public void conversion () {
        Set<UserRole> originalSet = EnumSet.of(UserRole.CAR_ADMIN, UserRole.INFOSESSION_ADMIN, UserRole.PROFILE_ADMIN);
        String string = UserRole.toString(originalSet);
        Set<UserRole> newSet = UserRole.fromString(string);
        assertEquals(originalSet, newSet);
        String newString = UserRole.toString(newSet);
        assertEquals(string, newString);
    }

    @Test
    public void conversionSimple () {
        assertEquals ("", UserRole.toString(EnumSet.noneOf(UserRole.class)));
        assertEquals ("0", UserRole.toString(EnumSet.of(UserRole.USER)));
    }
}
