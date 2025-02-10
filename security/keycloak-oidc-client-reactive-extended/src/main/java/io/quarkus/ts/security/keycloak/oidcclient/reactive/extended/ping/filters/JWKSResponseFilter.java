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
@OidcEndpoint(OidcEndpoint.Type.JWKS)
public class JWKSResponseFilter implements OidcResponseFilter {

    private static final Logger LOG = Logger.getLogger(JWKSResponseFilter.class);
    public static final List<String> interceptedMessageLogs = new CopyOnWriteArrayList<>();

    @Override
    public void filter(OidcResponseContext responseContext) {
        LOG.info("JWKS response intercepted");
        interceptedMessageLogs.add("JWKS response intercepted");
        JsonObject jwks = responseContext.responseBody().toJsonObject();

        if (jwks.containsKey("keys")) {
            LOG.infof("JWKS contains %d keys", jwks.getJsonArray("keys").size());
            interceptedMessageLogs.add("JWKS contains " + jwks.getJsonArray("keys").size() + " keys ");
        }

        String contentType = responseContext.responseHeaders().get("Content-Type");
        LOG.infof("JWKS response Content-Type: %s", contentType);
        interceptedMessageLogs.add("JWKS response Content-Type: " + contentType);

    }
}
