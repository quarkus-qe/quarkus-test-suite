package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import io.quarkus.arc.Unremovable;
import io.quarkus.oidc.common.OidcEndpoint;
import io.quarkus.oidc.common.OidcResponseFilter;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
@Unremovable
@OidcEndpoint(value = OidcEndpoint.Type.USERINFO)
public class UserinfoResponseFilter implements OidcResponseFilter {
    private static final Logger LOG = Logger.getLogger(UserinfoResponseFilter.class);

    @Override
    public void filter(OidcResponseContext responseContext) {
        LOG.info("UserinfoResponseFilter invoked!");
        if (responseContext.responseBody() != null) {
            String contentType = responseContext.responseHeaders().get("Content-Type");
            if (contentType == null || !contentType.startsWith("application/json")) {
                LOG.warn("Userinfo response is not JSON - skipping checks");
                return;
            }

            JsonObject body = responseContext.responseBody().toJsonObject();
            if (body.containsKey("sub")) {
                String sub = body.getString("sub");
                LOG.infof("Userinfo 'sub': %s", sub);
            } else {
                LOG.warn("Userinfo response missing 'sub' claim!");
            }

            if (body.containsKey("preferred_username")) {
                String username = body.getString("preferred_username");
                LOG.infof("Userinfo 'preferred_username': %s", username);
            } else {
                LOG.warn("'preferred_username' claim not found in userinfo");
            }
        }
    }
}
