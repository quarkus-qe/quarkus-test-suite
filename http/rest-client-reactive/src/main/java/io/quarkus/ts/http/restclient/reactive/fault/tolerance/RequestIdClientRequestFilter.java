package io.quarkus.ts.http.restclient.reactive.fault.tolerance;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * Injects `REQUEST_ID` into the headers of every outgoing REST client call.
 * It is used in combination with the `RequestIdManager` class to ensure
 * request context propagation between client and server calls.
 */
@Provider
@ApplicationScoped
public class RequestIdClientRequestFilter implements ClientRequestFilter {

    private final RequestIdManager requestIdManager;

    public RequestIdClientRequestFilter(RequestIdManager requestIdManager) {
        this.requestIdManager = requestIdManager;
    }

    @Override
    public void filter(ClientRequestContext requestContext) {
        int requestId = requestIdManager.currentRequestId();
        requestContext.getHeaders().putSingle("REQUEST_ID", requestId);
    }
}
