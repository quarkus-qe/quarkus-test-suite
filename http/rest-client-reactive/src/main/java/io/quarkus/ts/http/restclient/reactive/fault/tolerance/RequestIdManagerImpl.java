package io.quarkus.ts.http.restclient.reactive.fault.tolerance;

import jakarta.enterprise.context.RequestScoped;

/**
 * Manages the `REQUEST_ID` for each request, allowing it
 * to be overridden when the `REQUEST_ID` is passed via headers from client requests.
 * Handles a unique id per request.
 */
@RequestScoped
public class RequestIdManagerImpl implements RequestIdManager {

    private int requestID;

    public int currentRequestId() {
        return requestID;
    }

    public void overrideRequestId(int inboundRequestId) {
        this.requestID = inboundRequestId;
    }
}
