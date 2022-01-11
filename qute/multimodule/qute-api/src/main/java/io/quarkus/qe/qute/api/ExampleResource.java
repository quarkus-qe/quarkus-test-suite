package io.quarkus.qe.qute.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class ExampleResource {

    private final AppMessagesProvider appMessagesProvider;

    public ExampleResource(AppMessagesProvider appMessagesProvider) {
        this.appMessagesProvider = appMessagesProvider;
    }

    @GET
    @Path("{lang}")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@PathParam("lang") String lang) {
        AlertMessages appMessages = appMessagesProvider.appMessages(lang);
        return appMessages.withoutParams() + ". " + appMessages.withParams("Nikos");
    }
}
