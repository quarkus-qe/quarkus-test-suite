package io.quarkus.ts.http.restclient.reactive.goaway;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/goaway-server")
public class GoAwayServerResource {
    private static final byte[] CONTENT = new byte[10 * 1024];

    @GET
    @Path("/file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] download() {
        return CONTENT;
    }
}
