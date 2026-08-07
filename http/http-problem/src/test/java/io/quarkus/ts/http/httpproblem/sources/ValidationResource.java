package io.quarkus.ts.http.httpproblem.sources;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/problem")
@Produces(MediaType.APPLICATION_JSON)
public class ValidationResource {

    @POST
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response validate(@Valid ValidatedBean bean) {
        return Response.ok().build();
    }
}
