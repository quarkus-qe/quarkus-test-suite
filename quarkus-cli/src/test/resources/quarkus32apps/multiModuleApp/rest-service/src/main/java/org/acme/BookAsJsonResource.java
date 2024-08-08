package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Path("/book/json")
public class BookAsJsonResource {

    @GET
    public String get() {
        return "{\"title\":\"Title in Json\"}";
    }
}
