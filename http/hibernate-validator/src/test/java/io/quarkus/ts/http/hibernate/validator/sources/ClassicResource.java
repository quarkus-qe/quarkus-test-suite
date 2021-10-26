package io.quarkus.ts.http.hibernate.validator.sources;

import javax.validation.constraints.Digits;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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