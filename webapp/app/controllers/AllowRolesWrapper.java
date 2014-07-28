package controllers;

import be.ugent.degage.db.models.UserRole;
import be.ugent.degage.db.models.UserStatus;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

import java.util.Set;

/**
 * Implements the {@link AllowRoles} annotation.
 */
public class AllowRolesWrapper extends Action<AllowRoles> {

    /**
     * Delegates the user to the given HTTP context if the user is authorized.
     * The authorized roles are retrieved from the RoleAuthenticated annotation.
     *
     * @param ctx The given HTTP context
     * @return The result, either the requested page or an unauthorized request page
     */
    public F.Promise<Result> call(Context ctx) {
        try {
            UserRole[] securedRoles = configuration.value();

            String statusString = ctx.session().get("status");

            if (statusString == null) {
                // not yet logged in
                return F.Promise.pure(redirect(routes.Login.login(ctx.request().path())));
            }

            UserStatus status = UserStatus.valueOf(statusString);
            if (status == UserStatus.BLOCKED || status == UserStatus.DROPPED || status == UserStatus.EMAIL_VALIDATING) {
                // not allowed to log in
                ctx.flash().put("danger", "Dit account is nog niet geactiveerd of werd geblokkeerd.");
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
