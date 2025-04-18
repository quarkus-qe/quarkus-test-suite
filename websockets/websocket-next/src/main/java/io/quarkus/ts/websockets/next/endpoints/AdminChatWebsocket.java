package io.quarkus.ts.websockets.next.endpoints;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.websockets.next.OnError;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;

@WebSocket(path = "adminChat")
public class AdminChatWebsocket {
    @Inject
    SecurityIdentity currentIdentity;

    @RolesAllowed("admin")
    @OnTextMessage(broadcast = true)
    public String echo(String message) {
        return currentIdentity.getPrincipal().getName() + ": " + message;
    }

    @OnError
    public String error(ForbiddenException t) {
        return "forbidden: " + currentIdentity.getPrincipal().getName();
    }

    @OnError
    public String error(UnauthorizedException t) {
        return "forbidden anonymous";
    }
}
