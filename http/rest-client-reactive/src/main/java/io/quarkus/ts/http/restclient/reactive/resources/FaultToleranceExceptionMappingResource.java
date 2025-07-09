package io.quarkus.ts.http.restclient.reactive.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.http.restclient.reactive.exceptions.MyCheckedException;
import io.quarkus.ts.http.restclient.reactive.fault.tolerance.FaultToleranceExceptionMappingClient;

@Path("/test-fault-tolerance")
public class FaultToleranceExceptionMappingResource {

    @Inject
    @RestClient
    FaultToleranceExceptionMappingClient client;

    @GET
    @Path("/trigger-exception")
    public String triggerException() throws MyCheckedException {
        return client.get();
    }
}
