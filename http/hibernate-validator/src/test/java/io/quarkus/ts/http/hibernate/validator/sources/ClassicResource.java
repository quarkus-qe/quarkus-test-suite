package io.quarkus.ts.http.hibernate.validator.sources;

import jakarta.validation.constraints.Digits;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/classic")
public class ClassicResource {

    @GET
    @Path("/validate-no-produces/{id}")
    public String validateNoProduces(
            @Digits(integer = 5, fraction = 0, message = "numeric value out of bounds") @PathParam("id") String id) {
        return id;
    }

    @GET
    @Path("/validate-multiple-produces/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN })
    public String validateMultipleProduces(
            @Digits(integer = 5, fraction = 0, message = "numeric value out of bounds") @PathParam("id") String id) {
        return id;
    }
}