package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import io.quarkus.arc.Unremovable;
import io.quarkus.oidc.common.OidcEndpoint;
import io.quarkus.oidc.common.OidcResponseFilter;
import io.quarkus.oidc.common.runtime.OidcConstants;

@ApplicationScoped
@Unremovable
@OidcEndpoint(value = OidcEndpoint.Type.TOKEN)
public class TokenResponseFilter implements OidcResponseFilter {

    private static final Logger LOG = Logger.getLogger(TokenResponseFilter.class);
    public static final List<String> interceptedMessageLogs = new CopyOnWriteArrayList<>();

    @Override
    public void filter(OidcResponseContext responseContext) {
        String grantType = responseContext.requestProperties().get(OidcConstants.GRANT_TYPE);
        LOG.infof("Grant Type: %s", grantType);
        interceptedMessageLogs.add("TokenResponseFilter invoked with grant type: " + grantType);

        String contentType = responseContext.responseHeaders().get("Content-Type");
        if (contentType != null && contentType.equals("application/json")
                && responseContext.responseBody().toJsonObject().containsKey("refresh_token")
                && "refresh_token".equals(responseContext.requestProperties().get(OidcConstants.GRANT_TYPE))) {
            LOG.info("***.Tokens have been refreshed");
            interceptedMessageLogs.add("Tokens have been refreshed");
        }

    }
}
