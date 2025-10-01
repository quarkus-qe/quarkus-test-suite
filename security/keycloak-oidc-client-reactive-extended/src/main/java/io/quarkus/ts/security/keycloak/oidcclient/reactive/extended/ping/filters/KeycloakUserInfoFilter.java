package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.arc.Unremovable;
import io.quarkus.oidc.common.OidcEndpoint;
import io.quarkus.oidc.common.OidcResponseFilter;

@ApplicationScoped
@Unremovable
@OidcEndpoint(value = OidcEndpoint.Type.USERINFO)
public class KeycloakUserInfoFilter implements OidcResponseFilter {

    public static final List<String> interceptedMessageLogs = new CopyOnWriteArrayList<>();

    @Override
    public void filter(OidcResponseContext responseContext) {
        interceptedMessageLogs.add("UserInfo response intercepted by Keycloak-specific filter");
    }
}
