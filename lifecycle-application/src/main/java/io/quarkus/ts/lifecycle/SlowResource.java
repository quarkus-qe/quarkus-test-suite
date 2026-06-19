package io.quarkus.ts.lifecycle;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;

@Path("/slow")
public class SlowResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() throws InterruptedException {
        Thread.sleep(10_000);
        return "Thank you for waiting!";
    }

    @GET
    @Path("/reactive")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> getReactive() {
        return Uni.createFrom().item("Thank you for waiting!").map(str -> {
            try {
                Thread.sleep(10_000);
                return str;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        });
    }
}
