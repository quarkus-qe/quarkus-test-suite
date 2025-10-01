package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import io.quarkus.arc.Unremovable;
import io.quarkus.oidc.common.OidcEndpoint;
import io.quarkus.oidc.common.OidcRequestFilter;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;

@ApplicationScoped
@Unremovable
@OidcEndpoint(value = OidcEndpoint.Type.CLIENT_REGISTRATION)
public class ClientRegistrationRequestFilter implements OidcRequestFilter {

    public static final List<String> interceptedMessageLogs = new CopyOnWriteArrayList<>();
    private static final Logger LOG = Logger.getLogger(ClientRegistrationRequestFilter.class);

    @Override
    public void filter(OidcRequestContext requestContext) {
        LOG.info("ClientRegistrationRequestFilter invoked");
        interceptedMessageLogs.add("ClientRegistrationRequestFilter invoked");
        try {
            JsonObject body = requestContext.requestBody().toJsonObject();

            // Modify client_name if present
            if (body.containsKey("client_name")) {
                String originalName = body.getString("client_name");
                LOG.infof("Client name is %s", originalName);
                String modifiedName = "Modified_" + originalName;
                body.put("client_name", modifiedName);
                requestContext.requestBody(Buffer.buffer(body.toString()));
                LOG.info(String.format("ClientRegistrationRequestFilter received request %s", modifiedName));
                interceptedMessageLogs.add("Modified client_name from '" + originalName + "' to '" + modifiedName + "'");
            }
        } catch (Exception e) {
            interceptedMessageLogs.add("Error processing request: " + e.getMessage());
        }
    }
}
