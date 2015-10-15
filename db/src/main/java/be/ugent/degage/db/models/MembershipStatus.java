package be.ugent.degage.db.models;

/**
 * Status of membership (before it is approved). Once ACCEPTED only the {@link UserStatus} remains important.
 */
public enum MembershipStatus {
    PENDING,
    ACCEPTED,
    DENIED
}
