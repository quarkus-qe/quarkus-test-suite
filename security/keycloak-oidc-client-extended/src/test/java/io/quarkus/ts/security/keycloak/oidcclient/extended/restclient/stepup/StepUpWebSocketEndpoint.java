package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.stepup;

import io.quarkus.oidc.AuthenticationContext;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;

@AuthenticationContext("silver")
@WebSocket(path = "/ws/step-up/silver")
public class StepUpWebSocketEndpoint {

    @OnOpen
    public String onOpen() {
        return "WebSocket opened with ACR silver";
    }

    @OnTextMessage
    public String onMessage(String message) {
        return "Echo with silver ACR: " + message;
    }
}
