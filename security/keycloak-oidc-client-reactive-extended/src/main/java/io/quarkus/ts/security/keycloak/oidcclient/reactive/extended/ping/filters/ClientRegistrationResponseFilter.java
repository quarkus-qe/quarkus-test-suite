package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import io.quarkus.arc.Unremovable;
import io.quarkus.oidc.common.OidcEndpoint;
import io.quarkus.oidc.common.OidcResponseFilter;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;

@ApplicationScoped
@Unremovable
@OidcEndpoint(value = OidcEndpoint.Type.CLIENT_REGISTRATION)
public class ClientRegistrationResponseFilter implements OidcResponseFilter {

    private static final Logger LOG = Logger.getLogger(ClientRegistrationResponseFilter.class);
    public static final List<String> interceptedMessageLogs = new CopyOnWriteArrayList<>();

    @Override
    public void filter(OidcResponseContext responseContext) {
        LOG.info("ClientRegistrationResponseFilter invoked");
        interceptedMessageLogs.add("ClientRegistrationResponseFilter invoked");

        if (responseContext.statusCode() == 201 || responseContext.statusCode() == 200) {
            try {
                JsonObject body = responseContext.responseBody().toJsonObject();

                // Add custom metadata to response
                if (body.containsKey("client_id")) {
                    body.put("custom_metadata", "Added by filter");
                    responseContext.responseBody(Buffer.buffer(body.toString()));

                    LOG.info("Added custom_metadata to client registration response");
                    interceptedMessageLogs.add("Added custom_metadata to response");
                }
            } catch (Exception e) {
                LOG.errorf("Error processing client registration response: %s", e.getMessage());
                interceptedMessageLogs.add("Error processing response: " + e.getMessage());
            }
        }
    }
}
