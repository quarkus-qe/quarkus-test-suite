package io.quarkus.ts.http.restclient.reactive.fault.tolerance;

public interface RequestIdManager {

    int currentRequestId();
}
