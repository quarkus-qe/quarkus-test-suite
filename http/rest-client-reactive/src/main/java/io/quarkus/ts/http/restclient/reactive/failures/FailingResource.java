package io.quarkus.ts.http.restclient.reactive.failures;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/fail")
public class FailingResource {
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final boolean[] fail = { true, true, true, true, false };
    // in earlier versions, connection was closed after three errors. We need to ensure, that this is not the case.

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/cyclic")
    public String fail() {
        int visit = counter.getAndIncrement();
        int index = visit % fail.length;
        if (fail[index]) {
            throw new RuntimeException("Whoops, we encountered a problem!");
        } else {
            return "You're the " + (visit + 1) + "th visitor of this page";
        }
    }
}
