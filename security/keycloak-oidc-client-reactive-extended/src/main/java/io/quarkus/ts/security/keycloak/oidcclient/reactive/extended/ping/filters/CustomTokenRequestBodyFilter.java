package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import io.quarkus.arc.Unremovable;
import io.quarkus.oidc.common.OidcEndpoint;
import io.quarkus.oidc.common.OidcRequestContextProperties;
import io.quarkus.oidc.common.OidcRequestFilter;
import io.vertx.mutiny.core.buffer.Buffer;

@ApplicationScoped
@Unremovable
@OidcEndpoint(value = OidcEndpoint.Type.TOKEN)
@Priority(1)
public class CustomTokenRequestBodyFilter implements OidcRequestFilter {
    private static final Logger LOG = Logger.getLogger(CustomTokenRequestBodyFilter.class);
    public static final List<String> interceptedMessageLogs = new CopyOnWriteArrayList<>();

    @Override
    public void filter(OidcRequestContext requestContext) {
        Buffer existingModification = requestContext.contextProperties()
                .get(OidcRequestContextProperties.REQUEST_BODY);

        String originalBody = (existingModification != null)
                ? existingModification.toString()
                : requestContext.requestBody().toString();
        LOG.infof("Original request body: %s", originalBody);
        interceptedMessageLogs.add("Original body captured");

        String modifiedBody = originalBody + "&custom_param=custom_value";
        LOG.infof("Modified body request : %s", modifiedBody);
        requestContext.requestBody(Buffer.buffer(modifiedBody));
        interceptedMessageLogs.add("Custom param added to request");
    }

}
