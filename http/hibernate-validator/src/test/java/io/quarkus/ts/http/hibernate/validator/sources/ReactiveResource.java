package io.quarkus.ts.http.hibernate.validator.sources;

import javax.validation.constraints.Digits;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.RestPath;

import io.smallrye.mutiny.Uni;

@Path("/reactive")
public class ReactiveResource {

    @GET
    @Path("/validate-no-produces/{id}")
    public Uni<String> validateNoProduces(
            @Digits(integer = 5, fraction = 0, message = "numeric value out of bounds") @RestPath("id") String id) {
        return Uni.createFrom().item(id);
    }

    @GET
    @Path("/validate-multiple-produces/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN })
    public Uni<String> validateMultipleProduces(
            @Digits(integer = 5, fraction = 0, message = "numeric value out of bounds") @RestPath("id") String id) {
        return Uni.createFrom().item(id);
    }
}