package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.arc.Unremovable;
import io.quarkus.oidc.common.OidcEndpoint;
import io.quarkus.oidc.common.OidcRequestContextProperties;
import io.quarkus.oidc.common.OidcRequestFilter;
import io.vertx.mutiny.core.buffer.Buffer;

@ApplicationScoped
@Unremovable
@OidcEndpoint(value = OidcEndpoint.Type.TOKEN)
public class ChainedParameterRequestFilter implements OidcRequestFilter {

    public static final List<String> interceptedMessageLogs = new CopyOnWriteArrayList<>();

    @Override
    public void filter(OidcRequestContext requestContext) {

        // Check if a previous filter in the chain already modified the request body
        // If so, we need to work with that modified version, not the original
        Buffer modifiedBody = requestContext.contextProperties().get(OidcRequestContextProperties.REQUEST_BODY);

        String currentBody = (modifiedBody != null)
                ? modifiedBody.toString()
                : requestContext.requestBody().toString();

        if (currentBody.contains("custom_param=custom_value")) {
            String newBody = currentBody + "&chained_param=added_by_second_filter";
            requestContext.requestBody(Buffer.buffer(newBody));
            interceptedMessageLogs.add("Second filter acted and added chained_param");
        }
    }
}
