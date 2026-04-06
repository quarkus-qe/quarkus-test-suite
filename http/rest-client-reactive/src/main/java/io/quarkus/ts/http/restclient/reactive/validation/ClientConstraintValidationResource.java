package io.quarkus.ts.http.restclient.reactive.validation;

import java.util.List;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/client/constraint-validation")
public class ClientConstraintValidationResource {

    @RestClient
    ConstraintValidationRestClient myRestClient;

    @POST
    @Path("/not-null-list")
    public List<Body> clientPostNotNullList(List<Body> bodyList) {
        return myRestClient.postNotNullList(bodyList);
    }

    @POST
    @Path("/query-and-list")
    public List<Body> clientPostQueryAndLis(@QueryParam("query") String query, List<Body> bodyList) {
        return myRestClient.postQueryAndList(query, bodyList);
    }
}
