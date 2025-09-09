package io.quarkus.ts.http.restclient.reactive.validation;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/validator-check")
public class UserValidationResource {

    @Inject
    UserProcessingServiceBean service;

    @POST
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ValidatedUser check(@Valid ValidatedUser entity) {
        return service.process(entity);
    }
}
