package io.quarkus.ts.http.restclient.reactive.validation;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/constraint-validation")
@RegisterRestClient
public interface ConstraintValidationRestClient {

    @POST
    @Path("/not-null-list")
    List<Body> postNotNullList(@NotNull List<@Valid Body> body);

    @POST
    @Path("/query-and-list")
    List<Body> postQueryAndList(@QueryParam("argument") @Size(min = 4, max = 32) String query, List<@Valid Body> body);
}
