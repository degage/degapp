/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.models;

/**
 * The various user statuses. (Database default: EMAIL_VALIDATING)
 */
public enum UserStatus {
    EMAIL_VALIDATING,
    REGISTERED,
    FULL_VALIDATING,
    FULL,
    BLOCKED,
    DROPPED,
    INACTIVE
}
