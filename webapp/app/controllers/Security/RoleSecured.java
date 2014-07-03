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
import providers.UserRoleProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

/**
 * Class providing an annotation to secure methods or types.
 * A method or type secured using this annotation will restrict the entrance only to users
 * having the required user role(s) to call this method or type.
 *
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
        public F.Promise<Result> call(Context ctx) { // TODO: check whether renaming SimpleResult to Result was correct here
            try {
                UserRole[] securedRoles = configuration.value();
                User user = DataProvider.getUserProvider().getUser(ctx.session(), true); // get user from session

                // If user is null, redirect to login page
                if(user == null) {
                    return F.Promise.pure(redirect(routes.Login.login(ctx.request().path())));
                } else if(UserProvider.isBlocked(user)) {
                    ctx.flash().put("danger", "Dit account is not niet geactiveerd of geblokkeerd.");
                    return F.Promise.pure(redirect(routes.Login.login(ctx.request().path())));
                }

                Set<UserRole> roles = DataProvider.getUserRoleProvider().getRoles(user.getId(), true); // cached instance

                // If user has got one of the specified roles, delegate to the requested page
                if(securedRoles.length == 0 || UserRoleProvider.hasSomeRole(roles, securedRoles)){
                    return delegate.call(ctx);
                } else {
                    // User is not authorized
                    return F.Promise.pure((Result)unauthorized(views.html.unauthorized.render(securedRoles)));
                }
            }
            catch(Throwable t) {
               throw new RuntimeException(t);
            }
        }

    }
}
