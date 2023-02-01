package io.quarkus.ts.vertx;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;

@Path("/hello")
public class HelloResource {
    private final Vertx vertx;

    @Inject
    public HelloResource(Vertx vertx) {
        this.vertx = vertx;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Hello> getMessage(@QueryParam("name") @DefaultValue("World") String name) {
        return vertx.fileSystem()
                .readFile("message.template")
                .map(Buffer::toString)
                .map(string -> String.format(string, name).trim())
                .map(Hello::new);
    }
}
