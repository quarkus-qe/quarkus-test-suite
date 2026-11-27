package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.arc.Unremovable;
import io.quarkus.oidc.common.OidcEndpoint;
import io.quarkus.oidc.common.OidcResponseFilter;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;

@ApplicationScoped
@Unremovable
@OidcEndpoint(value = OidcEndpoint.Type.TOKEN)
public class CustomTokenResponseBodyFilter implements OidcResponseFilter {

    public static final List<String> interceptedMessageLogs = new CopyOnWriteArrayList<>();

    @Override
    public Uni<Void> filter(OidcResponseFilterContext responseContext) {
        if (responseContext.responseBody() == null) {
            return Uni.createFrom().voidItem();
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
        return Uni.createFrom().voidItem();
    }
}
