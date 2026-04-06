package io.quarkus.ts.http.restclient.reactive.validation;

import java.util.List;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * Class used to mock the rest API of service
 */
@Path("/constraint-validation")
public class ConstraintValidationResource {

    @POST
    @Path("/not-null-list")
    public List<Body> postNotNullList(List<Body> body) {
        return body;
    }

    @POST
    @Path("/query-and-list")
    public List<Body> postQueryAndList(List<Body> body) {
        return body;
    }
}
