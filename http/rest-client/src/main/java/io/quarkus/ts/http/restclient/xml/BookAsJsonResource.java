package io.quarkus.ts.http.restclient.xml;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Path("/book/json")
public class BookAsJsonResource {

    @GET
    public String get() {
        return "{\"title\":\"Title in Json\"}";
    }
}
