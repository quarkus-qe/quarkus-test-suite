package io.quarkus.ts.http.restclient.vanilla;

import java.io.Closeable;
import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Useless API, used just for passing type controls in {@link RestCallerService}
 */
@Path("/useless")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface UselessRestApi extends Closeable {

    @GET
    @Path("all")
    List<String> getAll();
}
