package controllers.Security;

import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;
import controllers.routes;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.With;
import providers.DataProvider;
import providers.UserProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

/**
 * Class providing an annotation to secure methods or types.
 * A method or type secured using this annotation will restrict the entrance only to users
 * having the required user role(s) to call this method or type.
 * <p>
 * Created by Benjamin on 26/02/14.
 */
public class RoleSecured {

        /**
         * Creation of the annotation interface.
         * Use of the RoleAuthorization class for action composition.
         */
        @With(RoleAuthorizationAction.class)
        @Target({ElementType.TYPE, ElementType.METHOD})
        @Retention(RetentionPolicy.RUNTIME)
        public @interface RoleAuthenticated {
            UserRole[] value() default {UserRole.USER};
        }

        /**
         * Action allowing authentication of a user by role.
         * The username is retrieved from the session cookie and used to
         * determine the user role.
         */
        public static class RoleAuthorizationAction extends Action<RoleAuthenticated> {

            /**
             * Delegates the user to the given HTTP context if the user is authorized.
             * The authorized roles are retrieved from the RoleAuthenticated annotation.
             * @param ctx The given HTTP context
             * @return The result, either the requested page or an unauthorized request page
             */
            public F.Promise<Result> call(Context ctx) {
                try {
                    UserRole[] securedRoles = configuration.value();
                    User user = DataProvider.getUserProvider().getUserFromHttpSession(ctx.session()); // get user from session

                    // If user is null, redirect to login page
                    if (user == null) {
                        return F.Promise.pure(redirect(routes.Login.login(ctx.request().path())));
                    } else if (UserProvider.isBlocked(user)) {
                        ctx.flash().put("danger", "Dit account is not niet geactiveerd of geblokkeerd.");
                        return F.Promise.pure(redirect(routes.Login.login(ctx.request().path())));
                    }

                    if (securedRoles.length == 0) {
                        return delegate.call(ctx);
                    }

                    Set<UserRole> roleSet = UserRole.fromString(ctx.session().get("roles"));

                    if (roleSet != null) {
                        if (roleSet.contains(UserRole.SUPER_USER)) {
                            return delegate.call(ctx);
                        }
                        for (UserRole role : securedRoles) {
                            if (roleSet.contains(role)) {
                                return delegate.call(ctx);
                            }
                        }
                    }

                    //It this point is reached, then user is not authorized
                    return F.Promise.pure((Result) unauthorized(views.html.unauthorized.render(securedRoles)));

                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }

        }
    }
