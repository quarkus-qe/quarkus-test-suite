package http.sse;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/sse")
public class SseResource {

    @Inject
    SseClient client;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getSse() {
        try {
            return client.someMethod();
        } catch (RuntimeException exception) {
            return exception.getMessage();
        }
    }
}
