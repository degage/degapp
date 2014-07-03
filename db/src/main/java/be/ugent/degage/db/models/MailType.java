package be.ugent.degage.db.models;

/**
 * Created by stefaan on 04/03/14.
 */
public enum MailType {
    VERIFICATION(1), WELCOME(2), INFOSESSION_ENROLLED(3), RESERVATION_APPROVE_REQUEST(4),
    RESERVATION_APPROVED_BY_OWNER(5), RESERVATION_REFUSED_BY_OWNER(6), PASSWORD_RESET(7),
    TERMS(8), MEMBERSHIP_APPROVED(9), MEMBERSHIP_REFUSED(10), CARCOST_APPROVED(11),
    CARCOST_REFUSED(12), REFUEL_APPROVED(13), REFUEL_REFUSED(14), REMINDER_MAIL(15),
    REFUEL_REQUEST(16), CARCOST_REQUEST(17), CONTRACTMANAGER_ASSIGNED(18), DETAILS_PROVIDED(19);
    private final int key;

    private MailType(final int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
