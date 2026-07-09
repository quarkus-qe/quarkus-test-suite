package io.quarkus.ts.openshift.security.basic;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import io.quarkus.security.Authenticated;

@Path("/lost-context")
@Authenticated
public class RequestContextLost {

    @Context
    HttpHeaders requestHeaders;

    @POST
    @Path("delay")
    public Response delay(@QueryParam("wait_time") @DefaultValue("-1") final long waitTime) throws Exception {
        Thread.sleep(waitTime);
        requestHeaders.getDate();
        return Response.ok().build();
    }
}
