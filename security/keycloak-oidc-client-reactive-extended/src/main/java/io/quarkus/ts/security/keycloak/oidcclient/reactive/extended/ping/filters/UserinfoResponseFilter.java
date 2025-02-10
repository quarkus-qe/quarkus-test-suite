package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    public static final List<String> interceptedMessageLogs = new CopyOnWriteArrayList<>();

    @Override
    public void filter(OidcResponseContext responseContext) {
        LOG.info("UserinfoResponseFilter invoked!");
        interceptedMessageLogs.add("UserinfoResponseFilter invoked!");
        if (responseContext.responseBody() != null) {
            String contentType = responseContext.responseHeaders().get("Content-Type");
            if (contentType == null || !contentType.startsWith("application/json")) {
                LOG.warn("Userinfo response is not JSON - skipping checks");
                interceptedMessageLogs.add("Userinfo response is not JSON - skipping checks");
                return;
            }

            JsonObject body = responseContext.responseBody().toJsonObject();
            if (body.containsKey("sub")) {
                String sub = body.getString("sub");
                LOG.infof("Userinfo 'sub': %s", sub);
                interceptedMessageLogs.add("Userinfo sub: " + sub);
            } else {
                LOG.warn("Userinfo response missing 'sub' claim!");
                interceptedMessageLogs.add("Userinfo response missing 'sub' claim!");
            }

            if (body.containsKey("preferred_username")) {
                String username = body.getString("preferred_username");
                LOG.infof("Userinfo 'preferred_username': %s", username);
                interceptedMessageLogs.add("Userinfo preferred_username: " + username);
            } else {
                LOG.warn("'preferred_username' claim not found in userinfo");
                interceptedMessageLogs.add("'preferred_username' claim not found in userinfo");
            }
        }
    }
}
