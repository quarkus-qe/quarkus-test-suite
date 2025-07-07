package io.quarkus.ts.http.restclient.reactive.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.ts.http.restclient.reactive.fault.tolerance.SpecificRetryService;

@Path("/ft-reflection")
public class FaultToleranceReflectionResource {

    @Inject
    SpecificRetryService specificRetryService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response response() {
        String result = specificRetryService.performAction();
        return Response.ok(result).build();
    }
}
