package io.quarkus.ts.websockets.next.endpoints;

import jakarta.inject.Inject;

import io.quarkus.oidc.BearerTokenAuthentication;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;

@WebSocket(path = "/bearer")
@BearerTokenAuthentication
public class AuthenticatedWebSocket {
    @Inject
    SecurityIdentity currentIdentity;

    @OnTextMessage(broadcast = true)
    public String echo(String message) {
        return currentIdentity.getPrincipal().getName() + ": " + message;
    }
}
