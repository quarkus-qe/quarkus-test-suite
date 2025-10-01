package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.arc.Unremovable;
import io.quarkus.oidc.common.OidcEndpoint;
import io.quarkus.oidc.common.OidcResponseFilter;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;

@ApplicationScoped
@Unremovable
@OidcEndpoint(value = OidcEndpoint.Type.TOKEN)
public class CustomTokenResponseBodyFilter implements OidcResponseFilter {

    public static final List<String> interceptedMessageLogs = new CopyOnWriteArrayList<>();

    @Override
    public void filter(OidcResponseContext responseContext) {
        if (responseContext.responseBody() == null) {
            return;
        }
        interceptedMessageLogs.add("Response body intercepted");
        JsonObject body = responseContext.responseBody().toJsonObject();

        // Modify scope format from comma-separated to space-separated
        if (body.containsKey("scope")) {
            String scope = body.getString("scope");
            if (scope != null && scope.contains(",")) {
                String modifiedScope = scope.replace(",", " ");
                body.put("scope", modifiedScope);
                responseContext.responseBody(Buffer.buffer(body.toString()));
                interceptedMessageLogs.add("Scope corrected from '" + scope + "' to '" + modifiedScope + "'");
            }
        }
    }
}
