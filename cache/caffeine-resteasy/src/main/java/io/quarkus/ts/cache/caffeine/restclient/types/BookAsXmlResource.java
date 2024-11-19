package io.quarkus.ts.cache.caffeine.restclient.types;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/book")
public class BookAsXmlResource {

    private static int counter = 0;

    @GET
    @Path("/xml-cache")
    @Produces(MediaType.APPLICATION_XML)
    public String helloCache() throws InterruptedException {
        counter++;
        return "<book><title>Title in Xml with counter equal to " + counter + "</title></book>";
    }

    @GET
    @Path("/xml-cache-invalidate")
    @Produces(MediaType.TEXT_PLAIN)
    public String invalidateCache() {
        return "xml cache was invalidated";
    }

    @GET
    @Path("/reset-counter-xml")
    public String resetCounter() {
        counter = 0;
        return "Counter reset";
    }
}
