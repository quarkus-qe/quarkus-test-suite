package io.quarkus.ts.http.minimum.reactive;

import jakarta.interceptor.Interceptors;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.smallrye.mutiny.Uni;

@Path("/operator")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Interceptors({ JakartaRestInterceptor.class })
public class OperatorResource {

    @POST
    public Uni<Response> postOperator(Operator operator) {
        return Uni.createFrom().item(Response.status(Response.Status.OK)
                .entity("Hello " + operator.getName()).build());
    }
}
