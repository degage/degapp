package controllers;

import be.ugent.degage.db.models.UserRole;
import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the associated action can only be performed by users having a certain role,
 */
@With(AllowRolesWrapper.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowRoles {
    UserRole[] value() default {UserRole.USER};
}
