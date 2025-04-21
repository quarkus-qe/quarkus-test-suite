package io.quarkus.ts.websockets.next.endpoints;

import jakarta.inject.Inject;

import io.quarkus.security.Authenticated;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.PermissionChecker;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.websockets.next.OnError;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;

@Authenticated
@WebSocket(path = "/authChat")
public class AuthenticatedChatWebSocket {
    @Inject
    SecurityIdentity currentIdentity;

    @OnTextMessage(broadcast = true)
    @PermissionsAllowed("endpoint:invoke")
    public String echo(String message) {
        return currentIdentity.getPrincipal().getName() + ": " + message;
    }

    @OnError
    String error(ForbiddenException t) {
        return "forbidden:" + currentIdentity.getPrincipal().getName();
    }

    @PermissionChecker("endpoint:invoke")
    boolean canInvokeEndpoint(String message) {
        String principalName = currentIdentity.getPrincipal().getName();
        return principalName.equals("alice") || principalName.equals("bob");
    }

}
