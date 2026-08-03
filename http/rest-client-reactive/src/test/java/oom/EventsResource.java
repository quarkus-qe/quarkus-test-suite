package oom;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/events")
public class EventsResource {
    @POST
    public Response accept() {
        return Response.noContent().build();
    }
}
