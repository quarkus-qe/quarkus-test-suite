package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.rar;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.arc.Unremovable;
import io.quarkus.oidc.OidcRedirectFilter;
import io.quarkus.oidc.TenantFeature;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@TenantFeature("rar-redirect-wrong")
@ApplicationScoped
@Unremovable
public class RarRedirectWrongFilter implements OidcRedirectFilter {

    @Override
    public void filter(OidcRedirectContext redirectContext) {
        JsonArray authorizationDetailsArray = new JsonArray();

        JsonObject obj = new JsonObject();
        obj.put("type", "invalid_type");
        obj.put("credential_configuration_id", "wrong_credential");
        authorizationDetailsArray.add(obj);

        redirectContext.additionalQueryParams()
                .add("authorization_details", authorizationDetailsArray.encode());
    }
}
