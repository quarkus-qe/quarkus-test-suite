package io.quarkus.ts.http.advanced.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.RestResponse;

@Path("/morning")
public class MorningResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public RestResponse<String> morning() {
        return RestResponse.ResponseBuilder.ok("Buenos dias", MediaType.TEXT_PLAIN_TYPE)
                .build();
    }
}
